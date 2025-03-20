package com.example.wishlistapp.ui.theme.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GroupDao {



    @Insert
    suspend fun insertGroup(result: Group)
    @Delete
    suspend fun deleteGroup(result: Group)
    @Query("SELECT * FROM group_table")
    suspend fun getAllGrupos(): List<Group>


}