# Customized Versions of imported Libraries for Firebase

import json
from firebase import Firebase

jsonFile = open('./app/python_firebase/assets/FirebaseRESTKey.json')
firebaseConfig = json.load(jsonFile)

firebaseObj = Firebase(firebaseConfig)
fireAuth = firebaseObj.auth()
