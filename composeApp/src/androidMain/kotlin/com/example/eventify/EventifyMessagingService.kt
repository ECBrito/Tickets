package com.example.eventify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class EventifyMessagingService : FirebaseMessagingService() {

    // Chamado quando chega uma mensagem e a app está em ABERTA (Foreground)
    // ou para processar dados quando está em background.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Se a notificação tiver título e corpo, mostramos
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Eventify", it.body ?: "Nova atualização!")
        }
    }

    // Chamado quando o Token muda (ex: primeira instalação)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aqui deveríamos enviar o token para o servidor/repo,
        // mas como não temos injeção aqui, faremos isso no MainScreen.
        println("FCM Token Atualizado: $token")
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "eventify_updates"
        val notificationId = Random.nextInt()

        // 1. Intent para abrir a app ao clicar na notificação
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Gerir o Canal (Obrigatório Android 8+)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 3. Construir a Notificação
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Podes mudar para o teu logo R.drawable.ic_logo
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(notificationId, builder.build())
    }
}