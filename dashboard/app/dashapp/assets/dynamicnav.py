from app.python_firebase.firebase_connect import fireauth


def dynamicnav():
    if fireauth.is_user():
        return "usernav.html"
    else:
        return "mainnav.html"
