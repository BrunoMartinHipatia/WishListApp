package com.example.wishlistapp

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.wishlistapp.ui.theme.component.GamesScreen
import com.example.wishlistapp.ui.theme.component.GroupScreen
import com.example.wishlistapp.ui.theme.services.FriendServiceImpl
import com.example.wishlistapp.ui.theme.services.SteamFriendService
import com.example.wishlistapp.ui.theme.services.SteamUserService
import com.example.wishlistapp.ui.theme.services.UserServiceImpl
import com.example.wishlistapp.viewmodel.GamesViewModel
import com.example.wishlistapp.viewmodel.GroupViewModel
import kotlinx.coroutines.launch

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiKey = BuildConfig.STEAM_API_KEY
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val steamFriendService = SteamFriendService.create()
        val friendService = FriendServiceImpl(steamFriendService)
        val steamUserService = SteamUserService.create()
        val steamId = sharedPreferences.getString("SteamID", null) ?: ""
        val userService = UserServiceImpl(steamUserService)


        lifecycleScope.launch {
            val steamUser = userService.fetchSteamUser(steamId, apiKey)
            setContent {
                val groupViewModel: GroupViewModel = viewModel()

                    GamesScreen(
                        viewModel = groupViewModel,
                        friendService = friendService,
                        apiKey = apiKey,
                        steamUser= steamUser

                    )
                }

            }
        }

}
