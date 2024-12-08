package com.knockoutgym.boxingapp1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.knockoutgym.boxingapp1.ui.theme.BoxingApp1Theme


class MainActivity : ComponentActivity() {

    private lateinit var navHostController: NavHostController
    private lateinit var auth: FirebaseAuth

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        // Inicializar el launcher para solicitar permisos
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido
                Log.d("MainActivity", "Permiso POST_NOTIFICATIONS concedido")
            } else {
                // Permiso denegado
                Log.d("MainActivity", "Permiso POST_NOTIFICATIONS denegado")
                Toast.makeText(
                    this,
                    "Las notificaciones están desactivadas. Puedes activarlas en la configuración de la aplicación.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Solicitar el permiso de notificaciones si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val userId = user.uid
                    if (token != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("usuarios").document(userId)
                            .update("fcmToken", token)
                            .addOnSuccessListener {
                                Log.d("FCM", "Token actualizado en Firestore manualmente")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FCM", "Error al actualizar token manualmente", e)
                            }
                    } else {
                        Log.e("FCM", "No se obtuvo el token FCM")
                    }
                } else {
                    Log.e("FCM", "Falló la obtención del token FCM", task.exception)
                }
            }
        }

        setContent {
            navHostController = rememberNavController()

            BoxingApp1Theme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationWrapper(navHostController, auth)
                }
            }
        }
    }
}
