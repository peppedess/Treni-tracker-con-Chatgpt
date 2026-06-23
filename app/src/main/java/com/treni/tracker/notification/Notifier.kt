package com.treni.tracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.treni.tracker.R
import com.treni.tracker.ui.TrenoDetailActivity

object Notifier {

    private const val CHANNEL_ID = "treni_tracker_channel"
    private const val CHANNEL_NAME = "Aggiornamenti treni"

    fun creaCanale(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifiche su ritardi, fermate e arrivi dei treni monitorati"
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * Mostra una notifica che, se toccata, apre direttamente il dettaglio
     * del treno a cui si riferisce (non solo l'app in generale).
     */
    fun notifica(
        context: Context,
        notificationId: Int,
        titolo: String,
        corpo: String,
        numeroTreno: String,
        stazionePartenzaCod: String,
        stazionePartenzaNome: String,
        stazioneDestinazioneNome: String?,
        timestampMs: Long
    ) {
        val intent = Intent(context, TrenoDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(TrenoDetailActivity.EXTRA_NUMERO_TRENO, numeroTreno)
            putExtra(TrenoDetailActivity.EXTRA_STAZIONE_PARTENZA_COD, stazionePartenzaCod)
            putExtra(TrenoDetailActivity.EXTRA_STAZIONE_PARTENZA_NOME, stazionePartenzaNome)
            putExtra(TrenoDetailActivity.EXTRA_STAZIONE_DESTINAZIONE_NOME, stazioneDestinazioneNome)
            putExtra(TrenoDetailActivity.EXTRA_TIMESTAMP_MS, timestampMs)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_train)
            .setContentTitle(titolo)
            .setContentText(corpo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, builder.build())
    }
}
