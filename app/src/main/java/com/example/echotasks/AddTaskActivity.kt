package com.example.echotasks

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private var calendar: Calendar = Calendar.getInstance()
    private var taskId: Int = -1
    private var isDateTimeChanged = false
    private var selectedColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val edtTitle: EditText = findViewById(R.id.etTitle)
        val edtDesc: EditText = findViewById(R.id.etDesc)
        val btnPickTime: Button = findViewById(R.id.btnPickTime)
        val tvSelectedTime: TextView = findViewById(R.id.tvSelectedTime)
        val btnSave: Button = findViewById(R.id.btnSave)

        // ✅ Check if we're editing an existing task
        taskId = intent.getIntExtra("task_id", -1)
        if (taskId != -1) {
            val taskTitle = intent.getStringExtra("task_title")
            val taskDesc = intent.getStringExtra("task_description")
            val taskTime = intent.getLongExtra("task_time", 0L)
            selectedColor = intent.getStringExtra("task_color") ?: ""


            edtTitle.setText(taskTitle)
            edtDesc.setText(taskDesc)

            if (taskTime > 0L) {
                calendar.timeInMillis = taskTime
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                tvSelectedTime.text = sdf.format(calendar.time)
            }

            btnSave.text = "Update Task"
        }

        // Date & Time Picker
        btnPickTime.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)

                    TimePickerDialog(
                        this,
                        { _, hour, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)

                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            tvSelectedTime.text = sdf.format(calendar.time)
                            isDateTimeChanged = true
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save or Update Task
        btnSave.setOnClickListener {
            val taskTitle = edtTitle.text.toString().trim()
            val taskDesc = edtDesc.text.toString().trim()

            if (taskTitle.isEmpty()) {
                edtTitle.error = "Title required"
                return@setOnClickListener
            }

            val finalTime = if (taskId != -1 && !isDateTimeChanged) {
                intent.getLongExtra("task_time", 0L)
            } else {
                calendar.timeInMillis
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                tvSelectedTime.text = "Please select a future time"
                return@setOnClickListener
            }

            val task = Task(
                id = if (taskId != -1) taskId else 0,
                title = taskTitle,
                description = taskDesc,
                timeInMillis = finalTime,
                color = selectedColor
            )

            if (taskId == -1) {
                taskViewModel.insert(task)
            } else {
                taskViewModel.update(task)
            }

            scheduleReminder(task)
            finish()
        }
    }

    private fun scheduleReminder(task: Task) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ✅ On Android 12+ check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
            putExtra("taskDesc", task.description)
            putExtra("taskId", task.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id, // 👈 use task.id for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                task.timeInMillis,
                pendingIntent
            )
        } catch (se: SecurityException) {
            se.printStackTrace()
            Toast.makeText(
                this,
                "Unable to schedule alarm. Please allow Exact Alarms in settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
