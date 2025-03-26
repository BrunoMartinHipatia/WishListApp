package com.example.wishlistapp.ui.theme.services

import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {
    @GET("https://api.steampowered.com/ISteamApps/GetAppList/v2/")
    suspend fun getAppList(): AppListResponse

    @GET("https://store.steampowered.com/api/appdetails")
    suspend fun getGameDetails(
        @Query("appids") appid: Int
    ): Map<String, GameDetailResponse>
}
