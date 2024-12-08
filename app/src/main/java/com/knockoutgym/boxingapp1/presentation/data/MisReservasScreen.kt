@file:Suppress("DEPRECATION")

package com.knockoutgym.boxingapp1.presentation.data

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.knockoutgym.boxingapp1.presentation.model.Reserva
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReservasScreen(
    userId: String,
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val reservasList = remember { mutableStateListOf<Reserva>() }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Obtener las reservas del usuario desde la colección "reservas"
    LaunchedEffect(userId) {
        try {
            val reservasSnapshot = db.collection("reservas")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            reservasList.clear()
            reservasSnapshot.documents.forEach { reservaDoc ->
                val reserva = Reserva(
                    claseId = reservaDoc.getString("claseId") ?: "",
                    claseNombre = reservaDoc.getString("claseNombre") ?: "Nombre Desconocido",
                    reservaId = reservaDoc.id,
                    fecha = reservaDoc.getString("fecha") ?: "",
                    horario = reservaDoc.getString("horario") ?: "",
                    timestampReserva = reservaDoc.getTimestamp("timestampReserva")?.toDate()

                )
                reservasList.add(reserva)
            }

            // Parsear fechas y horas y aceptar horas de uno o dos digitos
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm", Locale.getDefault())

            // Ordenar las reservas por fecha de la clase
            reservasList.sortBy { reserva ->
                LocalDateTime.parse("${reserva.fecha} ${reserva.horario}", dateTimeFormatter)
            }

            // Obtener la fecha y hora actuales
            val currentDateTime = LocalDateTime.now()

            // Calcular la fecha y hora dentro de 5 días
            val dateInFiveDays = currentDateTime.plusDays(5)

            // Filtrar las reservas para mostrar solo desde hoy hasta 5 días más
            reservasList.retainAll { reserva ->
                val reservaDateTime = LocalDateTime.parse("${reserva.fecha} ${reserva.horario}", dateTimeFormatter)
                // Comprobar que la fecha de la reserva no es antes de hoy y no es después de dentro de 5 días
                !reservaDateTime.isBefore(currentDateTime.toLocalDate().atStartOfDay()) &&
                        !reservaDateTime.isAfter(dateInFiveDays.toLocalDate().atTime(LocalTime.MAX))
            }

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            Toast.makeText(context, "Error al cargar reservas: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("MisReservasScreen", "Error al cargar reservas", e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reservas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reservasList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No tienes reservas activas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(reservasList) { reserva ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Clase: ${reserva.claseNombre}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(text = "Fecha: ${reserva.fecha}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Horario: ${reserva.horario}", style = MaterialTheme.typography.bodyMedium)
                                reserva.timestampReserva?.let {
                                    Text(
                                        text = "Reservado el: ${it.toLocaleString()}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        eliminarReserva(reserva, reservasList, context, userId)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar reserva"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}




