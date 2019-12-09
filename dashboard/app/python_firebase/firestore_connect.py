import firebase_admin
from firebase_admin import credentials, firestore

cred = credentials.Certificate('./app/python_firebase/assets/FirestoreKey.json')
app = firebase_admin.initialize_app(cred)

store = firestore.client()
