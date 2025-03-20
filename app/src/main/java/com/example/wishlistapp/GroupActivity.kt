package com.example.wishlistapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wishlistapp.ui.theme.component.FriendsScreen
import com.example.wishlistapp.ui.theme.component.GroupScreen
import com.example.wishlistapp.ui.theme.services.FriendServiceImpl
import com.example.wishlistapp.ui.theme.services.SteamFriendService
import com.example.wishlistapp.ui.theme.services.SteamUserService
import com.example.wishlistapp.ui.theme.services.UserServiceImpl

import com.example.wishlistapp.viewmodel.GroupViewModel
import kotlinx.coroutines.launch

class GroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Obtener el SteamID del usuario desde SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val steamId = sharedPreferences.getString("SteamID", null) ?: ""

        // 🔹 API Key de Steam (asegúrate de tenerla en un lugar seguro)
        val apiKey = BuildConfig.STEAM_API_KEY

        // 🔹 Crear instancias de los servicios necesarios
        val steamFriendService = SteamFriendService.create()
        val friendService = FriendServiceImpl(steamFriendService)
        val steamUserService = SteamUserService.create()
        val userService = UserServiceImpl(steamUserService)
        lifecycleScope.launch {
            val steamUser = userService.fetchSteamUser(steamId, apiKey)
            setContent {
                val groupViewModel: GroupViewModel = viewModel()

                GroupScreen(
                    viewModel = groupViewModel,
                    friendService = friendService,
                    apiKey = apiKey,
                    steamUser= steamUser,

                )
            }
        }

    }
}
