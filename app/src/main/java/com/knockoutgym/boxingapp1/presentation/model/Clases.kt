package com.knockoutgym.boxingapp1.presentation.model

data class Clases(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val maximoPersonas: Int = 10, // Número máximo de personas permitido
    val horariosPorDia: Map<String, List<String>> = mapOf(), // Días y sus horarios
    val personasReservadasPorHorario: MutableMap<String, Int> = mutableMapOf() // Número de personas reservadas por horario
)


