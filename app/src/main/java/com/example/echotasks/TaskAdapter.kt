package com.example.echotasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onDeleteClick: (Task) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onColorSelected: (Task, String) -> Unit,
    private val onCheckedChange: (Int, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, onDeleteClick, onEditClick , onColorSelected ,onCheckedChange)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val taskTime: TextView = itemView.findViewById(R.id.tvTime)
        private val taskDesc: TextView = itemView.findViewById(R.id.tvDesc)
        private val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val editBtn: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val circleRed: View = itemView.findViewById(R.id.circleRed)
        private val circleBlue: View = itemView.findViewById(R.id.circleBlue)
        private val circleGreen: View = itemView.findViewById(R.id.circleGreen)
        private val circleWhite:View = itemView.findViewById(R.id.circleWhite)
        val cardView: CardView = itemView.findViewById(R.id.cardRoot)
        private val checkboxCompleted: CheckBox = itemView.findViewById(R.id.cbDone)


        fun bind(
            task: Task,
            onDeleteClick: (Task) -> Unit,
            onEditClick: (Task) -> Unit,
            onColorSelected: (Task, String) -> Unit,
            onCheckedChange: (Int, Boolean) -> Unit
        ) {
            taskTitle.text = task.title
            taskDesc.text = task.description
            taskTime.text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date(task.timeInMillis))

            deleteBtn.setOnClickListener { onDeleteClick(task) }
            editBtn.setOnClickListener { onEditClick(task) }
            circleRed.setOnClickListener { onColorSelected(task, "red") }
            circleBlue.setOnClickListener { onColorSelected(task, "blue") }
            circleGreen.setOnClickListener { onColorSelected(task, "green") }
            circleWhite.setOnClickListener { onColorSelected(task, "white") }


            checkboxCompleted.isChecked = task.isCompleted
            checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(task.id, isChecked)
            }

            // Set CardView background based on task color
            when (task.color) {
                "red" -> cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#FFCDD2"))
                "blue" -> cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#BBDEFB"))
                "green" -> cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#C8E6C9"))
                "white" -> cardView.setCardBackgroundColor(android.graphics.Color.WHITE)
                else -> cardView.setCardBackgroundColor(android.graphics.Color.WHITE)
            }

        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
