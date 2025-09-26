package com.example.echotasks

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("taskId", -1)
        if (taskId == -1) return

        // ✅ Fetch latest task from DB
        val db = TaskDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val task = db.taskDao().getTaskById(taskId)
            if (task != null && !task.isCompleted) {
                withContext(Dispatchers.Main) {
                    showNotification(context, task.title, task.description)
                    Handler(Looper.getMainLooper()).postDelayed({
                        SpeechRepeater.start(context, task.title)
                    }, 100)
                }
            }
        }
    }

    private fun showNotification(ctx: Context, title: String, desc: String) {
        val channelId = "task_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            ctx.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val stopIntent = Intent(ctx, StopReceiver::class.java)
        val stopPending = PendingIntent.getBroadcast(
            ctx, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(desc)
            .setStyle(NotificationCompat.BigTextStyle().bigText(desc))
            .setOngoing(true)
            .addAction(R.drawable.ic_stop, "Stop", stopPending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        NotificationManagerCompat.from(ctx).notify(1, notification)
    }

    companion object {
        fun cancelAlarm(context: Context, taskId: Int) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "ECHO_TASK_ALARM"
                putExtra("taskId", taskId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
