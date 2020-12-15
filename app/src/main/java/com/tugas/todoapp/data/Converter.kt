package com.tugas.todoapp.data

import androidx.room.TypeConverter
import com.tugas.todoapp.data.model.Priority

class Converter {

    @TypeConverter
    fun fromPriority(priority: Priority) : String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String) : Priority {
        return Priority.valueOf(priority)
    }
}