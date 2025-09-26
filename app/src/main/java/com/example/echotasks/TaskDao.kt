package com.example.echotasks

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("SELECT * FROM tasks ORDER BY timeInMillis ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY timeInMillis ASC")
    suspend fun getAllTasksList(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): Task?
}
