package com.example.wishlistapp.ui.theme.data

class SteamUserRepositoryImpl(private val dao: SteamUserDao): SteamUserRepository {
    override suspend fun insertResult(result: SteamUser) {
        dao.insertSteamUser(result)
    }

    override suspend fun deleteResult(result: SteamUser) {
        dao.insertSteamUser(result)
    }
}