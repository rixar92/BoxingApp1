package com.knockoutgym.boxingapp1.presentation.register

import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import com.knockoutgym.boxingapp1.presentation.data.provincias
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Función para validar los campos de entrada
@RequiresApi(Build.VERSION_CODES.O)
fun validateInput(
    nombre: String,
    apellidos: String,
    dni: String,
    fechaNacimiento: String,
    telefono: String,
    direccion: String,
    provincia: String,
    localidad: String,
    email: String,
    password: String,
    setNombreError: (String?) -> Unit,
    setApellidosError: (String?) -> Unit,
    setDniError: (String?) -> Unit,
    setFechaNacimientoError: (String?) -> Unit,
    setTelefonoError: (String?) -> Unit,
    setDireccionError: (String?) -> Unit,
    setProvinciaError: (String?) -> Unit,
    setLocalidadError: (String?) -> Unit,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (nombre.isBlank()) {
        setNombreError("El nombre no puede estar vacío")
        isValid = false
    }

    if (apellidos.isBlank()) {
        setApellidosError("Los apellidos no pueden estar vacíos")
        isValid = false
    }

    if (!isValidDNI(dni)) {
        setDniError("DNI inválido")
        isValid = false
    }

    if (!isValidDate(fechaNacimiento)) {
        setFechaNacimientoError("Fecha de nacimiento inválida")
        isValid = false
    }

    if (!isValidPhoneNumber(telefono)) {
        setTelefonoError("Número de teléfono inválido")
        isValid = false
    }

    if (direccion.isBlank()) {
        setDireccionError("La dirección no puede estar vacía")
        isValid = false
    }

    if (!isValidProvincia(provincia)) {
        setProvinciaError("Provincia inválida")
        isValid = false
    }

    if (localidad.isBlank()) {
        setLocalidadError("La localidad no puede estar vacía")
        isValid = false
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        setEmailError("Correo electrónico inválido")
        isValid = false
    }

    if (password.length < 6) {
        setPasswordError("La contraseña debe tener al menos 6 caracteres")
        isValid = false
    }

    return isValid
}

// Función para validar el DNI
fun isValidDNI(dni: String): Boolean {
    val dniRegex = Regex("""^\d{8}[A-HJ-NP-TV-Z]$""")
    if (!dniRegex.matches(dni)) {
        return false
    }
    val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
    val number = dni.substring(0, 8).toInt()
    val letter = dni[8]
    val calculatedLetter = letras[number % 23]
    return letter == calculatedLetter
}

// Función para validar la fecha de nacimiento
@RequiresApi(Build.VERSION_CODES.O)
fun isValidDate(dateStr: String): Boolean {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(dateStr, formatter)
        val today = LocalDate.now()
        !date.isAfter(today)
    } catch (e: DateTimeParseException) {
        false
    }
}

// Función para validar el número de teléfono
fun isValidPhoneNumber(phone: String): Boolean {
    val phoneRegex =
        Regex("""^[6-9]\d{8}$""") // Números españoles que empiezan con 6, 7, 8 o 9 y tienen 9 dígitos
    return phoneRegex.matches(phone)
}

// Función para validar la provincia
fun isValidProvincia(provincia: String): Boolean {
    return provincias.contains(provincia.trim())
}
// Funciones de validación individuales
fun validateNombre(nombre: String, setNombreError: (String?) -> Unit): Boolean {
    return if (nombre.isBlank()) {
        setNombreError("El nombre no puede estar vacío")
        false
    } else {
        setNombreError(null)
        true
    }
}

fun validateApellidos(apellidos: String, setApellidosError: (String?) -> Unit): Boolean {
    return if (apellidos.isBlank()) {
        setApellidosError("Los apellidos no pueden estar vacíos")
        false
    } else {
        setApellidosError(null)
        true
    }
}

// Función para actualizar un campo específico en Firestore
fun updateUserField(
    userId: String,
    fieldName: String,
    value: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updates = hashMapOf<String, Any>(
        fieldName to value
    )
    db.collection("usuarios").document(userId)
        .update(updates)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Error desconocido")
        }
}
fun validateDni(dni: String, setDniError: (String?) -> Unit): Boolean {
    val dniRegex = Regex("""^\d{8}[A-HJ-NP-TV-Z]$""")
    if (!dniRegex.matches(dni)) {
        setDniError("DNI inválido")
        return false
    }
    val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
    val number = dni.substring(0, 8).toInt()
    val letter = dni[8]
    val calculatedLetter = letras[number % 23]
    if (letter != calculatedLetter) {
        setDniError("La letra del DNI no coincide")
        return false
    }
    setDniError(null)
    return true
}
@RequiresApi(Build.VERSION_CODES.O)
fun validateFechaNacimiento(fechaNacimiento: String, setFechaNacimientoError: (String?) -> Unit): Boolean {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(fechaNacimiento, formatter)
        val today = LocalDate.now()
        if (date.isAfter(today)) {
            setFechaNacimientoError("La fecha no puede ser futura")
            false
        } else {
            setFechaNacimientoError(null)
            true
        }
    } catch (e: DateTimeParseException) {
        setFechaNacimientoError("Formato de fecha inválido")
        false
    }
}
fun validateTelefono(telefono: String, setTelefonoError: (String?) -> Unit): Boolean {
    val phoneRegex = Regex("""^[6-9]\d{8}$""")
    if (!phoneRegex.matches(telefono)) {
        setTelefonoError("Número de teléfono inválido")
        return false
    }
    setTelefonoError(null)
    return true
}

fun validateDireccion(direccion: String, setDireccionError: (String?) -> Unit): Boolean {
    return if (direccion.isBlank()) {
        setDireccionError("La dirección no puede estar vacía")
        false
    } else {
        setDireccionError(null)
        true
    }
}

fun validateProvincia(provincia: String, setProvinciaError: (String?) -> Unit): Boolean {
    return if (provincias.contains(provincia)) {
        setProvinciaError(null)
        true
    } else {
        setProvinciaError("Provincia inválida")
        false
    }
}

fun validateLocalidad(localidad: String, setLocalidadError: (String?) -> Unit): Boolean {
    return if (localidad.isBlank()) {
        setLocalidadError("La localidad no puede estar vacía")
        false
    } else {
        setLocalidadError(null)
        true
    }
}

fun validateEmail(email: String, setEmailError: (String?) -> Unit): Boolean {
    return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        setEmailError("Correo electrónico inválido")
        false
    } else {
        setEmailError(null)
        true
    }
}


