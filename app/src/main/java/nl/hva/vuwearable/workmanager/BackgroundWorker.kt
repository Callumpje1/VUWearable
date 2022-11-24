package nl.hva.vuwearable.workmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.fragment.app.activityViewModels
import androidx.work.*
import nl.hva.vuwearable.MainActivity
import nl.hva.vuwearable.R
import nl.hva.vuwearable.udp.UDPConnection
import nl.hva.vuwearable.ui.udp.UDPViewModel


class BackgroundWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val CHANNEL_ID = "notificationWifi"

    private val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val udpViewModel = UDPViewModel()
    override fun doWork(): Result {
        createNotificationChannel()
        var result: Result = Result.retry()
        var previous = false
        Thread(UDPConnection({
            when (it) {
                false -> {
                    Log.i("wifi disconnected", "wifi connection")
                    createNotification()
                    result = Result.retry()
                    previous = it
                }
                true -> {
                    Log.i("wifi connected", "wifi connection")
                    result = Result.success()
                    previous = it

                }
            }
        }, 10, 10)).start()
        return result
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                name = "notification channel"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() {
        val activityActionIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                activityActionIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_vu_logo)
                .setContentTitle("Connection Lost")
                .setContentText("Return to the app and reconnect the device")
                .addAction(R.drawable.ic_vu_logo, "Fix issue", pendingIntent)
        notificationManager.notify(1, notificationBuilder.build())
    }
}