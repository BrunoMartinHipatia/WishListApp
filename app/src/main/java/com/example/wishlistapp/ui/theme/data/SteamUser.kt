package com.example.wishlistapp.ui.theme.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_table")
data class SteamUser(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "steam_user_id")
    val id: Int,
    @ColumnInfo(name = "steam_id")
    val steamid: String,
    @ColumnInfo(name = "personaname")
    val personaname: String,
    @ColumnInfo(name = "avatarfull")
    val avatarfull: String,
    @ColumnInfo(name = "gruposaceptados")
    val gruposAceptados: List<Group>,
    @ColumnInfo(name = "gruposnoaceptados")
    val gruposNoAceptados: List<Group>) : Parcelable

