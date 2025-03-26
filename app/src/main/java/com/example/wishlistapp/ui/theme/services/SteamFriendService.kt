package com.example.wishlistapp.ui.theme.services

import android.util.Log
import com.example.wishlistapp.ui.theme.data.Friend
import com.example.wishlistapp.ui.theme.data.SteamUser
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Interfaz para obtener la lista de amigos de un usuario de Steam
interface SteamFriendService {
    @GET("ISteamUser/GetFriendList/v1/")
    suspend fun getFriendList(
        @Query("key") apiKey: String,
        @Query("steamid") steamId: String
    ): SteamFriendResponse

    @GET("ISteamUser/GetPlayerSummaries/v2/")
    suspend fun getFriendDetails(
        @Query("key") apiKey: String,
        @Query("steamids") steamIds: String
    ): SteamUserResponse

    companion object {
        fun create(): SteamFriendService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.steampowered.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(SteamFriendService::class.java)
        }
    }
}

// Modelos de datos para la respuesta de Steam
data class SteamFriendResponse(val friendslist: FriendList?)
data class FriendList(val friends: List<Friend>?)



// Servicio para manejar la obtención de amigos
interface FriendService {
    suspend fun fetchFriendList(steamId: String, apiKey: String): List<Friend>?
    suspend fun fetchFriendDetails(steamIds: List<String>, apiKey: String): List<SteamUser>?
}

class FriendServiceImpl(private val steamFriendService: SteamFriendService) : FriendService {
    override suspend fun fetchFriendList(steamId: String, apiKey: String): List<Friend>? {
        return try {
            val url = "https://api.steampowered.com/ISteamUser/GetFriendList/v1/?key=$apiKey&steamid=$steamId"
            println("Fetching friends from: $url") // Imprime en consola
            Log.d("SteamAPI", "Fetching friends from: $url") // Imprime en Logcat (Android Studio)

            val response = steamFriendService.getFriendList(apiKey, steamId)
            response.friendslist?.friends
        } catch (e: Exception) {
            Log.e("SteamAPI", "Error fetching friends", e)
            null
        }
    }


    override suspend fun fetchFriendDetails(steamIds: List<String>, apiKey: String): List<SteamUser>? {
        return try {
            val ids = steamIds.joinToString(",")
            val response = steamFriendService.getFriendDetails(apiKey, ids)
            response.response.players
        } catch (e: Exception) {
            null
        }
    }
}
