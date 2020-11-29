"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const functions = require("firebase-functions");
const admin = require('firebase-admin');
admin.initializeApp();
exports.createAccountDocument = functions.auth.user().onCreate((user) => {
    // get user data from the auth trigger
    const userUid = user.uid; // The UID of the user.
    const email = user.email; // The email of the user.
    let displayName = user.displayName; // The display name of the user.
    if (displayName === null) {
        // @ts-ignore
        displayName = email.substring(0, email.lastIndexOf("@"));
    }
    // set account  doc
    const account = {
        userUid: userUid,
        email: email,
        displayName: displayName,
        numberOfWBR: 0
    };
    // write new doc to collection
    return admin.firestore().collection('users').doc(userUid).set(account);
});
exports.createReport = functions.firestore
    .document('pendingReports/{reportId}')
    .onCreate((snap, context) => {
    // set account  doc
    // const geoPoint:GeoPoint = snap.data().locationGeoPoint
    // const geohash = geo.point(geoPoint.latitude,geoPoint.longitude)
    const report = {
        ID: snap.data().id,
        creatorID: snap.data().creatorID,
        wildBoar: snap.data().wildBoar,
        dead: snap.data().dead,
        locationGeoPoint: snap.data().locationGeoPoint,
        region: snap.data().region,
        subregion: snap.data().subregion,
        borough: snap.data().borough,
        description: snap.data().description,
        timestamp: snap.data().timestamp,
        photoID: snap.data().photoID,
    };
    // const center = geohash;
    // const radius = 0.01; //10metrów
    // const field = 'geohash';
    // const x = 1
    // const date = new Date(Date.now() - x * 24 * 60 * 60 * 1000)  // x days ago
    // const firestoreRef = firestore().collection('reports').where('createdAt', '>', date);
    // const query = geo.query(firestoreRef).within(center, radius, field);
    const uid = snap.data().creatorID;
    const docID = snap.data().id;
    // query.subscribe(
    //     console.log
    // );
    let total = 0;
    for (let i = 0; i < snap.data().wildBoar.length; i++) {
        const tmp = snap.data().wildBoar[i];
        total = total + tmp;
    }
    console.log('total: ' + total);
    // write new doc to collection
    return admin.firestore().collection('users').doc(uid)
        .update({ numberOfWBR: admin.firestore.FieldValue.increment(total) })
        .then(admin.firestore().doc(`/reports/${docID}`).set(report)
        .then(snap.ref.delete())
        .then(admin.firestore().collection("userTokens").doc(uid).get()
        .then((docUserTokens) => {
        var _a;
        if (docUserTokens.exists) {
            console.log('Uid: ' + ((_a = docUserTokens.data()) === null || _a === void 0 ? void 0 : _a.token));
            admin.firestore().collection('users').doc(uid).get()
                .then((docUser) => {
                var _a, _b;
                admin.messaging().sendToDevice((_a = docUserTokens.data()) === null || _a === void 0 ? void 0 : _a.token, {
                    notification: {
                        title: `Dziękujemy za zgłoszenie.`,
                        body: "Twoja dotychczasowa liczba zgłoszonych dzików to " + ((_b = docUser.data()) === null || _b === void 0 ? void 0 : _b.numberOfWBR)
                    }
                });
            });
        }
    })));
    // perform desired operations ...
});
exports.deleteReport = functions.firestore
    .document('pendingDelete/{reportId}')
    .onCreate((snap, context) => {
    //const uid = snap.data().userID
    const rapportID = snap.data().raportID;
    const uid = snap.data().userID;
    const ref = admin.firestore().collection('reports').doc(rapportID);
    return ref.get().then((reportDoc) => {
        if (reportDoc.exists) {
            console.log(reportDoc.get("creatorID"));
            if (reportDoc.get("creatorID") === uid) {
                console.log("true");
                ref.delete();
            }
            else {
                console.log("false");
            }
        }
    }).then(snap.ref.delete());
    // perform desired operations ...
});
// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
// export const helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
//# sourceMappingURL=index.js.map