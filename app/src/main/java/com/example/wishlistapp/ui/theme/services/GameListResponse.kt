package com.example.wishlistapp.ui.theme.services

data class GameListResponse(
    val applist: AppList
)


data class SteamGame(
    val appid: Int,
    val name: String,
    val price: Double?,
    val priceFinal: Double?,
    val discountPercent: Int?,
    val headerImage: String?,
    val isFree: Boolean,
    val detailed_description: String
)
fun GameData.toSteamGame(appid: Int): SteamGame {
    return SteamGame(
        appid = appid,
        name = name,
        price = price_overview?.initial?.div(100.0),
        priceFinal = price_overview?.final?.div(100.0),
        discountPercent = price_overview?.discount_percent,
        headerImage = header_image,
        isFree = is_free,
        detailed_description = detailed_description
    )
}