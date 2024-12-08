package com.knockoutgym.boxingapp1.presentation.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.firestore.FirebaseFirestore
import com.knockoutgym.boxingapp1.presentation.model.Reserva
import kotlinx.coroutines.tasks.await

// Función para eliminar una reserva
suspend fun eliminarReserva(
    reserva: Reserva,
    reservasList: SnapshotStateList<Reserva>,
    context: Context,
    userId: String
) {
    try {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        // Eliminar la reserva de la colección "reservas"
        val reservaRef = db.collection("reservas").document(reserva.reservaId)
        batch.delete(reservaRef)

        // Actualizar la colección "clases"
        val claseRef = db.collection("clases").document(reserva.claseId)
        val claseSnapshot = claseRef.get().await()
        val personasReservadasPorHorario = (claseSnapshot.get("personasReservadasPorHorario") as? Map<String, Long>)?.toMutableMap()
            ?: mutableMapOf()

        val key = "${reserva.fecha}-${reserva.horario}"
        val reservasActuales = personasReservadasPorHorario[key]?.toInt() ?: 0

        if (reservasActuales > 0) {
            personasReservadasPorHorario[key] = (reservasActuales - 1).toLong()
            batch.update(claseRef, "personasReservadasPorHorario", personasReservadasPorHorario)
        }

        // Ejecutar el batch
        batch.commit().await()

        // Ahora eliminar el documento correspondiente en 'scheduled_notifications' que es con el que se realizan las notificaciones push
        val notificationsRef = db.collection("scheduled_notifications")
        val notificationsQuery = notificationsRef
            .whereEqualTo("userId", userId)
            .whereEqualTo("claseId", reserva.claseId)
            .whereEqualTo("fecha", reserva.fecha)
            .whereEqualTo("horario", reserva.horario)

        val notificationsSnapshot = notificationsQuery.get().await()
        for (doc in notificationsSnapshot.documents) {
            doc.reference.delete().await()
        }
        // Borrar la reserva de la lista y actualizar
        reservasList.remove(reserva)

        Toast.makeText(context, "Reserva eliminada correctamente", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al eliminar reserva: ${e.message}", Toast.LENGTH_LONG).show()
        Log.e("MisReservasScreen", "Error al eliminar reserva", e)
    }
}