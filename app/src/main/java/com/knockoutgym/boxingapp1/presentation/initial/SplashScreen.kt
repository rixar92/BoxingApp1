package com.knockoutgym.boxingapp1.presentation.initial

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            // No está autenticado, navegar a la pantalla inicial
            navHostController.navigate("initial") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // Obtener el rol del usuario
            val role = getUserRole(auth.currentUser!!.uid)
            if (role != null) {
                when (role) {
                    "admin" -> {
                        navHostController.navigate("admin_home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    "user" -> {
                        navHostController.navigate("user_home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    else -> {
                        Toast.makeText(context, "Rol desconocido", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        navHostController.navigate("initial") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "No se pudo obtener el rol del usuario", Toast.LENGTH_LONG).show()
                auth.signOut()
                navHostController.navigate("initial") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // Se muestra un circulo de carga
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// Función para obtener el rol del usuario
suspend fun getUserRole(userId: String): String? {
    val db = FirebaseFirestore.getInstance()
    return try {
        val snapshot = db.collection("usuarios").document(userId).get().await()
        snapshot.getString("role")
    } catch (e: Exception) {
        null
    }
}
