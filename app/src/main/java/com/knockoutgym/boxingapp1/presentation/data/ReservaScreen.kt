package com.knockoutgym.boxingapp1.presentation.data

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.knockoutgym.boxingapp1.presentation.model.Reserva
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservaScreen(
    claseId: String,
    claseNombre: String,
    horarios: Map<String, List<String>>,
    onReservar: suspend (String, String) -> Result<String>
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var isAdmin by remember { mutableStateOf(false) }
    var maximoPersonas by remember { mutableStateOf<Int?>(null) }

    val inscritosList = remember { mutableStateListOf<Reserva>() }
    var isLoadingInscritos by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(true) } // Indica si se están cargando datos (maximoPersonas y reservas)
    val (personasReservadasMap, setPersonasReservadasMap) = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var diaSeleccionado by remember { mutableStateOf<String?>(null) }
    var horarioSeleccionado by remember { mutableStateOf<String?>(null) }

    var shouldReload by remember { mutableStateOf(false) } // Estado para forzar recarga tras reservar

    val defaultButtonColor = MaterialTheme.colorScheme.secondary
    val selectedButtonColor = MaterialTheme.colorScheme.primary

    // Fecha actual y rango
    val fechaActual = LocalDate.now()
    val maxFecha = fechaActual.plusDays(5)

    // Filtrar días
    val diasFiltrados = horarios.keys.filter { diaString ->
        try {
            val dia = LocalDate.parse(diaString)
            dia.isAfter(fechaActual.minusDays(1)) && dia.isBefore(maxFecha.plusDays(1))
        } catch (e: Exception) {
            false
        }
    }.sortedBy { LocalDate.parse(it) }

    // Cargar rol del usuario
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val userDoc = db.collection("usuarios").document(userId).get().await()
                val role = userDoc.getString("role") ?: ""
                isAdmin = role == "admin"
            } catch (e: Exception) {
                Toast.makeText(context, "Error al obtener el rol: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ReservaScreen", "Error rol usuario", e)
            }
        }
    }

    // Cargar maximoPersonas desde Firestore
    LaunchedEffect(claseId) {
        isLoadingData = true
        try {
            val claseDoc = db.collection("clases").document(claseId).get().await()
            maximoPersonas = claseDoc.getLong("maximoPersonas")?.toInt() ?: 10
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar maximoPersonas: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("ReservaScreen", "Error cargar maximoPersonas", e)
            maximoPersonas = 10
        }
    }

    // Una sola consulta para todas las reservas en el rango de días
    // Si maximoPersonas y horarios ya se cargaron, y si shouldReload cambia, recargamos datos
    LaunchedEffect(horarios, maximoPersonas, shouldReload) {
        if (maximoPersonas != null) {
            isLoadingData = true
            coroutineScope.launch {
                try {
                    val fechaInicioStr = fechaActual.toString()
                    val fechaFinStr = maxFecha.toString()

                    val reservasSnap = db.collection("reservas")
                        .whereEqualTo("claseId", claseId)
                        .whereGreaterThanOrEqualTo("fecha", fechaInicioStr)
                        .whereLessThanOrEqualTo("fecha", fechaFinStr)
                        .get()
                        .await()

                    val newMap = mutableMapOf<String, Int>()

                    // Inicializamos todas las combinaciones dia-horario a 0
                    for ((dia, horas) in horarios) {
                        for (hora in horas) {
                            newMap["$dia-$hora"] = 0
                        }
                    }

                    // Contar localmente
                    for (document in reservasSnap.documents) {
                        val diaRes = document.getString("fecha") ?: continue
                        val horaRes = document.getString("horario") ?: continue
                        val key = "$diaRes-$horaRes"
                        if (newMap.containsKey(key)) {
                            newMap[key] = newMap[key]!! + 1
                        }
                    }

                    setPersonasReservadasMap(newMap)
                    isLoadingData = false
                } catch (e: Exception) {
                    isLoadingData = false
                    Toast.makeText(context, "Error al cargar reservas: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("ReservaScreen", "Error cargar reservas", e)
                }
            }
        }
    }

    // Mostrar indicador de carga si aún no tenemos maximoPersonas o estamos cargando datos
    if (maximoPersonas == null || isLoadingData) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Selecciona un día para $claseNombre",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lista días
            Column {
                diasFiltrados.forEach { dia ->
                    val isSelected = diaSeleccionado == dia
                    val backgroundColor = if (isSelected) selectedButtonColor else defaultButtonColor
                    val letterColor = if (isSelected) Color.White else Color.Black

                    val horariosDisponibles = horarios[dia]?.filter { horario ->
                        val reservas = personasReservadasMap["$dia-$horario"] ?: 0
                        reservas < maximoPersonas!! || isAdmin
                    }

                    if (!horariosDisponibles.isNullOrEmpty()) {
                        Button(
                            onClick = {
                                diaSeleccionado = if (isSelected) null else dia
                                horarioSeleccionado = null
                                inscritosList.clear()
                            },
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = backgroundColor,
                                contentColor = letterColor
                            )
                        ) {
                            Text(text = dia)
                        }
                    }
                }
            }

            diaSeleccionado?.let { dia ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Selecciona un horario para $dia",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val horariosDisponibles = horarios[dia]?.filter { horario ->
                    val reservas = personasReservadasMap["$dia-$horario"] ?: 0
                    reservas < maximoPersonas!! || isAdmin
                }

                Column {
                    horariosDisponibles?.forEach { horario ->
                        val isSelected = horarioSeleccionado == horario
                        val backgroundColor = if (isSelected) selectedButtonColor else defaultButtonColor
                        val letterColor = if (isSelected) Color.White else Color.Black
                        Button(
                            onClick = {
                                horarioSeleccionado = if (isSelected) null else horario

                                if (isAdmin && horarioSeleccionado != null) {
                                    // Cargar inscritos
                                    isLoadingInscritos = true
                                    inscritosList.clear()
                                    coroutineScope.launch {
                                        try {
                                            val reservasSnapshot = db.collection("reservas")
                                                .whereEqualTo("claseId", claseId)
                                                .whereEqualTo("fecha", diaSeleccionado)
                                                .whereEqualTo("horario", horarioSeleccionado)
                                                .get()
                                                .await()

                                            val newList = reservasSnapshot.documents.map { reservaDoc ->
                                                val userIdInscrito = reservaDoc.getString("userId") ?: ""
                                                val userDoc = db.collection("usuarios").document(userIdInscrito).get().await()
                                                val nombre = userDoc.getString("nombre") ?: "Nombre desconocido"
                                                val apellidos = userDoc.getString("apellidos") ?: "Apellidos desconocidos"
                                                val dni = userDoc.getString("dni") ?: "DNI desconocido"

                                                Reserva(
                                                    claseId = reservaDoc.getString("claseId") ?: "",
                                                    claseNombre = reservaDoc.getString("claseNombre") ?: "Nombre Desconocido",
                                                    reservaId = reservaDoc.id,
                                                    fecha = reservaDoc.getString("fecha") ?: "",
                                                    horario = reservaDoc.getString("horario") ?: "",
                                                    timestampReserva = reservaDoc.getTimestamp("timestampReserva")?.toDate(),
                                                    userId = userIdInscrito,
                                                    nombre = nombre,
                                                    apellidos = apellidos,
                                                    dni = dni
                                                )
                                            }
                                            inscritosList.clear()
                                            inscritosList.addAll(newList)
                                            isLoadingInscritos = false
                                        } catch (e: Exception) {
                                            isLoadingInscritos = false
                                            Toast.makeText(context, "Error al cargar inscritos: ${e.message}", Toast.LENGTH_LONG).show()
                                            Log.e("ReservaScreen", "Error al cargar inscritos", e)
                                        }
                                    }
                                } else {
                                    inscritosList.clear()
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = backgroundColor,
                                contentColor = letterColor
                            )
                        ) {
                            Text(text = horario)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar plazas disponibles
            if (!isAdmin && diaSeleccionado != null && horarioSeleccionado != null) {
                val key = "$diaSeleccionado-$horarioSeleccionado"
                val reservasActuales = personasReservadasMap[key] ?: 0
                val plazasDisponibles = maximoPersonas!! - reservasActuales
                Text(
                    text = "Plazas disponibles: $plazasDisponibles / $maximoPersonas",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Botón reservar (recarga tras reservar)
            if (!isAdmin) {
                Button(
                    onClick = {
                        if (diaSeleccionado != null && horarioSeleccionado != null) {
                            if (userId != null) {
                                coroutineScope.launch {
                                    val resultado = onReservar(diaSeleccionado!!, horarioSeleccionado!!)
                                    resultado.fold(
                                        onSuccess = {
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                            // Forzar recarga
                                            shouldReload = !shouldReload
                                        },
                                        onFailure = { e ->
                                            Toast.makeText(context, e.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            } else {
                                Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Por favor selecciona un día y un horario", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = diaSeleccionado != null && horarioSeleccionado != null
                ) {
                    Text(text = "Reservar")
                }
            }

            // Lista de inscritos (solo admin)
            if (isAdmin && horarioSeleccionado != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Personas inscritas:",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoadingInscritos) {
                    CircularProgressIndicator()
                } else if (inscritosList.isEmpty()) {
                    Text(text = "No hay personas inscritas en este horario.")
                } else {
                    Column {
                        inscritosList.forEach { reserva ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Nombre: ${reserva.nombre} ${reserva.apellidos}")
                                    Text(text = "DNI: ${reserva.dni}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}











