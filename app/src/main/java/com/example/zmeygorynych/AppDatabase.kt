package com.example.zmeygorynych

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Personnel::class, WorkType::class, PositionCode::class, WorkDay::class],
    version = 9, // Увеличиваем версию до 8
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

        // Миграция с версии 7 на 8 (добавляем новые поля)
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем новые колонки для сумм часов
                database.execSQL("ALTER TABLE work_days ADD COLUMN windowHoursSum INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE work_days ADD COLUMN skdHoursSum INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE work_days ADD COLUMN zubovHoursSum INTEGER DEFAULT NULL")
            }
        }

        // Если были другие миграции, добавьте их тоже
        private val MIGRATION_1_8 = object : Migration(1, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Здесь можно описать все изменения с версии 1 до 8
                // Но лучше использовать поэтапные миграции
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zmeygorynych.db"
                )
                    .addMigrations(MIGRATION_7_8) // Добавляем нашу миграцию
                    // .fallbackToDestructiveMigration() // УДАЛИТЬ эту строку!
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}


