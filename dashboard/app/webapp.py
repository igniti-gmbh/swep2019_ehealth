# Server Routes for Flask
import datetime
import time

import flask
from firebase_admin import auth, firestore
from firebase_admin import exceptions
from flask import Blueprint, render_template, redirect, url_for, request, flash, session

from .python_firebase.firebase_connect import firebase_app


server_bp = Blueprint('server_bp', __name__)

client = firestore.client(app=firebase_app)


@server_bp.route('/login')
def login():
    return render_template('login.html', navigation=dynamic_nav())


@server_bp.route('/login', methods=['POST'])
def login_post():
    from .python_firebase.firebase_custom import fireAuth

    email = request.form.get('email')
    password = request.form.get('password')

    user = fireAuth.sign_in_with_email_and_password(email, password)
    id_token = user['idToken']

    try:
        # Create the session cookie. This will also verify the ID token in the process.
        # The session cookie will have the same claims as the ID token.
        decoded_claims = auth.verify_id_token(id_token, app=firebase_app)

        # Only process if the user signed in within the last 5 minutes.
        if time.time() - decoded_claims['auth_time'] < 5 * 60:
            expires_in = datetime.timedelta(days=5)
            expires = datetime.datetime.now() + expires_in
            session_cookie = auth.create_session_cookie(id_token, expires_in=expires_in, app=firebase_app)
            response = flask.make_response(redirect(url_for('server_bp.profile')))

            # Cookie setzen, wenn unter SSL
            # response.set_cookie("session", session_cookie, expires=expires, httponly=True, secure=True,
            # domain='localhosz:5000')
            response.set_cookie("loggedInCookie", session_cookie, expires=expires, httponly=True)
            return response
        # User did not sign in recently. To guard against ID token theft, require
        # re-authentication.
        return flask.abort(401, 'Recent sign in required')
    except auth.InvalidIdTokenError:
        return flask.abort(401, 'Invalid ID token')
    except exceptions.FirebaseError:
        return flask.abort(401, 'Failed to create a session cookie')


@server_bp.route('/signup')
def signup():
    return render_template('signup.html', navigation=dynamic_nav())


@server_bp.route('/signup', methods=['POST'])
def signup_post():
    name = request.form.get('name')
    email = request.form.get('email')
    password = request.form.get('password')

    new_user = auth.create_user(email=email, password=password, display_name=name, app=firebase_app)

    if new_user is not None:
        return redirect('/login')
    else:
        flash('Nutzer konnte nicht registriert werden.')
        return redirect(url_for('/signup'))


@server_bp.route('/logout')
def logout():
    session_cookie = request.cookies.get('loggedInCookie')
    try:
        decoded_claims = auth.verify_session_cookie(session_cookie, app=firebase_app)
        auth.revoke_refresh_tokens(decoded_claims['sub'], app=firebase_app)
        response = flask.make_response(redirect('/login'))
        response.set_cookie('session', expires=0)
        return response
    except auth.InvalidSessionCookieError:
        return redirect('/login')


@server_bp.route('/')
def index():
    return render_template('index.html', navigation=dynamic_nav())


@server_bp.route('/profile')
def profile():

    decoded_cookie = has_cookie_access()
    if not decoded_cookie:
        return redirect('/login')
    else:
        session['uid'] = decoded_cookie['uid']

        document = client.document('users', session['uid']).get().to_dict()
        session['age'] = document['age']
        session['daily_step_goal'] = document['daily_step_goal']
        session['position'] = document['position']
        session['room'] = document['steps_device']
        session['steps_today_total'] = document['steps_today_total']

        return render_template('profile.html', navigation=dynamic_nav())


@server_bp.route('/dashboard')
def dashboard():
    decoded_cookie = has_cookie_access()
    if not decoded_cookie:
        return redirect('/login')
    else:
        return redirect('/dashboard/')


def dynamic_nav():
    session_cookie = has_cookie_access()
    if not session_cookie:
        return "mainnav.html"
    else:
        return "usernav.html"


def has_cookie_access():
    session_cookie = request.cookies.get('loggedInCookie')
    if not session_cookie:
        return False
    try:
        decode_claims = auth.verify_session_cookie(session_cookie, check_revoked=True, app=firebase_app)
        return decode_claims
    except auth.InvalidSessionCookieError:
        # Session cookie is invalid, expired or revoked. Force user to login.
        return False
