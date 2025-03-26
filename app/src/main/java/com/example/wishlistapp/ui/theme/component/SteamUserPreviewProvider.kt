package com.example.wishlistapp.ui.theme.component

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.wishlistapp.ui.theme.data.SteamUser

class SteamUserPreviewProvider : PreviewParameterProvider<SteamUser?> {
    override val values: Sequence<SteamUser?>
        get() = sequenceOf(
            SteamUser(
                id = 0,
                steamid = "123456789",
                personaname = "JugadorX",
                avatarfull = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/xx/xx_full.jpg",
                emptyList(), emptyList()
            )
        )
}
