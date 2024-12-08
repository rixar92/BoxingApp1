package com.knockoutgym.boxingapp1.presentation.model

import java.util.Date

// Modelo de datos actualizado para Reserva
data class Reserva(
    val claseId: String = "",
    val claseNombre: String = "",
    val reservaId: String = "",
    val fecha: String = "",
    val horario: String = "",
    val timestampReserva: Date? = null,
    val userId: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = ""
)

