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
        room: 450,
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
    .onCreate(async (snap, context) => {

        const deviceId = context.params.deviceId;

        // Room ziehen

        let roomRef = realtime.ref('/' + deviceId);
        let room;
        await roomRef.once('value', (snap) => {
            room = snap.child('roomID').val();
        });

        // Wenn kein room existiert, dann Abbruch

        if (!room || typeof room !== 'number') {
            console.log('Raum existiert nicht');
            return null
        }


        // Werte ziehen aus Realtime Database
        const time = snap.child("time").val();
        let timestamp = new Date(time * 1000);
        const gas = parseFloat(snap.child("gas").val());
        const humidity = parseFloat(snap.child("humidity").val());
        const pressure = parseFloat(snap.child("pressure").val());
        const temperature = parseFloat(snap.child("temperature").val());


        // Anpassen, damit auf dem Google Server das timestamp korrekt gelesen wird
        let offsetChange = 60 - timestamp.getTimezoneOffset();
        let newTimestamp = new Date(timestamp.getTime());
        await newTimestamp.setMinutes(timestamp.getMinutes() + offsetChange);
        const time_array = await splitTimestamp(newTimestamp);

        // Referenz wohin Wert geschrieben werden soll
        let docRef = await db.doc('rooms/' + room + '/' + time_array[2] + '/' + time_array[1] + '/'
            + time_array[0] + '/' + time_array[3]);

        //Batch Objekt erstellen
        let batch = await db.batch();

        // Fuegt Durchschnittaswert in das Dokument ein
        const currentDocSnapshot = await docRef.get();

        if (!currentDocSnapshot.exists) {
            batch.set(docRef, {
                'gasCurrent': gas,
                'humidityCurrent': humidity,
                'pressureCurrent': pressure,
                'temperatureCurrent': temperature,
                'timestampCurrent' : timestamp,
                'gasAverage': gas,
                'humidityAverage': humidity,
                'pressureAverage': pressure,
                'temperatureAverage': temperature,
                'values': 1,
            })
        } else {

            const values = currentDocSnapshot.data();

            batch.update(docRef, {
                'gasAverage': newAverage(values['gasAverage'], gas, values['values']),
                'humidityAverage': newAverage(values['humidityAverage'], humidity, values['values']),
                'pressureAverage': newAverage(values['pressureAverage'], pressure, values['values']),
                'temperatureAverage': newAverage(values['temperatureAverage'], temperature, values['values']),
                'values': values['values'] + 1,
                'gasCurrent': gas,
                'humidityCurrent': humidity,
                'pressureCurrent': pressure,
                'temperatureCurrent': temperature,
                'timestampCurrent' : timestamp,
            })
        }




        // Batch schreiben und commiten
        batch.set(docRef.collection('gas').doc(time.toString()), {
            'timestamp': timestamp,
            'value': gas
        });
        batch.set(docRef.collection('humidity').doc(time.toString()), {
            'timestamp': timestamp,
            'value': humidity
        });
        batch.set(docRef.collection('pressure').doc(time.toString()), {
            'timestamp': timestamp,
            'value': pressure
        });
        batch.set(docRef.collection('temperature').doc(time.toString()), {
            'timestamp': timestamp,
            'value': temperature
        });

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

        // Firestore Timestamp in  Javascript Timestamp verwandeln
        let timestamp = await newDoc.timestamp;
        timestamp = timestamp.toDate();
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
            if (!doc.exists) {
                return transaction.create(docRef, {value: value})
            } else {
                let newSteps = doc.data().value + value;
                return transaction.update(docRef, {value: newSteps});
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

function newAverage (currentValue, addedValue, valueTotal){

    let newValue = currentValue * valueTotal;
    newValue = newValue + addedValue;
    newValue = newValue / (valueTotal  + 1);

    return newValue

}



