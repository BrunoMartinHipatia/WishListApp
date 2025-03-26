package com.example.wishlistapp.ui.theme.component

import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.UserService

class DummyUserService : UserService {
    // Implementa solo lo mínimo necesario si se usa algo
    override suspend fun fetchSteamUser(steamId: String, apiKey: String): SteamUser? {
        TODO("Not yet implemented")
    }
}
