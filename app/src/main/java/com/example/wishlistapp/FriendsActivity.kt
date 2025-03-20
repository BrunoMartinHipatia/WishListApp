package com.example.wishlistapp

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wishlistapp.ui.theme.component.LoginScreen
import com.example.wishlistapp.ui.theme.component.handleSteamResponse

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wishlistapp.ui.theme.component.FriendsScreen
import com.example.wishlistapp.ui.theme.component.HomeScreen
import com.example.wishlistapp.ui.theme.services.FriendService
import com.example.wishlistapp.ui.theme.services.FriendServiceImpl
import com.example.wishlistapp.ui.theme.services.SteamFriendService
import com.example.wishlistapp.ui.theme.services.SteamUserService
import kotlinx.coroutines.launch

import com.example.wishlistapp.ui.theme.services.UserServiceImpl
import com.example.wishlistapp.viewmodel.GroupViewModel

class FriendsActivity : ComponentActivity() {
    private val userService = UserServiceImpl(SteamUserService.create())
    private val friendService = FriendServiceImpl(SteamFriendService.create())
    private val apiKey = BuildConfig.STEAM_API_KEY
    private val boolean: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "onCreate called")

        // Obtener información del usuario antes de renderizar la pantalla de login
        lifecycleScope.launch {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val steamId = sharedPreferences.getString("SteamID", null) ?: ""

            if (steamId != null) {
                Log.d("steamId", steamId)
                val steamUser = userService. fetchSteamUser(steamId, apiKey)
                val steamFriends = friendService.fetchFriendList(steamId, apiKey)
                Log.d("LoginActivity", "Steam User Fetched: ${steamFriends}")
                setContent {
                    val groupViewModel: GroupViewModel = viewModel()

                    if (steamFriends != null) {
                        FriendsScreen(groupViewModel,friendService, apiKey, steamUser, boolean)
                    }else{
                        Toast.makeText(this@FriendsActivity, "El perfil está privado", Toast.LENGTH_LONG).show()
                    }
                }
            } else {

            }
        }

    }

    private fun getStoredSteamId(): String? {
        return getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("SteamID", null)
    }
}