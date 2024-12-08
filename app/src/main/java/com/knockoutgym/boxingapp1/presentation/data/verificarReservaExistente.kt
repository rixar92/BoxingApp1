package com.knockoutgym.boxingapp1.presentation.data

import com.google.firebase.firestore.FirebaseFirestore

fun verificarReservaExistente(
    userId: String,
    claseId: String,
    fecha: String,
    horario: String,
    onResult: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("reservas")
        .whereEqualTo("usuarioId", userId)
        .whereEqualTo("claseId", claseId)
        .whereEqualTo("fecha", fecha)
        .whereEqualTo("horario", horario)
        .get()
        .addOnSuccessListener { result ->
            // Si encontramos algún documento, significa que ya tiene una reserva
            onResult(result.isEmpty.not())
        }
        .addOnFailureListener {
            // Manejar el error
            onResult(false)
        }
}