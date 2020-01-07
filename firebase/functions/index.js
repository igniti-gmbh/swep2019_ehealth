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
    return db.collection('users').doc(user.uid).delete()
        .catch(err => {
            console.log('User konnte nicht entfernt werden :' + err)
        })
});


// Reset der daily steps
exports.scheduledStepsReset = functions.pubsub.schedule('0 0 * * *').onRun((context) => {
    let usersRef = db.collection('users');
    return usersRef.get()
        .then(snapshot => {
            snapshot.forEach(doc => {
                return doc.ref.set({steps_today_total: 0}, {merge: true});
            });
            return true;
        })
        .catch(err => {
            console.log('Dokumente konnten nicht erreicht werden', err)
        })

});

/*
exports.moveTemperature = functions.firestore
    .document('devices/{deviceId}/temperature/{docId}')
    .onCreate((snap, context) => {

        //RoomId holen

        const deviceId = context.params.deviceId;
        const room = db.doc('devices/' + deviceId).get()
            .then(documentSnapshot => {
                return documentSnapshot.get('roomId');
            });

        // Checkt on roomId existiert
        if (!room.exists) {
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

        // TODO Wie speichern wir die Daten in den Räumen ab?
        // Referenz wohin Wert geschrieben werden soll
        let docRef = db.doc('rooms/' + room + '/' + year + '/' + month + '/' + day + '/' + hours);

    });
*/

/* Aggregiert Daten */
exports.moveSteps = functions.firestore
    .document('/devices/{deviceId}/steps/{docId}')
    .onCreate(async (snap, context) => {

        // User ID, die zum device gehört holen.
        const deviceId = context.params.deviceId;
        let devicesSnap = await db.doc('devices/' + deviceId).get();

        let user = await devicesSnap.get('userId');

        // Wenn kein User existiert, dann Abbruch
        if (!user) {
            console.log('User existiert nicht.');
            return null;
        }


        // Erstelltes Document kopieren
        const newDoc = await snap.data();

        // In Javascript Timestamp verwandeln
        let timestamp;
        timestamp = await newDoc.timestamp;
        timestamp = timestamp.toDate();


        // In Variablen teilen
        const day = timestamp.getDate();
        const month = timestamp.getMonth() + 1;
        const year = timestamp.getFullYear();
        const hours = timestamp.getHours();

        // Value of new entry
        const additionalSteps = await newDoc.value;

        // Dokumentiert für den Tag die totalen Schritte
        let docRefTotal = db.doc('users/' + user + '/' + year + '/' + month + '/' + day + '/totalSteps');
        await documentTheDay(docRefTotal, additionalSteps);

        // TODO Das hier einstellen
        // Schritte zu Tagesergebnis hinzufügen
        await addToToday(timestamp, user, additionalSteps);

        // Referenz wohin Wert geschrieben werden soll
        let docRef = db.doc('users/' + user + '/' + year + '/' + month + '/' + day + '/' + hours);
        return documentTheHour(snap, docRef, additionalSteps);

    });

// Legacy: Vergleicht ob Snapshot von heute ist und fuegt es den daily steps hinzu
async function addToToday(docTimestamp, userId, value) {

    let currentDate = new Date();
    docTimestamp.setHours(0, 0, 0, 0);
    currentDate.setHours(0, 0, 0, 0);

    let userRef = db.doc('users/' + userId);
    let userDoc = await userRef.get();
    let steps = await userDoc.data().steps_today_total;

    if (docTimestamp.getTime() === currentDate.getTime()) {
        return userRef.set({steps_today_total: steps + value}, {merge: true});
    } else {
        return false;
    }
}

// Addiert sich zur totalen summe zusammen
async function documentTheDay(userRef, value) {

    let steps = 0;

    let userDoc = await userRef.get();

    if (!userDoc) {

        let doc_value = await userDoc.data().value;

        if (!doc_value) {
            steps = doc_value;
        }
    }

    return userRef.set({value: steps + value}, {merge: true});

}

// Fuegt Schritte zu einzelnen Stunden
async function documentTheHour(snap, docRef, additionalSteps) {
    // Schritte zu User verschieben
    // Aktuellen Wert abgreifen
    let currentSteps = await docRef.get().then((docSnap) => {
        return docSnap.get('value');
    });

    if (!currentSteps) {
        currentSteps = 0;
    }

    // Löscht das aktuelle Dokument aus dem devices Bereich
    return snap.ref.delete().then(() => {
        return docRef.set({value: currentSteps + additionalSteps}, {merge: true});
    });

}


