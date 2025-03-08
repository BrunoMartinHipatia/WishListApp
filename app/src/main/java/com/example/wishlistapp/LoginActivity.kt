package com.example.wishlistapp

import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wishlistapp.ui.theme.component.LoginScreen
import com.example.wishlistapp.ui.theme.component.handleSteamResponse

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.wishlistapp.ui.theme.services.SteamUser
import com.example.wishlistapp.ui.theme.services.SteamUserService
import com.example.wishlistapp.ui.theme.services.UserServiceImpl

class LoginActivity : ComponentActivity() {
    private val userService = UserServiceImpl(SteamUserService.create())
    private val apiKey = BuildConfig.STEAM_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "onCreate called")

        // Obtener información del usuario antes de renderizar la pantalla de login
        lifecycleScope.launch {
            val steamId = getStoredSteamId()
            if (steamId != null) {
                val steamUser = userService.fetchSteamUser(steamId, apiKey)
                Log.d("LoginActivity", "Steam User Fetched: $steamUser")
                setContent {
                    LoginScreen(userService, apiKey, steamUser)
                }
            } else {
                setContent {
                    LoginScreen(userService, apiKey, null)
                }
            }
        }

        handleSteamLogin(intent)
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent)
        Log.d("LoginActivity", "onNewIntent received with data: ${intent.data}")
        intent?.let { handleSteamLogin(it) }
    }

    private fun handleSteamLogin(intent: Intent) {
        Log.d("SteamLogin", "handleSteamLogin called with intent: ${intent.data}")
        val uri: Uri? = intent.data
        if (uri != null && uri.scheme == "wishlistapp" && uri.host == "steamcallback") {
            handleSteamResponse(uri, this)
        }
    }

    private fun getStoredSteamId(): String? {
        return getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("SteamID", null)
    }
}