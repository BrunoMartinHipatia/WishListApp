package com.example.wishlistapp.ui.theme.data

data class SteamAppListResponse(
    val applist: SteamAppContainer
)

data class SteamAppContainer(
    val apps: List<SteamApp>
)

data class SteamApp(
    val appid: Int,
    val name: String
)