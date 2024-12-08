package com.knockoutgym.boxingapp1.presentation.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class validacionDiaHora {

}
@RequiresApi(Build.VERSION_CODES.O)
// Validación de tiempo en formato HH:mm
fun isValidTime(time: String): Boolean {
    return try {
        val (hours, minutes) = time.split(":").map { it.toInt() }
        hours in 0..23 && minutes in 0..59
    } catch (e: Exception) {
        false
    }
}

// Validación de día en formato YYYY-MM-DD
@RequiresApi(Build.VERSION_CODES.O)
fun isValidDay(day: String): Boolean {
    return try {
        LocalDate.parse(day)
        true
    } catch (e: Exception) {
        false
    }
}
// Función auxiliar para parsear fechas de forma segura
@RequiresApi(Build.VERSION_CODES.O)
fun parseDateOrNull(dateString: String): LocalDate? {
    return try {
        LocalDate.parse(dateString)
    } catch (e: Exception) {
        null
    }
}