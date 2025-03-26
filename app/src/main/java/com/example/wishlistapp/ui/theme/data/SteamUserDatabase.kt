package com.example.wishlistapp.ui.theme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wishlistapp.Converters

@Database(entities = [SteamUser::class], version = 1)
@TypeConverters(Converters::class)
abstract class SteamUserDatabase : RoomDatabase()
{
    abstract fun converterDao(): SteamUserDao
    companion object {
        @Volatile
        private var INSTANCE: SteamUserDatabase? = null

        fun getDatabase(context: Context): SteamUserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SteamUserDatabase::class.java,
                    "steamuser_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}