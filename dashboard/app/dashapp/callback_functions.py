from flask import request, session
from firebase_admin import firestore, auth
from ..python_firebase.firebase_connect import firebase_app

client = firestore.client(app=firebase_app)


def has_cookie_access():
    try:
        session_cookie = request.cookies.get('loggedInCookie')
    except RuntimeError:
        session_cookie = None
    if session_cookie is None:
        return None
    try:
        decode_claims = auth.verify_session_cookie(session_cookie, check_revoked=True, app=firebase_app)
        return decode_claims
    except auth.InvalidSessionCookieError:
        # Session cookie is invalid, expired or revoked. Force user to login.
        return None