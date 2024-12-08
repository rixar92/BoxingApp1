package com.knockoutgym.boxingapp1.presentation.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
//Funcion para crear un canal para las notificaciones push
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Recordatorios de Clases"
        val descriptionText = "Notificaciones para recordarte tus clases"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("CLASES_CHANNEL_ID", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
