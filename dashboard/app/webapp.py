import json
import requests
from flask import Blueprint, render_template, redirect, url_for, request, flash
from app.dashapp.assets.dynamicnav import dynamicnav
from app.python_firebase.firebase_connect import fireauth, userdata

server_bp = Blueprint('server_bp', __name__)


@server_bp.route('/login')
def login():
    return render_template('login.html', navigation=dynamicnav())


@server_bp.route('/login', methods=['POST'])
def login_post():
    email = request.form.get('email')
    password = request.form.get('password')

    try:
        fireauth.sign_in_with_email_and_password(email, password)
        userdata.get_data()
    except requests.exceptions.HTTPError as e:
        if e != "":
            code = json.loads(e.strerror)
            flash(code["error"]["message"])
            return redirect(url_for('server_bp.login'))

    return redirect(url_for("server_bp.profile"))


@server_bp.route('/signup')
def signup():
    return render_template('signup.html', navigation=dynamicnav())


@server_bp.route('/signup', methods=['POST'])
def signup_post():
    name = request.form.get('name')
    email = request.form.get('email')
    password = request.form.get('password')

    try:
        user = fireauth.create_user_with_email_and_password(email, password)
    except requests.exceptions.HTTPError as e:
        if e != "":
            code = json.loads(e.strerror)
            flash(code["error"]["message"])
            return redirect(url_for('server_bp.signup'))

    fireauth.send_email_verification(user['idToken'])
    fireauth.change_username(user['idToken'], name)
    return redirect(url_for('server_bp.login'))


@server_bp.route('/passwordreset')
def passwordreset():
    return render_template('passwordreset.html', navigation=dynamicnav())


@server_bp.route('/passwordreset')
def passwordreset_post():
    email = request.form.get('email')

    try:
        fireauth.request_passwordreset(email=email)
    except requests.exceptions.HTTPError as e:
        if e != "":
            code = json.loads(e.strerror)
            flash(code["error"]["message"])
            return redirect(url_for('server_bp.passwordreset'))

    flash('Sie erhalten eine E-Mail zum Zurücksetzen Ihrer E-Mail.')
    return redirect(url_for('server_bp.passwordreset'))

@server_bp.route('/logout')
def logout():
    fireauth.logout_user()

    return redirect(url_for('server_bp.index'))

@server_bp.route('/')
def index():
    return render_template('index.html', navigation=dynamicnav())


@server_bp.route('/profile')
def profile():
    if fireauth.current_user:
        return render_template('profile.html', name=fireauth.current_user['displayName'], navigation=dynamicnav())
    else:
        return redirect(url_for("server_bp.login"))


@server_bp.route('/dashboard')
def dashboard():
    #if not fireauth.is_user():
        # return redirect(url_for('server_bp.login'))
    return True
