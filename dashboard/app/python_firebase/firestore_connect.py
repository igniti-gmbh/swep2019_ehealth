from firebase_admin import firestore
from .firebase_connect import firebase_app

store = firestore.client(app=firebase_app)
