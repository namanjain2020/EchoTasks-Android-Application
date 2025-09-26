package com.example.echotasks

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = TaskDatabase.getDatabase(context)
                val tasks = db.taskDao().getAllTasksList()

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                for (task in tasks) {
                    val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                        putExtra("taskTitle", task.title)
                        putExtra("taskId", task.id)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        task.id,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                task.timeInMillis,
                                pendingIntent
                            )
                        } else {

                             val requestIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                             requestIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                             context.startActivity(requestIntent)
                        }
                    } else {

                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            task.timeInMillis,
                            pendingIntent
                        )
                    }
                }
            }
        }
    }
}
