package com.example.zmeygorynych

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.zmeygorynych.converters.DateConverter

@Database(
        entities = [Personnel::class, WorkType::class, PositionCode::class, WorkDay::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personnelDao(): PersonnelDao
    abstract fun workTypeDao(): WorkTypeDao
    abstract fun positionCodeDao(): PositionCodeDao
    abstract fun workDayDao(): WorkDayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zmeygorynych.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}


