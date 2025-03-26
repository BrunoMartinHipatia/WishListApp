package com.example.wishlistapp.ui.theme.component

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ComponentActivity
import coil.compose.rememberAsyncImagePainter
import com.example.wishlistapp.GroupsActivity
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.services.FriendService
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.SteamGame
import com.example.wishlistapp.viewmodel.GroupViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.*
import com.example.wishlistapp.ui.theme.retrofit.RetrofitInstance
import com.example.wishlistapp.ui.theme.services.toSteamGame

@Composable
fun GroupScreen(

    grupo: Group
) {
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val integrantesGrupo = remember { mutableStateListOf<SteamUser>() }

    LaunchedEffect(grupo) {
        coroutineScope.launch {
            buscarUsuariosGrupoConInfo(grupo) { users ->
                integrantesGrupo.clear()
                integrantesGrupo.addAll(users)
                isLoading = false
            }
        }
    }
    val juegosGrupo = remember { mutableStateListOf<SteamGame>() }

    LaunchedEffect(grupo.nombre) {
        buscarJuegosDelGrupoConDetalles(grupo) { juegos ->
            juegosGrupo.clear() // Limpiar antes de añadir nuevos
            juegosGrupo.addAll(juegos)
            juegos.forEach {
                Log.d("Juego del grupo", it.name)
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // 🔹 Columna izquierda: Usuarios
        Column(
            modifier = Modifier

                .padding(end = 8.dp) // Espacio entre columnas
        ) {
            Text("Users", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(integrantesGrupo) { user ->
                        Column(
                            modifier = Modifier

                                .padding(vertical = 8.dp),

                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(user.avatarfull),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(4.dp)
                            )
                            Text(user.personaname, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // 🔹 Columna derecha: Juegos
        Column(
            modifier = Modifier

                .padding(start = 8.dp)
        ) {
            Text("Games", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(juegosGrupo) { juego ->
                    Card(
                        modifier = Modifier

                            .padding(vertical = 4.dp).width(100.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(juego.name, style = MaterialTheme.typography.bodyMedium)

                        }
                    }
                }

            }

        }
        Column(
            modifier = Modifier

                .padding(start = 8.dp)
        ) {
            Text("Price", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(juegosGrupo) { juego ->
                    Card(
                        modifier = Modifier

                            .padding(vertical = 4.dp).width(100.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row (modifier = Modifier.padding(8.dp)) {
                                if(juego.discountPercent!! >0){
                                    Log.d("discountPercent", juego.discountPercent.toString())
                                    Text(juego.price.toString()+"€",    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textDecoration = TextDecoration.LineThrough
                                    ))
                                    Text(juego.priceFinal.toString()+"€", style = MaterialTheme.typography.bodyMedium)

                                }else{
                                    Text(juego.price.toString()+"€", style = MaterialTheme.typography.bodyMedium)
                                }
                    }
                }

            }

        }


        }

    }


}
fun buscarUsuariosGrupoConInfo(
    grupo: Group,
    onResult: (List<SteamUser>) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val groupRef = firestore.collection("groups").document(grupo.nombre)

    groupRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            val listaUsers = document.get("listaUsuarios") as? List<Map<String, Any>> ?: emptyList()
            val usuarios = listaUsers.mapNotNull {
                val steamid = it["steamid"] as? String
                val name = it["personaname"] as? String
                val avatar = it["avatarfull"] as? String

                if (steamid != null && name != null && avatar != null) {
                    SteamUser(
                        id = 0,
                        steamid = steamid,
                        personaname = name,
                        avatarfull = avatar,
                        gruposAceptados = emptyList(),
                        gruposNoAceptados = emptyList()
                    )
                } else null
            }

            onResult(usuarios)
        } else {
            onResult(emptyList())
        }
    }.addOnFailureListener {
        onResult(emptyList())
    }
}
fun buscarJuegosDelGrupoConDetalles(
    grupo: Group,
    onResult: (List<SteamGame>) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val groupRef = firestore.collection("groups").document(grupo.nombre)

    groupRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            val listaJuegos = document.get("listaJuegos") as? List<Any> ?: emptyList()
            Log.d("mis juegos", listaJuegos.toString())
            val appIds = listaJuegos.mapNotNull {
                when (it) {
                    is Long -> it.toInt()
                    is Int -> it
                    else -> null
                }
            }

            // ⚠️ Llamadas a la API en corrutina
            val steamApi = RetrofitInstance.api

            // 🔁 Usamos corrutina para hacer llamadas secuenciales o paralelas
            val scope = kotlinx.coroutines.GlobalScope // Usa tu viewModelScope o similar si prefieres

            scope.launch {
                val juegos = mutableListOf<SteamGame>()

                for (appId in appIds) {
                    try {
                        val response = steamApi.getGameDetails(appId)
                        val gameData = response[appId.toString()]?.data
                        gameData?.let {
                            juegos.add(it.toSteamGame(appId))
                        }
                    } catch (e: Exception) {
                        Log.e("SteamAPI", "Error con appId $appId", e)
                    }
                }

                // 🟢 Devuelve los juegos cuando estén listos
                withContext(Dispatchers.Main) {
                    onResult(juegos)
                }
            }

        } else {
            onResult(emptyList())
        }
    }.addOnFailureListener {
        Log.e("Firestore", "Error al obtener juegos del grupo", it)
        onResult(emptyList())
    }
}
