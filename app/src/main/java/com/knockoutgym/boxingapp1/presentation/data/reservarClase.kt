package com.knockoutgym.boxingapp1.presentation.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

suspend fun reservarClase(
    userId: String,
    claseId: String,
    claseNombre: String,
    fecha: String,
    horario: String
): Result<String> {
    val db = FirebaseFirestore.getInstance()

    return try {
        // Verificar si el usuario ya tiene una reserva para la misma clase, fecha y horario
        val existingReservations = db.collection("reservas")
            .whereEqualTo("userId", userId)
            .whereEqualTo("claseId", claseId)
            .whereEqualTo("fecha", fecha)
            .whereEqualTo("horario", horario)
            .get()
            .await()

        if (!existingReservations.isEmpty) {
            return Result.failure(Exception("Ya tienes una reserva para esta clase, fecha y horario"))
        }

        // Obtener la clase para verificar disponibilidad
        val claseSnapshot = db.collection("clases").document(claseId).get().await()
        val personasReservadasPorHorario = (claseSnapshot.get("personasReservadasPorHorario") as? Map<String, Long>)?.toMutableMap()
            ?: mutableMapOf()

        val maximoPersonas = claseSnapshot.getLong("maximoPersonas")?.toInt() ?: 10
        val key = "$fecha-$horario"
        val reservasActuales = personasReservadasPorHorario[key]?.toInt() ?: 0

        if (reservasActuales >= maximoPersonas) {
            return Result.failure(Exception("El horario está completo"))
        }

        // Realizar la reserva
        val batch = db.batch()
        personasReservadasPorHorario[key] = (reservasActuales + 1).toLong()
        val claseDocRef = db.collection("clases").document(claseId)
        batch.update(claseDocRef, "personasReservadasPorHorario", personasReservadasPorHorario)

        // Crear una nueva reserva en la colección "reservas"
        val nuevaReservaRef = db.collection("reservas").document()
        val nuevaReserva = hashMapOf(
            "claseId" to claseId,
            "claseNombre" to claseNombre,
            "userId" to userId,
            "fecha" to fecha,
            "horario" to horario,
            "timestampReserva" to FieldValue.serverTimestamp()
        )
        batch.set(nuevaReservaRef, nuevaReserva)

        // Ejecutar el batch
        batch.commit().await()

        // Programar la notificacion

        // Obtener el token FCM del usuario
        val userDoc = db.collection("usuarios").document(userId).get().await()
        val fcmToken = userDoc.getString("fcmToken")
            ?: return Result.failure(Exception("No se pudo obtener el token FCM del usuario."))

        // Calcular notification (30 minutos antes de la clase)
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val classDateTime = dateTimeFormat.parse("$fecha $horario")
            ?: return Result.failure(Exception("No se pudo parsear la fecha y hora de la clase."))

        val notificationTimeMillis = classDateTime.time - (30 * 60 * 1000) // 30 minutos antes
        val notificationTimeDate = Date(notificationTimeMillis)

        // Crear el documento en "scheduled_notifications"
        val notificationData = hashMapOf(
            "userId" to userId,
            "claseId" to claseId,
            "claseNombre" to claseNombre,
            "fecha" to fecha,
            "horario" to horario,
            "fcmToken" to fcmToken,
            "notificationTime" to Timestamp(notificationTimeDate)
        )

        db.collection("scheduled_notifications")
            .add(notificationData)
            .await()

        Result.success("Reserva exitosa")
    } catch (e: Exception) {
        Result.failure(e)
    }
}









