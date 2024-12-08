/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */


const {onSchedule} = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendScheduledNotifications = onSchedule(
    "every 15 minutes", async (event) => {
      const db = admin.firestore();
      const now = admin.firestore.Timestamp.now();

      const notificationsRef = db.collection("scheduled_notifications");
      const snapshot = await notificationsRef.where("notificationTime", "<="
          , now).get();

      if (snapshot.empty) {
        console.log("No hay notificaciones para enviar en este momento.");
        return;
      }

      const messages = [];
      const batch = db.batch();

      snapshot.forEach((doc) => {
        const data = doc.data();
        const fcmToken = data.fcmToken;
        const claseNombre = data.claseNombre;
        const horario = data.horario;

        if (fcmToken) {
          messages.push({
            token: fcmToken,
            notification: {
              title: "Recordatorio de clase",
              body: `En 30 minutos comienza ${claseNombre} a las ${horario}`,
            },
            data: {
              claseId: data.claseId,
              fecha: data.fecha,
              horario: data.horario,
            },
          });
        }
        batch.delete(doc.ref);
      });

      if (messages.length > 0) {
        const response = await admin.messaging().sendAll(messages);
        console.log(
            `Notificaciones enviadas con éxito: ${response.successCount},
     fallidas: ${response.failureCount}`);
        await batch.commit();
      } else {
        console.log("No hay tokens FCM disponibles o mensajes a enviar.");
      }
    });


// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
