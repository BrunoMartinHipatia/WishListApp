package com.example.wishlistapp.ui.theme.component

import android.app.Application
import android.app.appsearch.SearchResults
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.retrofit.RetrofitInstance
import com.example.wishlistapp.ui.theme.services.AppSummary
import com.example.wishlistapp.ui.theme.services.FriendService
import com.example.wishlistapp.ui.theme.services.SteamGame
import com.example.wishlistapp.ui.theme.services.toSteamGame
import com.example.wishlistapp.viewmodel.GamesViewModel
import com.example.wishlistapp.viewmodel.GamesViewModelFactory
import com.example.wishlistapp.viewmodel.GroupViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun GamesScreen(viewModel: GroupViewModel,
                friendService: FriendService,
                apiKey: String,
                steamUser: SteamUser?) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: GamesViewModel = viewModel(factory = GamesViewModelFactory(context))

    var searchQuery by remember { mutableStateOf("") }
    val filteredGames by remember { derivedStateOf { viewModel.filteredGames } }
    val filteredSearchedGames by remember { derivedStateOf { viewModel.filteredSearchedGames } }

    var selectedSteamGame by remember { mutableStateOf<SteamGame?>(null) }
    var selectedSearchedGame by remember { mutableStateOf<AppSummary?>(null) }

    // 🔹 Debounce del filtro de búsqueda
    LaunchedEffect(searchQuery) {
        delay(500)
        viewModel.searchGames(searchQuery)
    }

    when {
        selectedSteamGame != null -> {
            if (steamUser != null) {
                GameDetailScreen(game = selectedSteamGame!!, steamUser) {
                    selectedSteamGame = null
                }
            }
        }

        selectedSearchedGame != null -> {
            val searchedGame = selectedSearchedGame!!
            var detailedGame by remember { mutableStateOf<SteamGame?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(searchedGame.appid) {
                detailedGame = viewModel.getGameDetailsByAppId(searchedGame.appid)
                isLoading = false
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                detailedGame?.let {
                    if (steamUser != null) {
                        GameDetailScreen(game = it, steamUser) {
                            selectedSearchedGame = null
                        }
                    }
                } ?: run {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se pudo cargar el juego.", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { selectedSearchedGame = null }) {
                            Text("Volver")
                        }

                    }
                }
            }
        }

        else -> {
            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar juegos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                LazyColumn {
                    if (searchQuery.isNotBlank()) {
                        items(filteredSearchedGames) { game ->
                            GameCardLite(game) { selectedSearchedGame = game }
                        }
                    } else {
                        items(filteredGames) { game ->
                            GameCardFromJson(game) { selectedSteamGame = game }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardFromJson(game: SteamGame, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            game.headerImage?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Portada del juego",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(text = game.name, style = MaterialTheme.typography.titleMedium)

            if (game.isFree) {
                Text(text = "¡Gratis!", style = MaterialTheme.typography.bodyMedium)
            } else {
                game.price?.let {
                    Text(text = "Precio: ${it} €", style = MaterialTheme.typography.bodyMedium)
                }
                game.discountPercent?.takeIf { it > 0 }?.let {
                    Text(text = "Descuento: $it%", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
@Composable
fun GameDetailScreen(game: SteamGame, steamUser: SteamUser, onBack: () -> Unit) {
    var showGroupDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = game.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        game.headerImage?.let { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Header",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (game.isFree) {
            Text("¡Gratis!", style = MaterialTheme.typography.bodyMedium)
        } else {
            val discountPercent = game.discountPercent ?: 0
            val hasDiscount = discountPercent > 0
            val originalPrice = game.price ?: 0.0
            val discountedPrice = if (hasDiscount) originalPrice * (1 - discountPercent / 100.0) else null

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hasDiscount) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$discountPercent%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    if (hasDiscount) {
                        Text(
                            text = "${"%.2f".format(originalPrice)} €",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                        discountedPrice?.let {
                            Text(
                                text = "${"%.2f".format(it)} €",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Text(
                            text = "${"%.2f".format(originalPrice)} €",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stripHtml(game.detailed_description),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Volver")
            }

            Button(onClick = { showGroupDialog = true }) {
                Text("Comprar")
            }
        }

        BackHandler {
            onBack()
        }

        if (showGroupDialog) {
            AlertDialog(
                onDismissRequest = { showGroupDialog = false },
                title = { Text("Selecciona un grupo") },
                text = {
                    GroupSelectorDialog(steamUser, game.appid) {
                        showGroupDialog = false
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}
@Composable
fun GameCardLite(game: AppSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = game.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "App ID: ${game.appid}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
@Composable
fun GroupSelectorDialog(steamUser: SteamUser, juego: Int, onClose: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val grupos = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        grupos.clear()
        val doc = firestore.collection("users").document(steamUser.steamid).get().await()
        val gruposAceptados = doc.get("gruposAceptados") as? List<String> ?: emptyList()

        grupos.addAll(gruposAceptados)
    }

    LazyColumn {
        items(grupos) { grupoNombre ->
            Button(
                onClick = {
                    añadirJuegoAGrupo(grupoNombre, juego) {
                        Toast.makeText(context, "Juego añadido a $grupoNombre", Toast.LENGTH_SHORT).show()
                        onClose()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("Añadir a $grupoNombre")
            }
        }
    }
}

fun añadirJuegoAGrupo(groupName: String, appId: Int, onComplete: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    val juegoData = mapOf(
        "juego" to appId,
        "listaInteresados" to emptyList<String>() // o lo que uses para representar usuarios
    )

    firestore.collection("groups")
        .document(groupName)
        .update("listaJuegos", FieldValue.arrayUnion(juegoData))
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error añadiendo juego al grupo", e)
        }
}


fun stripHtml(input: String): String {
    return input.replace(Regex("<.*?>"), "").replace("&quot;", "\"").replace("&amp;", "&")
}

