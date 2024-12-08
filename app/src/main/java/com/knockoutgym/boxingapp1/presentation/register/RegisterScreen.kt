package com.knockoutgym.boxingapp1.presentation.register

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.knockoutgym.boxingapp1.presentation.data.provincias


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegisterScreen(
    auth: FirebaseAuth,
    onRegisterSuccess: () -> Unit
) {
    // Estado de los campos de entrada
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }

    // Estados para los mensajes de error
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidosError by remember { mutableStateOf<String?>(null) }
    var dniError by remember { mutableStateOf<String?>(null) }
    var fechaNacimientoError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }
    var direccionError by remember { mutableStateOf<String?>(null) }
    var provinciaError by remember { mutableStateOf<String?>(null) }
    var localidadError by remember { mutableStateOf<String?>(null) }

    // Estado para la visibilidad de la contraseña
    var passwordVisible by remember { mutableStateOf(false) }

    // Estado para controlar la expansión del menú desplegable
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Estado de scroll para permitir desplazamiento vertical
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Regístrate",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                if (nombreError != null) {
                    nombreError = null
                }
            },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = nombreError != null,
            singleLine = true
        )
        if (nombreError != null) {
            Text(
                text = nombreError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Apellidos
        OutlinedTextField(
            value = apellidos,
            onValueChange = {
                apellidos = it
                if (apellidosError != null) {
                    apellidosError = null
                }
            },
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth(),
            isError = apellidosError != null,
            singleLine = true
        )
        if (apellidosError != null) {
            Text(
                text = apellidosError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        // DNI
        OutlinedTextField(
            value = dni,
            onValueChange = {
                dni = it.uppercase()
                if (dniError != null) {
                    dniError = null
                }
            },
            label = { Text("DNI") },
            modifier = Modifier.fillMaxWidth(),
            isError = dniError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        if (dniError != null) {
            Text(
                text = dniError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Fecha de Nacimiento
        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = {
                fechaNacimiento = it
                if (fechaNacimientoError != null) {
                    fechaNacimientoError = null
                }
            },
            label = { Text("Fecha de Nacimiento (DD/MM/YYYY)") },
            modifier = Modifier.fillMaxWidth(),
            isError = fechaNacimientoError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        if (fechaNacimientoError != null) {
            Text(
                text = fechaNacimientoError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Teléfono
        OutlinedTextField(
            value = telefono,
            onValueChange = {
                telefono = it
                if (telefonoError != null) {
                    telefonoError = null
                }
            },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            isError = telefonoError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        if (telefonoError != null) {
            Text(
                text = telefonoError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Dirección
        OutlinedTextField(
            value = direccion,
            onValueChange = {
                direccion = it
                if (direccionError != null) {
                    direccionError = null
                }
            },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth(),
            isError = direccionError != null,
            singleLine = true
        )
        if (direccionError != null) {
            Text(
                text = direccionError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Provincia
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = provincia,
                onValueChange = { },
                readOnly = true,
                label = { Text("Provincia") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                isError = provinciaError != null,
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                provincias.forEach { provinciaItem ->
                    DropdownMenuItem(
                        text = { Text(provinciaItem) },
                        onClick = {
                            provincia = provinciaItem
                            expanded = false
                            if (provinciaError != null) {
                                provinciaError = null
                            }
                        }
                    )
                }
            }
        }
        if (provinciaError != null) {
            Text(
                text = provinciaError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Localidad
        OutlinedTextField(
            value = localidad,
            onValueChange = {
                localidad = it
                if (localidadError != null) {
                    localidadError = null
                }
            },
            label = { Text("Localidad") },
            modifier = Modifier.fillMaxWidth(),
            isError = localidadError != null,
            singleLine = true
        )
        if (localidadError != null) {
            Text(
                text = localidadError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

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
                    Icon(imageVector = image, contentDescription = null)
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
                val isValid = validateInput(
                    nombre = nombre,
                    apellidos = apellidos,
                    dni = dni,
                    fechaNacimiento = fechaNacimiento,
                    telefono = telefono,
                    direccion = direccion,
                    provincia = provincia,
                    localidad = localidad,
                    email = email,
                    password = password,
                    setNombreError = { nombreError = it },
                    setApellidosError = { apellidosError = it },
                    setDniError = { dniError = it },
                    setFechaNacimientoError = { fechaNacimientoError = it },
                    setTelefonoError = { telefonoError = it },
                    setDireccionError = { direccionError = it },
                    setProvinciaError = { provinciaError = it },
                    setLocalidadError = { localidadError = it },
                    setEmailError = { emailError = it },
                    setPasswordError = { passwordError = it }
                )

                if (isValid) {
                    // Crear usuario en Firebase Authentication
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: ""
                                val userMap = hashMapOf(
                                    "nombre" to nombre,
                                    "apellidos" to apellidos,
                                    "dni" to dni,
                                    "fechaNacimiento" to fechaNacimiento,
                                    "telefono" to telefono,
                                    "direccion" to direccion,
                                    "provincia" to provincia,
                                    "localidad" to localidad,
                                    "email" to email,
                                    "role" to "user"
                                )

                                db.collection("usuarios").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        // Una vez guardados los datos del usuario, obtener el token FCM
                                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                            if (tokenTask.isSuccessful) {
                                                val token = tokenTask.result
                                                if (token != null) {
                                                    // Guardar el token FCM en el documento del usuario
                                                    db.collection("usuarios").document(userId)
                                                        .update("fcmToken", token)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(
                                                                context,
                                                                "Registro exitoso",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            // Llamar a onRegisterSuccess() después de haber guardado el token
                                                            onRegisterSuccess()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            // Si falla al guardar el token, el usuario sigue registrado
                                                            Toast.makeText(
                                                                context,
                                                                "Registro exitoso pero no se pudo guardar el token FCM: ${e.message}",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            onRegisterSuccess() // Decidir si continuar o no
                                                        }
                                                } else {
                                                    // No se pudo obtener el token (nulo)
                                                    Toast.makeText(
                                                        context,
                                                        "Registro exitoso, pero no se obtuvo el token FCM",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    onRegisterSuccess()
                                                }
                                            } else {
                                                // Falló la obtención del token
                                                Toast.makeText(
                                                    context,
                                                    "Registro exitoso, pero falló al obtener el token: ${tokenTask.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onRegisterSuccess()
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Error al guardar datos: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } else {
                                // Error en el registro
                                Toast.makeText(
                                    context,
                                    "Error en el registro: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e("Register", "Error: ${task.exception}")
                            }
                        }
                } else {
                    Toast.makeText(context, "Por favor, corrige los errores", Toast.LENGTH_LONG)
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Registrarse")
        }
    }
}



