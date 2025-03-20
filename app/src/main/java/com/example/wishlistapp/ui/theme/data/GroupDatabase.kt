package com.example.wishlistapp.ui.theme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wishlistapp.Converters

@Database(entities = [Group::class], version = 1)
@TypeConverters(Converters::class)
abstract class GroupDatabase : RoomDatabase()
{
    abstract fun converterDao(): GroupDao
    companion object {
        @Volatile
        private var INSTANCE: GroupDatabase? = null

        fun getDatabase(context: Context): GroupDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GroupDatabase::class.java,
                    "group_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}