package com.example.wishlistapp.ui.theme.data

import androidx.room.Query

interface SteamUserRepository {

suspend fun insertResult(result: SteamUser)
suspend fun deleteResult(result: SteamUser)


}