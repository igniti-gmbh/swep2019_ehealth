import json
from .firebase_custom import Firebase
from .firestore_connect import store

jsonfile = open('./app/python_firebase/assets/FirebaseKey.json')
firebaseConfig = json.load(jsonfile)

firebaseObj = Firebase(firebaseConfig)
fireauth = firebaseObj.auth()


class Userdata(object):

    def __init__(self):
        self.user = None
        self.uid = None
        self.doc = None

    def get_data(self):
        self.user = fireauth.current_user
        self.uid = self.user['localId']
        self.doc = store.document('users', self.uid).get()

    def daily_steps(self):
        steps = self.doc.get('steps_today_total')
        return steps

    def step_goal(self):
        daily_steps = self.daily_steps()

        step_goal = self.doc.get('daily_step_goal')

        return (daily_steps / step_goal) * 100

    def displayName(self):
        return self.user['displayName']

    def room(self):
        room = self.doc.get('room')

        if room is None:
            room = 'Nicht angegeben'

        return room

    def age(self):
        age = self.doc.get('age')

        if age is None:
            age = 'Nicht angegeben'

        return age


userdata = Userdata()
