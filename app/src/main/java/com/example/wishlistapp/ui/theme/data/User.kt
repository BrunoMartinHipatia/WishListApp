package com.example.wishlistapp.ui.theme.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "examen_table")

data class User(

    @PrimaryKey
    @ColumnInfo(name= "user_id")
    val id: Int,
    @ColumnInfo("usuario")
    val usuario: String,
    @ColumnInfo("password")
    val password: String,

)
