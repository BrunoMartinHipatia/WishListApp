package com.example.wishlistapp.ui.theme.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SteamUserDao {



    @Insert
    suspend fun insertSteamUser(result: SteamUser)
    @Delete
    suspend fun deleteSteamUser(result: SteamUser)
    @Query("SELECT * FROM user_table")
    suspend fun getAllSteamUsers(): List<SteamUser>


}