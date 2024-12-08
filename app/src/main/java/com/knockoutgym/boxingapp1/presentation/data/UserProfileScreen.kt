@file:Suppress("DEPRECATION")

package com.knockoutgym.boxingapp1.presentation.data

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.knockoutgym.boxingapp1.presentation.register.updateUserField
import com.knockoutgym.boxingapp1.presentation.register.validateApellidos
import com.knockoutgym.boxingapp1.presentation.register.validateDireccion
import com.knockoutgym.boxingapp1.presentation.register.validateDni
import com.knockoutgym.boxingapp1.presentation.register.validateEmail
import com.knockoutgym.boxingapp1.presentation.register.validateFechaNacimiento
import com.knockoutgym.boxingapp1.presentation.register.validateLocalidad
import com.knockoutgym.boxingapp1.presentation.register.validateNombre
import com.knockoutgym.boxingapp1.presentation.register.validateProvincia
import com.knockoutgym.boxingapp1.presentation.register.validateTelefono


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserProfileScreen(
    auth: FirebaseAuth,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val userId = auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    // Estados para los datos del usuario
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Estado para controlar qué campo está en edición
    var campoEnEdicion by remember { mutableStateOf<String?>(null) }

    // Estados para los mensajes de error
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidosError by remember { mutableStateOf<String?>(null) }
    var dniError by remember { mutableStateOf<String?>(null) }
    var fechaNacimientoError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }
    var direccionError by remember { mutableStateOf<String?>(null) }
    var provinciaError by remember { mutableStateOf<String?>(null) }
    var localidadError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    // Estado de carga
    var isLoading by remember { mutableStateOf(true) }
    // Estado para mostrar el diálogo de contraseña
    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    // Cargar los datos del usuario desde Firestore
    LaunchedEffect(Unit) {
        if (userId != null) {
            db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        nombre = document.getString("nombre") ?: ""
                        apellidos = document.getString("apellidos") ?: ""
                        dni = document.getString("dni") ?: ""
                        fechaNacimiento = document.getString("fechaNacimiento") ?: ""
                        telefono = document.getString("telefono") ?: ""
                        direccion = document.getString("direccion") ?: ""
                        provincia = document.getString("provincia") ?: ""
                        localidad = document.getString("localidad") ?: ""
                        email = document.getString("email") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error al cargar datos: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    isLoading = false
                }
        }
    }

    if (isLoading) {
        // Mostrar un indicador de carga
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Mostrar los datos del usuario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Mi Cuenta",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)

            )

            // Campo de Nombre
            ProfileField(
                label = "Nombre",
                value = nombre,
                isEditing = campoEnEdicion == "nombre",
                onValueChange = { nombre = it },
                onEditClick = {
                    campoEnEdicion = if (campoEnEdicion == "nombre") null else "nombre"
                },
                isError = nombreError != null,
                errorMessage = nombreError,
                onSave = {
                    val isValid = validateNombre(nombre, setNombreError = { nombreError = it })
                    if (isValid && userId != null) {
                        updateUserField(userId, "nombre", nombre, onSuccess = {
                            campoEnEdicion = null
                            Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                        }, onFailure = {
                            Toast.makeText(
                                context,
                                "Error al actualizar nombre: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            )

            // Campo de Apellidos
            ProfileField(
                label = "Apellidos",
                value = apellidos,
                isEditing = campoEnEdicion == "apellidos",
                onValueChange = { apellidos = it },
                onEditClick = {
                    campoEnEdicion = if (campoEnEdicion == "apellidos") null else "apellidos"
                },
                isError = apellidosError != null,
                errorMessage = apellidosError,
                onSave = {
                    val isValid =
                        validateApellidos(apellidos, setApellidosError = { apellidosError = it })
                    if (isValid && userId != null) {
                        updateUserField(userId, "apellidos", apellidos, onSuccess = {
                            campoEnEdicion = null
                            Toast.makeText(context, "Apellidos actualizados", Toast.LENGTH_SHORT)
                                .show()
                        }, onFailure = {
                            Toast.makeText(
                                context,
                                "Error al actualizar apellidos: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            )

            // Campo de DNI
            ProfileField(
                label = "DNI",
                value = dni,
                isEditing = campoEnEdicion == "dni",
                onValueChange = { dni = it },
                onEditClick = { campoEnEdicion = if (campoEnEdicion == "dni") null else "dni" },
                isError = dniError != null,
                errorMessage = dniError,
                onSave = {
                    val isValid = validateDni(dni, setDniError = { dniError = it })
                    if (isValid && userId != null) {
                        updateUserField(userId, "dni", dni, onSuccess = {
                            campoEnEdicion = null
                            Toast.makeText(context, "DNI actualizado", Toast.LENGTH_SHORT).show()
                        }, onFailure = {
                            Toast.makeText(
                                context,
                                "Error al actualizar DNI: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            )

            // Campo de Fecha de Nacimiento
            ProfileField(
                label = "Fecha de Nacimiento",
                value = fechaNacimiento,
                isEditing = campoEnEdicion == "fechaNacimiento",
                onValueChange = { fechaNacimiento = it },
                onEditClick = {
                    campoEnEdicion =
                        if (campoEnEdicion == "fechaNacimiento") null else "fechaNacimiento"
                },
                isError = fechaNacimientoError != null,
                errorMessage = fechaNacimientoError,
                onSave = {
                    val isValid = validateFechaNacimiento(
                        fechaNacimiento,
                        setFechaNacimientoError = { fechaNacimientoError = it })
                    if (isValid && userId != null) {
                        updateUserField(userId, "fechaNacimiento", fechaNacimiento, onSuccess = {
                            campoEnEdicion = null
                            Toast.makeText(
                                context,
                                "Fecha de nacimiento actualizada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, onFailure = {
                            Toast.makeText(
                                context,
                                "Error al actualizar fecha de nacimiento: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            )

            // Campo de Teléfono
            ProfileField(
                label = "Teléfono",
                value = telefono,
                isEditing = campoEnEdicion == "telefono",
                onValueChange = { telefono = it },
                onEditClick = {
                    campoEnEdicion = if (campoEnEdicion == "telefono") null else "telefono"
                },
                isError = telefonoError != null,
                errorMessage = telefonoError,
                onSave = {
                    val isValid =
                        validateTelefono(telefono, setTelefonoError = { telefonoError = it })
                    if (isValid && userId != null) {
                        updateUserField(userId, "telefono", telefono, onSuccess = {
                            campoEnEdicion = null
                            Toast.makeText(context, "Teléfono actualizado", Toast.LENGTH_SHORT)
                                .show()
                        }, onFailure = {
                            Toast.makeText(
                                context,
                                "Error al actualizar teléfono: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            )

            // Campo de Dirección
            ProfileField(
                label = "Dirección",
                value = direccion,
                isEditing = campoEnEdicion == "direccion",
                onValueChange = { direccion = it },
                onEditClick = {
                    campoEnEdicion = if (campoEnEdicion == "direccion") null else "direccion"
                },
                isError = direccionError != null,
                errorMessage = direccionError,
                onSave = {
                    val isValid =
                        validateDireccion(direccion, setDireccionError = { direccionError = it })
                    if (isValid && userId != null) {
                        updateUserField(userId, "direccion", direccion, onSuccess = {
                            campoEnEdicion = null
                            Toast.makeText(context, "Dirección actualizada", Toast.LENGTH_SHORT)
                                .show()
                        }, onFailure = {
                            Toast.makeText(
                                context,
                                "Error al actualizar dirección: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            )

            // Campo de Provincia
            ProvinciaField(
                label = "Provincia",
                value = provincia,
                isEditing = campoEnEdicion == "provincia",
                onValueChange = { provincia = it },
                onEditClick = {
                    campoEnEdicion = if (campoEnEdicion == "provincia") null else "provincia"
                },
                isError = provinciaError != null,
                errorMessage = provinciaError,
                onSave = {
                    val isValid =
                        validateProvincia(provincia, setProvinciaError = { provinciaError = it })
                    if (isValid && userId != null) {
                        updateUserField(userId, "provincia", provincia, onSuccess = {
                            campoEnEdicion = null
                            Toast.makeText(context, "Provincia actualizada", Toast.LENGTH_SHORT)
                                .show()
                        }, onFailure = {
                            Toast.makeText(
                                context,
                                "Error al actualizar provincia: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            )

            // Campo de Localidad
            ProfileField(
                label = "Localidad",
                value = localidad,
                isEditing = campoEnEdicion == "localidad",
                onValueChange = { localidad = it },
                onEditClick = {
                    campoEnEdicion = if (campoEnEdicion == "localidad") null else "localidad"
                },
                isError = localidadError != null,
                errorMessage = localidadError,
                onSave = {
                    val isValid =
                        validateLocalidad(localidad, setLocalidadError = { localidadError = it })
                    if (isValid && userId != null) {
                        updateUserField(userId, "localidad", localidad, onSuccess = {
                            campoEnEdicion = null
                            Toast.makeText(context, "Localidad actualizada", Toast.LENGTH_SHORT)
                                .show()
                        }, onFailure = {
                            Toast.makeText(
                                context,
                                "Error al actualizar localidad: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            )

            // Campo de Email
            ProfileField(
                label = "Email",
                value = email,
                isEditing = campoEnEdicion == "email",
                onValueChange = { email = it },
                onEditClick = { campoEnEdicion = if (campoEnEdicion == "email") null else "email" },
                isError = emailError != null,
                errorMessage = emailError,
                onSave = {
                    val isValid = validateEmail(email, setEmailError = { emailError = it })
                    if (isValid) {
                        if (email != auth.currentUser?.email) {
                            // Mostrar el diálogo para solicitar la contraseña
                            showPasswordDialog = true
                        } else {
                            // Si el email no ha cambiado, simplemente cerramos la edición
                            campoEnEdicion = null
                        }
                    }
                }
            )

            // Botón para cerrar sesión
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cerrar Sesión")
            }
        }
    }

// Mostrar el diálogo de contraseña si es necesario
    if (showPasswordDialog) {
        PasswordDialog(
            onDismiss = { showPasswordDialog = false },
            // En el PasswordDialog, después de obtener la contraseña
            onConfirm = { password ->
                currentPassword = password
                showPasswordDialog = false
                // Llamar a la función para reautenticar y enviar el correo de verificación
                reauthenticateAndSendVerificationEmail(auth, email, currentPassword, onSuccess = {
                    campoEnEdicion = null
                    // Informar al usuario
                    Toast.makeText(
                        context,
                        "Se ha enviado un correo de verificación a tu nuevo email. Por favor, verifícalo.",
                        Toast.LENGTH_LONG
                    ).show()
                }, onFailure = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                })
            }
        )
    }
}

// Composable para cada campo del perfil
@Composable
fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    onEditClick: () -> Unit,
    isError: Boolean,
    errorMessage: String?,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    isError = isError,
                    singleLine = true
                )
                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Guardar"
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }
            }
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 0.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}


// Composable para el diálogo de contraseña
@Composable
fun PasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Reautenticación requerida") },
        text = {
            Column {
                Text(text = "Por favor, introduce tu contraseña actual para continuar.")
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isError = false
                    },
                    label = { Text(text = "Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "La contraseña no puede estar vacía",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password.isNotBlank()) {
                        onConfirm(password)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(text = "Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}

// Función para reautenticar y actualizar el email
fun reauthenticateAndSendVerificationEmail(
    auth: FirebaseAuth,
    newEmail: String,
    currentPassword: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val user = auth.currentUser
    if (user != null && user.email != null) {
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Enviar correo de verificación al nuevo email
                user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure("Error al enviar correo de verificación: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onFailure("Error de reautenticación: ${e.message}")
            }
    } else {
        onFailure("Usuario no autenticado.")
    }
}
//Componsable para cargar el desplegable de las provincias
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ProvinciaField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    onEditClick: () -> Unit,
    isError: Boolean,
    errorMessage: String?,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isEditing) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(text = label) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = isError,
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        provincias.forEach { provinciaItem ->
                            DropdownMenuItem(
                                text = { Text(text = provinciaItem) },
                                onClick = {
                                    onValueChange(provinciaItem)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Guardar"
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }
            }
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 0.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}




