package com.example.wishlistapp.ui.theme.services

import android.util.Log

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Interfaz para obtener la información de Steam
interface SteamUserService {
    @GET("ISteamUser/GetPlayerSummaries/v2/")
    suspend fun getSteamUser(
        @Query("key") apiKey: String,
        @Query("steamids") steamId: String
    ): SteamUserResponse

    companion object {
        fun create(): SteamUserService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.steampowered.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(SteamUserService::class.java)
        }
    }
}

// Modelos de datos para la respuesta de Steam
data class SteamUserResponse(val response: SteamUserList)
data class SteamUserList(val players: List<SteamUser>)
data class SteamUser(val steamid: String, val personaname: String, val avatarfull: String)

// Interfaz de servicio para manejar usuarios
interface UserService {
    suspend fun fetchSteamUser(steamId: String, apiKey: String): SteamUser?
}

class UserServiceImpl(private val steamService: SteamUserService) : UserService {
    override suspend fun fetchSteamUser(steamId: String, apiKey: String): SteamUser? {
        return try {
            val url = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=$apiKey&steamids=$steamId"
            Log.d("SteamAPI", "Llamando al endpoint: $url") // 🔹 Imprime la URL en Logcat

            val response = steamService.getSteamUser(apiKey, steamId)
            response.response.players.firstOrNull()
        } catch (e: Exception) {
            Log.e("UserService", "Error en la llamada a Steam API", e)
            null
        }
    }
}
