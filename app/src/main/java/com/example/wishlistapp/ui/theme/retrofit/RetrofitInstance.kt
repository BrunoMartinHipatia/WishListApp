package com.example.wishlistapp.ui.theme.retrofit

import com.example.wishlistapp.ui.theme.services.SteamApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: SteamApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.steampowered.com/") // base URL para obtener app list
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SteamApiService::class.java)
    }

    val storeApi: SteamApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://store.steampowered.com/") // base URL para appdetails
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SteamApiService::class.java)
    }
}
