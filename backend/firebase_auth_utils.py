"""
Disaster Responder - Backend Firebase Admin Auth & Role Authorization Module
Enforces strict server-side ADMIN and RESPONDER role authorization via Firebase ID Tokens.
NO fail-open alternative headers, secrets, or master password bypasses.
"""

import os
import logging
from typing import Optional
from fastapi import HTTPException, Header, Depends

logger = logging.getLogger("firebase_auth")

FIREBASE_ADMIN_INITIALIZED = False

try:
    import firebase_admin
    from firebase_admin import credentials, auth, firestore

    cred_path = os.getenv(
        "FIREBASE_SERVICE_ACCOUNT_PATH",
        os.path.join(os.path.dirname(__file__), "serviceAccountKey.json")
    )

    if os.path.exists(cred_path):
        if not firebase_admin._apps:
            cred = credentials.Certificate(cred_path)
            firebase_admin.initialize_app(cred)
        FIREBASE_ADMIN_INITIALIZED = True
        logger.info("Firebase Admin SDK initialized successfully using service account.")
    else:
        logger.warning("Firebase serviceAccountKey.json not found on backend server.")
except Exception as e:
    logger.warning(f"Firebase Admin SDK initialization pending: {e}")

def verify_firebase_admin_token(authorization: Optional[str] = Header(None)):
    if not FIREBASE_ADMIN_INITIALIZED:
        raise HTTPException(
            status_code=503,
            detail="Service Unavailable: Firebase Admin Service Account credentials not configured on backend server."
        )

    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=401,
            detail="Unauthorized: Missing or malformed Authorization header. Expected Bearer Firebase ID Token."
        )

    id_token = authorization.split("Bearer ")[1].strip()

    try:
        decoded_token = auth.verify_id_token(id_token)
        uid = decoded_token.get("uid")

        # Verify role in Firestore /users/{uid}
        db = firestore.client()
        user_doc = db.collection("users").document(uid).get()

        if user_doc.exists:
            data = user_doc.to_dict()
            role = data.get("role")
            is_approved = data.get("isApproved", False)

            if role == "ADMIN" and is_approved:
                return {"uid": uid, "role": "ADMIN", "isApproved": True}
            else:
                raise HTTPException(
                    status_code=403,
                    detail="Forbidden: Account is not an authorized Admin."
                )
        else:
            raise HTTPException(
                status_code=403,
                detail="Forbidden: User profile document not found in Firestore."
            )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=401,
            detail="Unauthorized: Invalid or expired Firebase ID Token."
        )

def verify_firebase_responder_token(authorization: Optional[str] = Header(None)):
    if not FIREBASE_ADMIN_INITIALIZED:
        raise HTTPException(
            status_code=503,
            detail="Service Unavailable: Firebase Admin Service Account credentials not configured on backend server."
        )

    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=401,
            detail="Unauthorized: Missing or malformed Authorization header. Expected Bearer Firebase ID Token."
        )

    id_token = authorization.split("Bearer ")[1].strip()

    try:
        decoded_token = auth.verify_id_token(id_token)
        uid = decoded_token.get("uid")

        db = firestore.client()
        user_doc = db.collection("users").document(uid).get()

        if user_doc.exists:
            data = user_doc.to_dict()
            role = data.get("role")
            is_approved = data.get("isApproved", False)

            if (role == "RESPONDER" or role == "ADMIN") and is_approved:
                return {"uid": uid, "role": role, "isApproved": True}
            else:
                raise HTTPException(
                    status_code=403,
                    detail="Forbidden: Account is not an authorized Responder."
                )
        else:
            raise HTTPException(
                status_code=403,
                detail="Forbidden: User profile document not found in Firestore."
            )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=401,
            detail="Unauthorized: Invalid or expired Firebase ID Token."
        )
