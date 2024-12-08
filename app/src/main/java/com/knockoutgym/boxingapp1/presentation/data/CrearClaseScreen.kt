package com.knockoutgym.boxingapp1.presentation.data

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CrearClaseScreen(onClaseCreated: () -> Unit) {
    var nombreClase by remember { mutableStateOf("") }
    var descripcionClase by remember { mutableStateOf("") }
    var maximoPersonas by remember { mutableStateOf("") }
    var rangoFechaInicio by remember { mutableStateOf("") }
    var rangoFechaFin by remember { mutableStateOf("") }
    var horariosEnRango by remember { mutableStateOf(listOf<String>()) }
    val horariosPorDia = remember { mutableStateMapOf<String, SnapshotStateList<String>>() }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Estados para filtrar los horarios
    var filtroFechaInicio by remember { mutableStateOf("") }
    var filtroFechaFin by remember { mutableStateOf("") }
    val diasExpandido = remember { mutableStateMapOf<String, Boolean>() }
    var errorFechaInicio by remember { mutableStateOf(false) }
    var errorFechaFin by remember { mutableStateOf(false) }
    // Función para validar que la hora esta metida como deseamos HH:mm
    fun isValidTime(time: String): Boolean {
        return try {
            val (hours, minutes) = time.split(":").map { it.toInt() }
            hours in 0..23 && minutes in 0..59
        } catch (e: Exception) {
            false
        }
    }

    // Función para validar la fecha como YYYY-MM-DD
    fun isValidDay(day: String): Boolean {
        return try {
            LocalDate.parse(day)
            true
        } catch (e: Exception) {
            false
        }
    }

    var horarioEditando by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var horarioTemporalEditando by remember { mutableStateOf("") }

    // Función para obtener los horarios filtrados
    fun getHorariosFiltrados(): Map<String, MutableList<String>> {
        val fechaInicioFiltro = parseDateOrNull(filtroFechaInicio)
        val fechaFinFiltro = parseDateOrNull(filtroFechaFin)

        return horariosPorDia.filter { (dia, _) ->
            val fechaDia = parseDateOrNull(dia)
            if (fechaDia == null) {
                false // Si el día no es una fecha válida, lo excluimos
            } else {
                (fechaInicioFiltro == null || !fechaDia.isBefore(fechaInicioFiltro)) &&
                        (fechaFinFiltro == null || !fechaDia.isAfter(fechaFinFiltro))
            }
        }
    }

    val horariosFiltrados = getHorariosFiltrados()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Crear Nueva Clase",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = nombreClase,
                onValueChange = { nombreClase = it },
                label = { Text("Nombre de la clase") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = descripcionClase,
                onValueChange = { descripcionClase = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = maximoPersonas,
                onValueChange = { maximoPersonas = it },
                label = { Text("Máximo de Personas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = rangoFechaInicio,
                onValueChange = { rangoFechaInicio = it },
                label = { Text("Fecha inicio (YYYY-MM-DD)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = rangoFechaFin,
                onValueChange = { rangoFechaFin = it },
                label = { Text("Fecha fin (YYYY-MM-DD)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = horariosEnRango.joinToString(", "),
                onValueChange = { input ->
                    horariosEnRango = input.split(",").map { it.trim() }
                },
                label = { Text("Horas (ej: 08:00, 10:00)") },
                placeholder = { Text("Separar por comas") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    if (isValidDay(rangoFechaInicio) && isValidDay(rangoFechaFin) && horariosEnRango.all(::isValidTime)) {
                        val fechaInicio = LocalDate.parse(rangoFechaInicio)
                        val fechaFin = LocalDate.parse(rangoFechaFin)
                        if (fechaInicio.isAfter(fechaFin)) {
                            Toast.makeText(
                                context,
                                "Fecha de inicio no puede ser después de la fecha de fin",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            var currentDate = fechaInicio
                            while (!currentDate.isAfter(fechaFin)) {
                                val diaStr = currentDate.toString()
                                val horariosList = horariosPorDia.getOrPut(diaStr) { mutableStateListOf() }
                                horariosList.addAll(horariosEnRango)
                                currentDate = currentDate.plusDays(1)
                            }
                            rangoFechaInicio = ""
                            rangoFechaFin = ""
                            horariosEnRango = emptyList()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Introduce fechas y horas válidas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Añadir Horarios en Rango")
            }


            // Campos para filtrar horarios
            OutlinedTextField(
                value = filtroFechaInicio,
                onValueChange = {
                    filtroFechaInicio = it
                    errorFechaInicio = parseDateOrNull(it) == null && it.isNotBlank()
                },
                label = { Text("Filtrar desde (YYYY-MM-DD)") },
                isError = errorFechaInicio,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            if (errorFechaInicio) {
                Text(
                    text = "Fecha no válida",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = filtroFechaFin,
                onValueChange = {
                    filtroFechaFin = it
                    errorFechaFin = parseDateOrNull(it) == null && it.isNotBlank()
                },
                label = { Text("Filtrar hasta (YYYY-MM-DD)") },
                isError = errorFechaFin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            if (errorFechaFin) {
                Text(
                    text = "Fecha no válida",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = {
                    // Al actualizar los valores de filtroFechaInicio y filtroFechaFin, los horarios se filtrarán automáticamente
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Aplicar Filtros")
            }
        }

        items(horariosFiltrados.keys.sorted()) { dia ->
            val isExpanded = diasExpandido[dia] ?: true

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { diasExpandido[dia] = !isExpanded }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Día: $dia", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
                if (isExpanded) {
                    horariosPorDia[dia]?.let { horarios ->
                        horarios.forEachIndexed { index, horario ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (horarioEditando == dia to index) {
                                    OutlinedTextField(
                                        value = horarioTemporalEditando,
                                        onValueChange = { horarioTemporalEditando = it },
                                        label = { Text("Modificar Horario") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        if (isValidTime(horarioTemporalEditando)) {
                                            horarios[index] = horarioTemporalEditando
                                            horarioEditando = null
                                            horarioTemporalEditando = ""
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Introduce una hora válida",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = "Guardar horario")
                                    }
                                    IconButton(onClick = {
                                        horarioEditando = null
                                        horarioTemporalEditando = ""
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Cancelar edición"
                                        )
                                    }
                                } else {
                                    Text(
                                        text = horario,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row {
                                        IconButton(onClick = {
                                            horarioEditando = dia to index
                                            horarioTemporalEditando = horario
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar horario"
                                            )
                                        }
                                        IconButton(onClick = {
                                            horarios.removeAt(index)
                                            if (horarios.isEmpty()) {
                                                horariosPorDia.remove(dia)
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar horario"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombreClase.isNotBlank() && horariosPorDia.isNotEmpty() && maximoPersonas.isNotBlank()) {
                        val maxPersonas = maximoPersonas.toIntOrNull() ?: 10
                        val nuevaClase = hashMapOf(
                            "nombre" to nombreClase,
                            "descripcion" to descripcionClase,
                            "horariosPorDia" to horariosPorDia.mapValues { it.value.toList() },
                            "maximoPersonas" to maxPersonas
                        )

                        db.collection("clases")
                            .add(nuevaClase)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Clase creada exitosamente",
                                    Toast.LENGTH_LONG
                                ).show()
                                onClaseCreated()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error al crear la clase: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Completa todos los campos y añade al menos un día con un horario",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear Clase")
            }
        }
    }
}







