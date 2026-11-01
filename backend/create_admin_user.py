"""
Disaster Responder - Secure Local Admin Provisioning CLI Tool
One-time interactive CLI script to provision verified Admin user documents in Cloud Firestore (/users/{uid}).

Usage Instructions:
    1. Open a terminal in the backend directory:
       cd backend
    2. Run the script:
       python create_admin_user.py
    3. Follow the interactive prompts to enter the user's Firebase Auth UID, display name, and email.
"""

import sys
import os
from datetime import datetime, timezone

# Add current directory to Python path
sys.path.insert(0, os.path.dirname(__file__))

from firebase_auth_utils import FIREBASE_ADMIN_INITIALIZED
import firebase_admin
from firebase_admin import auth, firestore

def main():
    print("=" * 65)
    print(" DISASTER RESPONDER - SECURE LOCAL ADMIN PROVISIONING TOOL")
    print("=" * 65)

    if not FIREBASE_ADMIN_INITIALIZED:
        print("[ERROR] Firebase Admin SDK is not initialized. Ensure 'serviceAccountKey.json' exists in 'backend/'.")
        sys.exit(1)

    print("\n[INFO] Connected to Firebase Project:", firebase_admin.get_app().project_id)
    print("-" * 65)

    # 1. Prompt for User UID
    uid = input("\nEnter the Firebase Authentication User UID (from Firebase Console): ").strip()
    if not uid:
        print("[ERROR] UID cannot be empty.")
        sys.exit(1)

    # 2. Verify User exists in Firebase Authentication
    try:
        user_record = auth.get_user(uid)
        print(f"[SUCCESS] Verified Firebase Auth User found:")
        print(f" - UID:   {user_record.uid}")
        print(f" - Email: {user_record.email}")
    except auth.UserNotFoundError:
        print(f"[ERROR] User with UID '{uid}' does not exist in Firebase Authentication.")
        sys.exit(1)
    except Exception as e:
        print(f"[ERROR] Failed to fetch Firebase Auth user: {e}")
        sys.exit(1)

    # 3. Prompt for Display Name and Email Verification
    print("\n" + "-" * 65)
    display_name = input("Enter Admin Display Name (e.g., Dr. Rajesh Kumar): ").strip()
    if not display_name:
        print("[ERROR] Display name cannot be empty.")
        sys.exit(1)

    confirm_email = input(f"Confirm Admin Email address (must match '{user_record.email}'): ").strip()
    if confirm_email.lower() != (user_record.email or "").lower():
        print(f"[ERROR] Email mismatch. Entered '{confirm_email}' does not match Firebase Auth email '{user_record.email}'.")
        sys.exit(1)

    # 4. Check if Firestore document already exists
    db = firestore.client()
    doc_ref = db.collection("users").document(uid)
    doc_snap = doc_ref.get()

    if doc_snap.exists:
        print(f"\n[ERROR] Firestore document 'users/{uid}' ALREADY EXISTS. Overwrite refused to prevent security escalation.")
        sys.exit(1)

    # 5. Safe creation using doc_ref.create(...)
    now_iso = datetime.now(timezone.utc).isoformat()
    admin_data = {
        "uid": uid,
        "displayName": display_name,
        "email": user_record.email,
        "role": "ADMIN",
        "isApproved": True,
        "createdAt": now_iso,
        "updatedAt": now_iso
    }

    try:
        doc_ref.create(admin_data)
        print("\n" + "=" * 65)
        print(" [SUCCESS] ADMIN PROFILE CREATED IN CLOUD FIRESTORE")
        print("=" * 65)
        print(f" Document Path: users/{uid}")
        print(f" Display Name:  {display_name}")
        print(f" Role:          ADMIN")
        print(f" Status:        APPROVED (isApproved = True)")
        print("=" * 65)
    except Exception as e:
        print(f"[ERROR] Failed to create Firestore document: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
