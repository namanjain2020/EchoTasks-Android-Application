package com.example.echotasks
import com.bumptech.glide.Glide
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private lateinit var imgProfile: ImageView

    private val PICK_IMAGE_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgProfile = findViewById(R.id.profileImage)

        // 🔹 Restore saved profile photo if exists
        val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
        val uriString = prefs.getString("profile_uri", null)
        if (uriString != null) {
            loadProfileImage(Uri.parse(uriString))
        }

        // 🔹 Click to choose new photo
        imgProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // RecyclerView + Adapter
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        adapter = TaskAdapter(
            onDeleteClick = { task -> taskViewModel.delete(task) },
            onEditClick = { task ->
                val intent = Intent(this, AddTaskActivity::class.java).apply {
                    putExtra("task_id", task.id)
                    putExtra("task_title", task.title)
                    putExtra("task_description", task.description)
                    putExtra("task_time", task.timeInMillis)
                    putExtra("task_color", task.color)

                }
                startActivity(intent)
            },
            onColorSelected = { task, color ->
                val updatedTask = task.copy(color = color)
                taskViewModel.update(updatedTask)
            },
            onCheckedChange = { taskId, isChecked ->
                taskViewModel.updateTaskCompletion(taskId, isChecked)
            }
        )




        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        taskViewModel.allTasks.observe(this, Observer { tasks ->
            tasks?.let { adapter.submitList(it) }
        })

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                loadProfileImage(uri)

                // Save URI permanently
                val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
                prefs.edit().putString("profile_uri", uri.toString()).apply()
            }
        }
    }

    private fun loadProfileImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(imgProfile)
    }
}
