const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

let db = admin.firestore();
let realtime = admin.database();



/* User wird in Firebase eingefügt */
exports.addUserToDB = functions.auth.user().onCreate((user) => {

    let data = {
        email: user.email,
        room: null,
        position: null,
        stepgoal: 10000,
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


exports.moveArduinoData = functions.database.ref('/{deviceId}/{document}')
    .onCreate(async (snap, context)=> {

    const deviceId = context.params.deviceId;

    // Room ziehen
    let roomRef = await realtime.ref('devices/'+deviceId).child('room');
    let room = await roomRef.val();

    // Wenn kein room existiert, dann Abbruch

    if(!room){
        console.log('Raum existiert nicht');
        return null
    }

    // Werte ziehen aus Realtime Database
    const timestamp = snap.child("time").val();
    const gas = snap.child("gas").val();
    const humidity = snap.child("humidity").val();
    const pressure = snap.child("pressure").val();
    const temperature = snap.child("temperature").val();

    const time_array = await splitTimestamp(timestamp);
    // Referenz wohin Wert geschrieben werden soll
    let docRef = db.doc('rooms/' + room + '/' + time_array[2] + '/' + time_array[1] + '/'
            + time_array[0] + '/' + time_array[3]);

    //Batch Objekt erstellen
    let batch = db.batch();

    // Vergleichen ob aktueller Wert in Übersicht geschrieben werden soll
    const currentTimestamp = await docRef.get().then(doc => { return doc.get('timestamp')});

    if (!currentTimestamp || currentTimestamp < timestamp){
    batch.set(docRef, {
        'gas': gas,
    'humidity': humidity,
    'pressure': pressure,
    'temperature': temperature,
    'timestamp': timestamp})
    }

    // Batch schreiben und commiten
    batch.add(docRef.collection('gas'), {'timestamp':timestamp,
    'gas': gas});
    batch.add(docRef.collection('humidity'), {'timestamp':timestamp,
    'humidity': humidity});
    batch.add(docRef.collection('pressure'), {'timestamp':timestamp,
    'pressure': pressure});
    batch.add(docRef.collection('temperature'), {'timestamp':timestamp,
    'temperature': temperature});

    await batch.commit();

    // Alten Wert in der Datenbank löschen und Funktion beenden
    return snap.ref.remove();

    });


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
        let timestamp = await newDoc.timestamp;
        const time_array = await splitTimestamp(timestamp);
        // Referenz wohin Wert geschrieben werden soll
        let docRef = db.doc('users/' + user + '/' + time_array[2] + '/' + time_array[1] + '/'
            + time_array[0] + '/' + time_array[3]);

        // Wert des neuen Push
        const additionalSteps = await newDoc.value;

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

    timestamp = timestamp.toDate();


    const day = timestamp.getDate();
    const month = timestamp.getMonth() + 1;
    const year = timestamp.getFullYear();
    const hours = timestamp.getHours();

    return [day, month, year, hours];
}




