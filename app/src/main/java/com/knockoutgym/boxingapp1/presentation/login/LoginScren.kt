package com.knockoutgym.boxingapp1.presentation.login

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    navigateToHome: () -> Unit
) {
    // Variables de estado
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estados para mensajes de error
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Visibilidad de la contraseña
    var passwordVisible by remember { mutableStateOf(false) }

    // Estado de carga
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Estado de scroll para permitir desplazamiento vertical
    val scrollState = rememberScrollState()

    // Estado para el diálogo de recuperación de contraseña
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetEmailError by remember { mutableStateOf<String?>(null) }
    var isResetLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Inicia Sesión",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) {
                    emailError = null
                }
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        if (emailError != null) {
            Text(
                text = emailError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) {
                    passwordError = null
                }
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                }
            }
        )
        if (passwordError != null) {
            Text(
                text = passwordError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // Validar los campos
                val isValid = validateInput(
                    email = email,
                    password = password,
                    setEmailError = { emailError = it },
                    setPasswordError = { passwordError = it }
                )

                if (isValid) {
                    isLoading = true
                    // Intentar iniciar sesión
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                // El usuario inició sesión correctamente
                                // Antes de navegar a Home, obtener el token FCM
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                        if (tokenTask.isSuccessful) {
                                            val token = tokenTask.result
                                            if (token != null) {
                                                val db = FirebaseFirestore.getInstance()
                                                db.collection("usuarios").document(userId)
                                                    .update("fcmToken", token)
                                                    .addOnSuccessListener {
                                                        Log.d("FCM", "Token actualizado en Firestore manualmente")
                                                        // Ahora sí navegar a Home
                                                        navigateToHome()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("FCM", "Error al actualizar token manualmente", e)
                                                        // Si el token es nulo navegar igualmente al Home
                                                        navigateToHome()
                                                    }
                                            } else {
                                                Log.e("FCM", "El token FCM es nulo")
                                                // Si el token es nulo navegar igualmente al Home
                                                navigateToHome()
                                            }
                                        } else {
                                            Log.e("FCM", "Falló la obtención del token FCM", tokenTask.exception)
                                            // Podrías navegar igual, aunque no hayas guardado el token
                                            navigateToHome()
                                        }
                                    }
                                } else {
                                    Log.e("FCM", "userId es nulo después del login")
                                    // Navegar a Home aunque no se haya podido actualizar el token
                                    navigateToHome()
                                }

                            } else {
                                // Manejar errores de login
                                val exception = task.exception
                                val errorMessage = when (exception) {
                                    is FirebaseAuthException -> {
                                        when (exception.errorCode) {
                                            "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
                                            "ERROR_USER_NOT_FOUND" -> "Usuario no encontrado"
                                            "ERROR_USER_DISABLED" -> "Usuario deshabilitado"
                                            "ERROR_INVALID_EMAIL" -> "Correo electrónico inválido"
                                            else -> "Error: ${exception.localizedMessage}"
                                        }
                                    }
                                    else -> {
                                        "Error al iniciar sesión: ${exception?.localizedMessage}"
                                    }
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                Log.e("Login", "Error: ${exception?.message}")
                            }
                        }
                } else {
                    Toast.makeText(context, "Por favor, corrige los errores", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(text = "Iniciar Sesión")
            }
        }


        // Enlace para recuperar contraseña
        TextButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("¿Olvidaste tu contraseña?")
        }

        // Diálogo de recuperación de contraseña
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Recuperar Contraseña") },
                text = {
                    Column {
                        Text("Ingresa tu correo electrónico para recibir un enlace de restablecimiento de contraseña.")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = {
                                resetEmail = it
                                if (resetEmailError != null) {
                                    resetEmailError = null
                                }
                            },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = resetEmailError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        if (resetEmailError != null) {
                            Text(
                                text = resetEmailError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Validar el correo electrónico
                            if (resetEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                                resetEmailError = "Correo electrónico inválido"
                            } else {
                                isResetLoading = true
                                auth.sendPasswordResetEmail(resetEmail)
                                    .addOnCompleteListener { task ->
                                        isResetLoading = false
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Se ha enviado un enlace de restablecimiento a tu correo.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            showResetDialog = false
                                        } else {
                                            val exception = task.exception
                                            val errorMessage = when (exception) {
                                                is FirebaseAuthInvalidUserException -> "No existe una cuenta con este correo electrónico."
                                                else -> "Error al enviar el correo: ${exception?.localizedMessage}"
                                            }
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        }
                    ) {
                        if (isResetLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Enviar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Función para validar los campos de entrada
fun validateInput(
    email: String,
    password: String,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        setEmailError("Correo electrónico inválido")
        isValid = false
    }

    if (password.isBlank()) {
        setPasswordError("La contraseña no puede estar vacía")
        isValid = false
    }

    return isValid
}
