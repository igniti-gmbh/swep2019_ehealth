const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

let db = admin.firestore();

/* User wird in Firebase eingefügt */
exports.addUserToDB = functions.auth.user().onCreate((user) => {

    let data = {
        email: user.email,
        room: null,
        position: null,
        steps_device: null,
        steps_today_total: 0,
        daily_step_goal: 10000,
        age: null,
    };


    return db.doc('users/' + user.uid).set(data);
});

/* User wird aus Firebase gelöscht */
exports.deleteUserFromDB = functions.auth.user().onDelete((user) => {
   db.collection('users').doc(user.uid).delete()
       .catch(err => {console.log('User konnte nicht entfernt werden :' + err)})
});


/* Aggregiert Daten */
exports.moveSteps = functions.firestore
    .document('/devices/{deviceId}/steps/{docId}')
    .onCreate((snap, context) => {

        // User ID, die zum device gehört holen.
        const deviceId = context.params.deviceId;
        const user = db.doc('devices/' + deviceId).get()
            .then(documentSnapshot => {
                return documentSnapshot.get('userId');
            });


        // Wenn kein User existiert, dann Abbruch
        if (!user.exists) {
            return;
        }

        // Erstelltes Document kopieren
        const newDoc = snap.data();


        // In Javascript Timestamp verwandeln
        const timestamp = newDoc.timestamp.toDate();
        // In Variablen teilen
        const day = timestamp.getDate();
        const month = timestamp.getMonth();
        const year = timestamp.getFullYear();
        const hours = timestamp.getHours();
        // Value Variable
        const value = newDoc.value;
        // Referenz wohin Wert geschrieben werden soll
        let docRef = db.doc('users/' + user + '/' + year + '/' + month + '/' + day + '/' + hours);


        // Schritte zu Tagesergebnis hinzufügen
        addToToday(timestamp, user, value);

        // Schritte zu User verschieben
        let current_steps = docRef.data().steps;

        if (!current_steps) {
            current_steps = 0;
        }

        // Löscht das aktuelle Dokument aus dem devices Bereich
        let isdeleted = snap.ref.delete().catch(err => { console.log('Doc konnte nicht gelöscht werden: ' + err)});


        if (isdeleted) {
            docRef.set({steps: current_steps + value}, {merge: true}).catch(err => {
                console.log('Fehler beim Ändern der Stundenschritte: ' + err)
            });
        }
    });

// Vergleicht ob Snapshot von heute ist und fuegt es den daily steps hinzu
function addToToday(docTimestamp, userId, value) {

    const docDate = docTimestamp.setHours(0, 0, 0, 0);
    const currentDate = new Date().setHours(0, 0, 0, 0);

    const userRef = db.doc('users/' + userId);
    const steps = userRef.data().steps_today_total;

    if (docDate.getTime() === currentDate.getTime()) {
        return userRef.set({steps_today_total: steps + value}, {merge: true});
    } else {
        return false;
    }
}