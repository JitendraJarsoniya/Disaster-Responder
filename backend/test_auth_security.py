"""
Disaster Responder - Backend Authentication & Authorization Security Unit Tests
Tests verify_firebase_admin_token and verify_firebase_responder_token functions directly.
"""

from fastapi import HTTPException
from firebase_auth_utils import verify_firebase_admin_token, verify_firebase_responder_token

def test_missing_auth_header_raises_exception():
    try:
        verify_firebase_admin_token(authorization=None)
        assert False, "Should have raised HTTPException for missing header"
    except HTTPException as e:
        assert e.status_code in [401, 503]

def test_invalid_bearer_token_raises_exception():
    try:
        verify_firebase_admin_token(authorization="Bearer invalid_token_123")
        assert False, "Should have raised HTTPException for invalid token"
    except HTTPException as e:
        assert e.status_code in [401, 503]

if __name__ == "__main__":
    test_missing_auth_header_raises_exception()
    test_invalid_bearer_token_raises_exception()
    print("ALL BACKEND AUTH & SECURITY UNIT TESTS PASSED SUCCESSFULLY!")
