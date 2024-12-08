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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModificarClaseScreen(
    claseId: String,
    onClaseModified: () -> Unit
) {
    var nombreClase by remember { mutableStateOf("") }
    var descripcionClase by remember { mutableStateOf("") }
    var maximoPersonas by remember { mutableStateOf("") }

    var rangoFechaInicio by remember { mutableStateOf("") }
    var rangoFechaFin by remember { mutableStateOf("") }
    var horariosEnRango by remember { mutableStateOf(listOf<String>()) }
    val horariosPorDia = remember { mutableStateMapOf<String, MutableList<String>>() }

    // Estados para edición de los campos
    var diaEditando by remember { mutableStateOf<String?>(null) }
    var diaTemporalEditando by remember { mutableStateOf("") }
    var horarioEditando by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var horarioTemporalEditando by remember { mutableStateOf("") }

    // Estados para filtrado
    var filtroFechaInicio by remember { mutableStateOf("") }
    var filtroFechaFin by remember { mutableStateOf("") }
    val diasExpandido = remember { mutableStateMapOf<String, Boolean>() }

    // Estados para manejar errores en los campos de filtro
    var errorFechaInicio by remember { mutableStateOf(false) }
    var errorFechaFin by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Cargar los datos de la clase desde Firestore
    LaunchedEffect(claseId) {
        db.collection("clases").document(claseId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    nombreClase = document.getString("nombre") ?: ""
                    descripcionClase = document.getString("descripcion") ?: ""
                    maximoPersonas = document.getLong("maximoPersonas")?.toString() ?: "10"
                    val horariosFirestore = document.get("horariosPorDia") as? Map<String, List<String>>
                    horariosFirestore?.forEach { (dia, horarios) ->
                        horariosPorDia[dia] = horarios.toMutableList()
                    }
                }
            }
    }


    // Función para obtener los horarios filtrados
    fun getHorariosFiltrados(): Map<String, MutableList<String>> {
        val fechaInicioFiltro = parseDateOrNull(filtroFechaInicio)
        val fechaFinFiltro = parseDateOrNull(filtroFechaFin)

        return horariosPorDia.filter { (dia, _) ->
            val fechaDia = parseDateOrNull(dia)
            if (fechaDia == null) {
                false // Excluimos días con formato incorrecto
            } else {
                (fechaInicioFiltro == null || !fechaDia.isBefore(fechaInicioFiltro)) &&
                        (fechaFinFiltro == null || !fechaDia.isAfter(fechaFinFiltro))
            }
        }
    }

    val horariosFiltrados = getHorariosFiltrados()

    Scaffold(
        bottomBar = {
            // Botón para modificar la clase
            Button(
                onClick = {
                    if (nombreClase.isNotBlank() && horariosPorDia.isNotEmpty() && maximoPersonas.isNotBlank()) {
                        val maxPersonas = maximoPersonas.toIntOrNull() ?: 10
                        val claseActualizada = mapOf(
                            "nombre" to nombreClase,
                            "descripcion" to descripcionClase,
                            "horariosPorDia" to horariosPorDia,
                            "maximoPersonas" to maxPersonas
                        )

                        db.collection("clases").document(claseId)
                            .update(claseActualizada)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Clase modificada exitosamente",
                                    Toast.LENGTH_LONG
                                ).show()
                                onClaseModified()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error al modificar la clase: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Completa todos los campos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Modificar Clase")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Contenido principal con scroll
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Modificar Clase",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Campos de entrada para la clase
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

                    // Rango de fechas
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
                        onValueChange = { input -> horariosEnRango = input.split(",").map { it.trim() } },
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
                                        val horariosList = horariosPorDia.getOrPut(diaStr) { mutableListOf() }
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
                            // Los horarios se filtrarán automáticamente al actualizar los valores de filtro
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text("Aplicar Filtros")
                    }
                }

                // Listado de horarios por día con filtro
                horariosFiltrados.forEach { (dia, horarios) ->
                    val isExpanded = diasExpandido[dia] ?: true

                    item {
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
                                horarios.forEachIndexed { index, horario ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (horarioEditando?.first == dia && horarioEditando?.second == index) {
                                            OutlinedTextField(
                                                value = horarioTemporalEditando,
                                                onValueChange = { horarioTemporalEditando = it },
                                                label = { Text("Modificar Horario") },
                                                modifier = Modifier.fillMaxWidth(0.7f)
                                            )
                                            Row {
                                                IconButton(onClick = {
                                                    if (isValidTime(horarioTemporalEditando)) {
                                                        horarios[index] = horarioTemporalEditando
                                                        horarioEditando = null
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Introduce un horario válido",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Check, contentDescription = "Guardar horario")
                                                }
                                                IconButton(onClick = { horarioEditando = null }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Cancelar edición")
                                                }
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
                                                    Icon(Icons.Default.Edit, contentDescription = "Editar horario")
                                                }
                                                IconButton(onClick = {
                                                    horarios.removeAt(index)
                                                    if (horarios.isEmpty()) {
                                                        horariosPorDia.remove(dia)
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar horario")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}









