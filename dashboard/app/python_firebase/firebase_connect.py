import firebase_admin
from firebase_admin import credentials
import os

dirname = os.path.dirname(__file__)
filename = os.path.join(dirname, 'assets/FirebaseKey.json')

cred = credentials.Certificate(filename)
firebase_app = firebase_admin.initialize_app(cred)