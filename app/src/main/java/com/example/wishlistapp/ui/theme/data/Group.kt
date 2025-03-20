package com.example.wishlistapp.ui.theme.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wishlistapp.ui.theme.data.SteamUser

@Entity(tableName = "group_table")
data class Group(
    @PrimaryKey(autoGenerate = true)
                 @ColumnInfo(name = "group_id")
                 val id:  Int = 0,
                 @ColumnInfo(name = "group_name")
                 val nombre: String,
                 @ColumnInfo("group_list_users")
                 val listaUsuarios: List<SteamUser>)