const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
 
exports.notifyNewMessage = functions.firestore
    .document('Responses/{phno}')
    .onWrite((docSnapshot, context) => {
        const message = docSnapshot.after.data();
        const AllTokens = message['My_Token'];
		const myDisplay= message['Response'];
 
            const payload = {
                notification: {
                    title: "carE-Share",
                    body: myDisplay
                },data: {}
            }
				return admin.messaging().sendToDevice(AllTokens, payload)
  .then(function(response) {
    console.log("Successfully sent message:", response);return;
  })
  .catch(function(error) {
    console.log("Error sending message:", error);
  });
	})
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
//firebase deploy --only functions