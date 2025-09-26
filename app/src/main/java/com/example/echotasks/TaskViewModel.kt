package com.example.echotasks

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    val allTasks: LiveData<List<Task>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao, application)
        allTasks = repository.allTasks
    }

    fun insert(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        cancelAlarm(task)
        repository.delete(task)
    }

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) = viewModelScope.launch {
        val task = repository.getTaskById(taskId)
        if (task != null) {
            repository.update(task.copy(isCompleted = isCompleted))
        }
    }

    private fun cancelAlarm(task: Task) {
        val alarmManager =
            getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(getApplication(), AlarmReceiver::class.java).apply {
            action = "ECHO_TASK_ALARM"
            putExtra("taskId", task.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
