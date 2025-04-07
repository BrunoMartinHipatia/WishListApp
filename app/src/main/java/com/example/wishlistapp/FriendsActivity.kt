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
import com.example.wishlistapp.ui.theme.services.FriendServiceImpl
import com.example.wishlistapp.ui.theme.services.SteamFriendService
import com.example.wishlistapp.ui.theme.services.SteamUserService
import com.example.wishlistapp.ui.theme.services.UserServiceImpl
import com.example.wishlistapp.viewmodel.GroupViewModel
import kotlinx.coroutines.launch

class FriendsActivity : ComponentActivity() {
    private val userService = UserServiceImpl(SteamUserService.create())
    private val friendService = FriendServiceImpl(SteamFriendService.create())
    private val apiKey = BuildConfig.STEAM_API_KEY
    private val boolean: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FriendsActivity", "onCreate called")

        // Obtener información del usuario antes de renderizar la pantalla de login
        lifecycleScope.launch {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val steamId = sharedPreferences.getString("SteamID", null)

            // Verifica si el steamId está disponible
            if (!steamId.isNullOrEmpty()) {
                Log.d("steamId", steamId)

                try {
                    // Fetch steam user and friends info
                    val steamUser = userService.fetchSteamUser(steamId, apiKey)
                    val steamFriends = friendService.fetchFriendList(steamId, apiKey)
                    Log.d("FriendsActivity", "Steam User Fetched: $steamUser")

                    // Verifica que los amigos de Steam sean válidos
                    if (steamFriends != null && steamFriends.isNotEmpty()) {
                        setContent {
                            val groupViewModel: GroupViewModel = viewModel()
                            // Pasa los datos al FriendsScreen
                            FriendsScreen(
                                viewModel = groupViewModel,
                                friendService = friendService,
                                apiKey = apiKey,
                                steamUser = steamUser,
                                boolean = boolean
                            )
                        }
                    } else {
                        Toast.makeText(this@FriendsActivity, "El perfil está privado", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    // Manejo de errores, por ejemplo, si falla la carga de datos
                    Log.e("FriendsActivity", "Error al obtener los datos de Steam", e)
                    Toast.makeText(this@FriendsActivity, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Si no se encuentra el SteamID, redirigir a la pantalla de login
                Toast.makeText(this@FriendsActivity, "Por favor, inicie sesión", Toast.LENGTH_LONG).show()
                // Aquí puedes redirigir a la pantalla de login
                // startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }
}
