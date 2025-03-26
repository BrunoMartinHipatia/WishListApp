package com.example.wishlistapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.ui.theme.services.*
import com.example.wishlistapp.ui.theme.retrofit.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class GamesViewModel(private val context: Context) : ViewModel() {

    private val _allGames = mutableStateListOf<SteamGame>()
    val filteredGames = mutableStateListOf<SteamGame>()

    private val allJsonGames = mutableStateListOf<AppSummary>() // 🔹 juegos sin detalles
    val filteredSearchedGames = mutableStateListOf<AppSummary>()

    lateinit var gameList: AppListResponse

    init {
        loadGamesFromAssets()
    }

    private fun loadGamesFromAssets() {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    context.assets.open("juegos.json").bufferedReader().use { it.readText() }
                }

                gameList = Gson().fromJson(json, AppListResponse::class.java)

                // Guardamos todos los juegos sin detalles
                val allGames = gameList.applist.apps
                    .filter { it.name.lowercase() != "desconocido" }

                allJsonGames.addAll(allGames) // 🔹 usados en la búsqueda

                val selectedGames = allGames.shuffled().take(50)

                selectedGames.forEach { app ->
                    try {
                        val response = RetrofitInstance.api.getGameDetails(app.appid)
                        val gameData = response[app.appid.toString()]?.data

                        if (gameData != null && gameData.name.isNotBlank()) {
                            val steamGame = gameData.toSteamGame(app.appid)
                            _allGames.add(steamGame)
                            filteredGames.add(steamGame)
                        }
                    } catch (e: Exception) {
                        Log.e("SteamDetails", "Error al obtener detalles de ${app.name}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("SteamAssets", "Error al leer juegos desde JSON", e)
            }
        }
    }

    fun searchGames(query: String) {
        val result = if (query.isBlank()) {
            emptyList()
        } else {
            allJsonGames.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        filteredSearchedGames.clear()
        filteredSearchedGames.addAll(result)
    }

    suspend fun getGameDetailsByAppId(appId: Int): SteamGame? {
        return try {
            val response = RetrofitInstance.api.getGameDetails(appId)
            val gameData = response[appId.toString()]?.data
            gameData?.toSteamGame(appId)
        } catch (e: Exception) {
            Log.e("GameDetails", "Error al obtener detalles para $appId", e)
            null
        }
    }
}
