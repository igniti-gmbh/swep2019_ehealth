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
        daily_step_goal: 10000,
        age: null,
        name: user.displayName,
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

/* Gepushte Schritte werden unter User aggregiert */
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

        let time_array = splitTimestamp(timestamp);
        /* return array mit gesplitteten Timestamp
        day = 0, month = 1, year = 2, hours = 3*/

        // Wert des neuen Push
        const additionalSteps = await newDoc.value;

        // Referenz wohin Wert geschrieben werden soll
        let docRef = await db.doc('users/' + user + '/' + time_array[2] + '/' + time_array[1] + '/'
            + time_array[0] + '/' + time_array[3]);

        await documentSteps(docRef, additionalSteps);

        return await deleteOriginal(snap)

    });




// Addiert sich zur totalen Summe zusammen
function documentSteps(docRef, value) {

    return db.runTransaction(transaction => {
        return transaction.get(docRef).then(doc => {
            if(!doc.exists) {
                return transaction.create(docRef, {value:value})
            } else {
                let newSteps = doc.data().value + value;
                return transaction.update(docRef, {value:newSteps});
            }

        })
    });
}

// Fuegt Schritte zu einzelnen Stunden
async function deleteOriginal(snap) {

    // Löscht das aktuelle Dokument aus dem devices Bereich
    return await snap.ref.delete();
}


// Timestamp in Variabel aufteilen
function splitTimestamp(timestamp) {

    const day = timestamp.getDate();
    const month = timestamp.getMonth() + 1;
    const year = timestamp.getFullYear();
    const hours = timestamp.getHours();

    return [day, month, year, hours];
}

