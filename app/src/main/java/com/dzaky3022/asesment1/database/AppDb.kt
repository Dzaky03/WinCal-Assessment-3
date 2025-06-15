package com.dzaky3022.asesment1.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dzaky3022.asesment1.ui.model.User
import com.dzaky3022.asesment1.ui.model.WaterResultEntity

@Database(entities = [WaterResultEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterResultDao(): WaterResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getAppDb(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = AppDatabase::class.java,
                    name = "wincal_db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}