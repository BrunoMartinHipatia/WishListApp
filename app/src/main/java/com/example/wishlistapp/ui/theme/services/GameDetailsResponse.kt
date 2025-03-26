package com.example.wishlistapp.ui.theme.services

data class AppListResponse(val applist: AppList)

data class AppList(val apps: List<AppSummary>)

data class AppSummary(val appid: Int, val name: String)

data class GameDetailResponse(val success: Boolean, val data: GameData?)

data class GameData(
    val name: String,
    val is_free: Boolean,
    val price_overview: PriceOverview?,
    val header_image: String,
    val detailed_description: String
)

data class PriceOverview(
    val final: Int, // en centavos
    val initial: Int, // en centavos
    val discount_percent: Int,
    val initial_formatted: String,
    val final_formatted: String
)
