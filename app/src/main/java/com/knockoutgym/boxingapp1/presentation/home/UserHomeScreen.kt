@file:Suppress("UNCHECKED_CAST")

package com.knockoutgym.boxingapp1.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.knockoutgym.boxingapp1.presentation.model.Clases

@Composable
fun UserHomeScreen(
    onMyAccountClick: () -> Unit,
    onClassClick: (Clases) -> Unit,
    onMyReservationsClick: () -> Unit
) {
    val clasesList = remember { mutableStateListOf<Clases>() }
    val db = FirebaseFirestore.getInstance()

    // Consultamos Firestore y añadimos a la lista de clases
    LaunchedEffect(Unit) {
        db.collection("clases")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val clase = Clases(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        descripcion = document.getString("descripcion") ?: "",
                        horariosPorDia = document.get("horariosPorDia") as? Map<String, List<String>>
                            ?: mapOf()
                    )
                    clasesList.add(clase)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Botones de "Mi Cuenta" y "Mis Reservas"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón "Mi Cuenta"
            Button(
                onClick = onMyAccountClick,
                modifier = Modifier
                    .weight(1f) // Ocupa una proporción de 1
                    .padding(end = 8.dp) // Espacio a la derecha
            ) {
                Text(text = "Mi Cuenta")
            }

            // Botón "Mis Reservas"
            Button(
                onClick = onMyReservationsClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(text = "Mis Reservas")
            }
        }

        // Listado de clases
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(clasesList.size) { index ->
                ClaseItem(
                    clase = clasesList[index],
                    onClick = {
                        onClassClick(clasesList[index]) // Pasamos la clase seleccionada
                    }
                )
            }
        }
    }
}




