package com.example.wishlistapp.ui.theme.services

import com.example.wishlistapp.ui.theme.data.SteamAppListResponse

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GameService {


        @GET("ISteamApps/GetAppList/v0002/")
        fun getAppList(
            @Query("key") apiKey: String,
            @Query("format") format: String = "json"
        ): Call<SteamAppListResponse>


    }