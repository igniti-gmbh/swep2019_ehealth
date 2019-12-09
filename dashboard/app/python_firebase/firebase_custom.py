import json
import requests
from firebase import Auth as auth_old
from firebase import Firebase as firebase_old
from firebase import raise_detailed_error


class Firebase(firebase_old):
    pass

    def auth(self):
        return Auth(self.api_key, self.requests, self.credentials)


class Auth(auth_old):
    pass

    def change_username(self, id_token, name):
        request_ref = "https://identitytoolkit.googleapis.com/v1/accounts:update?key={}".format(
            self.api_key
        )
        headers = {"content-type": "application/json; charset=UTF-8"}
        data = json.dumps({"idToken": id_token, "displayName": name, "returnSecureToken": True})
        request_object = requests.post(request_ref, headers=headers, data=data)
        raise_detailed_error(request_object)
        return request_object.json()

    def logout_user(self):
        self.current_user = None

    def is_user(self):
        if self.current_user is None:
            return False
        else:
            return True
