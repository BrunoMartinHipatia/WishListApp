package com.example.wishlistapp

import androidx.room.TypeConverter

import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromPreguntaList(preguntas: List<Group>?): String {
        return Gson().toJson(preguntas)
    }

    @TypeConverter
    fun toPreguntaList(json: String): List<Group>? {
        val type = object : TypeToken<List<Group>>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun fromUserList(value: List<SteamUser>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toUserList(value: String): List<SteamUser> {
        val listType = object : TypeToken<List<SteamUser>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
