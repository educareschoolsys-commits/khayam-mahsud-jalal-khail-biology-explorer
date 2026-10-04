package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FeeRecord
import com.example.data.model.GradingRule
import com.example.data.model.ResultRecord
import com.example.data.model.SavedDocument
import com.example.data.model.SchoolPhoto
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.TimetableEntry

@Database(
    entities = [
        SchoolProfile::class,
        GradingRule::class,
        Student::class,
        FeeRecord::class,
        ResultRecord::class,
        TimetableEntry::class,
        SchoolPhoto::class,
        SavedDocument::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getInstance(context: Context): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "iqra_school_database.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
