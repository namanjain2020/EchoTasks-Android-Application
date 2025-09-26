package com.example.echotasks

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskRepository(private val taskDao: TaskDao, private val context: Context) {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    fun insert(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.insert(task)
            scheduleAlarm(task)
        }
    }

    fun update(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.update(task)
            AlarmReceiver.cancelAlarm(context, task.id)
            if (!task.isCompleted) {
                scheduleAlarm(task)
            }
        }
    }

    fun delete(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.delete(task)
            AlarmReceiver.cancelAlarm(context, task.id)
        }
    }

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    private fun scheduleAlarm(task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ECHO_TASK_ALARM"
            putExtra("taskId", task.id)  // ✅ only taskId for matching
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        task.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        task.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.timeInMillis,
                    pendingIntent
                )
            }
        } catch (se: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                task.timeInMillis,
                pendingIntent
            )
        }
    }
}
