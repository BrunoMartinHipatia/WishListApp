package com.example.wishlistapp

import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wishlistapp.ui.theme.component.LoginScreen
import com.example.wishlistapp.ui.theme.component.handleSteamResponse

import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.example.wishlistapp.ui.theme.component.HomeScreen
import kotlinx.coroutines.launch
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.SteamUserService

import com.example.wishlistapp.ui.theme.services.UserServiceImpl
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {
    private val userService = UserServiceImpl(SteamUserService.create())
    private val apiKey = BuildConfig.STEAM_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "onCreate called")

        // Obtener información del usuario antes de renderizar la pantalla de login
        val firestore = FirebaseFirestore.getInstance()

        lifecycleScope.launch {
            val steamId = getStoredSteamId()
            if (steamId != null) {
                val steamUser = userService.fetchSteamUser(steamId, apiKey)
                val userMap = hashMapOf(
                    "steamId" to steamUser?.steamid,
                    "name" to steamUser?.personaname,
                    "image" to steamUser?.avatarfull,
                    "gruposAceptados" to steamUser?.gruposAceptados,
                    "gruposNoAceptados" to steamUser?.gruposNoAceptados,
                )



                firestore.collection("users").document(steamUser?.steamid!!)
                    .get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            // Si el usuario no existe, lo creamos
                            firestore.collection("users").document(steamUser.steamid).set(userMap)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Usuario creado en Firestore: ${steamUser.personaname}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error al registrar usuario en Firestore", e)
                                }
                        } else {
                            Log.d("Firestore", "El usuario ${steamUser.personaname} ya existe en Firestore")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error al comprobar existencia del usuario", e)
                    }



                Log.d("LoginActivity", "Steam User Fetched: $steamUser")
                setContent {
                    HomeScreen(userService, apiKey, steamUser)
                }
            } else {
                setContent {
                    LoginScreen( null)
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