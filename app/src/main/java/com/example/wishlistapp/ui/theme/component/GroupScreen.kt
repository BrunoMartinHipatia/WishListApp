package com.example.wishlistapp.ui.theme.component

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.unit.times
import com.example.wishlistapp.ui.theme.data.GameFirebase
import com.example.wishlistapp.ui.theme.data.SteamGameConInteresados
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.*
import com.example.wishlistapp.ui.theme.retrofit.RetrofitInstance
import com.example.wishlistapp.ui.theme.services.toSteamGame
import android.content.res.Configuration
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun GroupScreen(
    grupo: Group,
    steamId: String
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val integrantesGrupo = remember { mutableStateListOf<SteamUser>() }
    val juegosGrupo = remember { mutableStateListOf<SteamGameConInteresados>() }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var juegoAEliminar by remember { mutableStateOf<Int?>(null) }

    // 🔄 Cargar usuarios
    LaunchedEffect(grupo) {
        coroutineScope.launch {
            buscarUsuariosGrupoConInfo(grupo) { users ->
                integrantesGrupo.clear()
                integrantesGrupo.addAll(users)
                isLoading = false
            }
        }
    }

    // 🔄 Cargar juegos
    LaunchedEffect(grupo.nombre) {
        buscarJuegosDelGrupoConDetalles(grupo) { juegos ->
            juegosGrupo.clear()
            juegosGrupo.addAll(juegos)
        }
    }

    if (isPortrait) {
        // 📱 Modo vertical: Usuarios arriba
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            UsuariosSection(integrantesGrupo, isLoading,isPortrait)
            Spacer(modifier = Modifier.height(16.dp))
            JuegosSection(
                grupo = grupo,
                juegosGrupo = juegosGrupo,
                integrantesGrupo = integrantesGrupo,
                steamId = steamId,
                context = context,
                showDialog = showDialog,
                onDialogChange = { showDialog = it },
                juegoAEliminar = juegoAEliminar,
                onJuegoEliminarChange = { juegoAEliminar = it },
                coroutineScope = coroutineScope,
                isPortrait = isPortrait
            )
        }
    } else {
        // 💻 Modo horizontal: Usuarios a la izquierda
        Row(Modifier.fillMaxSize().padding(16.dp)) {
            UsuariosSection(integrantesGrupo, isLoading, isPortrait, Modifier.width(120.dp).fillMaxHeight().padding(end = 12.dp))
            JuegosSection(
                grupo = grupo,
                juegosGrupo = juegosGrupo,
                integrantesGrupo = integrantesGrupo,
                steamId = steamId,
                context = context,
                showDialog = showDialog,
                onDialogChange = { showDialog = it },
                juegoAEliminar = juegoAEliminar,
                onJuegoEliminarChange = { juegoAEliminar = it },
                coroutineScope = coroutineScope,
                isPortrait = isPortrait
            )
        }
    }
}
@Composable
fun UsuariosSection(
    integrantesGrupo: List<SteamUser>,
    isLoading: Boolean,
    isPortrait: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Users", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (isPortrait) {
                // 📱 Vertical: usuarios en fila horizontal
                LazyRow {
                    items(integrantesGrupo) { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(user.avatarfull),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(4.dp)
                            )
                            Text(
                                user.personaname,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // 💻 Horizontal: usuarios en columna vertical
                LazyColumn {
                    items(integrantesGrupo) { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(user.avatarfull),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(4.dp)
                            )
                            Text(
                                user.personaname,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


fun buscarUsuariosGrupoConInfo(
    grupo: Group, onResult: (List<SteamUser>) -> Unit
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
@Composable
fun JuegosSection(
    grupo: Group,
    juegosGrupo: List<SteamGameConInteresados>,
    integrantesGrupo: List<SteamUser>,
    steamId: String,
    context: android.content.Context,
    showDialog: Boolean,
    onDialogChange: (Boolean) -> Unit,
    juegoAEliminar: Int?,
    onJuegoEliminarChange: (Int?) -> Unit,
    coroutineScope: CoroutineScope,
    isPortrait: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Juegos del grupo", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // 🧭 Cabecera
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("NAME", Modifier.weight(0.4f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Text("Price", Modifier.weight(0.2f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Text("Price U", Modifier.weight(0.2f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Text("Users", Modifier.weight(0.3f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Text("Participate", Modifier.weight(0.3f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Text("Accions", Modifier.weight(0.3f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }

        Divider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(juegosGrupo) { juego ->
                val discountPercent = juego.game.discountPercent ?: 0
                val price = juego.game.price ?: 0.0
                val finalPrice = if (discountPercent > 0) price * (1 - discountPercent / 100.0) else price
                val interesados = integrantesGrupo.filter { it.steamid in juego.interesados }
                val yaParticipa = juego.interesados.contains(steamId)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 🎮 Imagen + nombre
                    Column(
                        modifier = Modifier.weight(0.4f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        juego.game.headerImage?.let { imageUrl ->
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(100.dp)
                            )
                        }
                        Text(
                            text = juego.game.name,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2
                        )
                    }

                    // 💶 Precio
                    Column(Modifier.weight(0.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (price != finalPrice) {
                            Text("%.2f€".format(price), textDecoration = TextDecoration.LineThrough)
                            Text("%.2f€".format(finalPrice))
                        } else {
                            Text("%.2f€".format(finalPrice))
                        }
                    }

                    // 💰 Precio por interesado
                    Column(Modifier.weight(0.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                        val porPersona = if (interesados.isNotEmpty()) finalPrice / interesados.size else finalPrice
                        Text("%.2f€".format(porPersona))
                    }

                    // 👥 Interesados
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(0.3f).height(3 * 40.dp),
                        userScrollEnabled = false
                    ) {
                        items(interesados.take(6)) { user ->
                            Box(
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(user.avatarfull),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(0.9f)
                                )
                            }
                        }
                    }

                    // ✅ Participar toggle
                    Image(
                        painter = painterResource(
                            id = if (yaParticipa) R.drawable.checked else R.drawable.notchecked
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(0.3f)
                            .size(48.dp)
                            .clickable {
                                participar(grupo, steamId, juego.game.appid) {
                                    coroutineScope.launch {
                                        buscarJuegosDelGrupoConDetalles(grupo) { nuevos ->
                                            (juegosGrupo as MutableList).apply {
                                                clear()
                                                addAll(nuevos)
                                            }
                                        }
                                    }
                                }
                            }
                    )

                    // 🛒 Acciones: Comprar & Eliminar
                    if(isPortrait){
                        Column (
                            modifier = Modifier.weight(0.3f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.buy),
                                contentDescription = "Comprar",
                                modifier = Modifier
                                    .clickable {
                                        val url = "https://store.steampowered.com/app/${juego.game.appid}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                    .size(40.dp)
                            )
                            Spacer(Modifier.width(8.dp))

                            Image(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Eliminar",
                                modifier = Modifier
                                    .clickable {
                                        onJuegoEliminarChange(juego.game.appid)
                                        onDialogChange(true)
                                    }
                                    .size(40.dp)
                            )
                        }
                    }else{
                        Row(
                            modifier = Modifier.weight(0.3f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.buy),
                                contentDescription = "Comprar",
                                modifier = Modifier
                                    .clickable {
                                        val url = "https://store.steampowered.com/app/${juego.game.appid}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                    .size(40.dp)
                            )
                            Spacer(Modifier.width(8.dp))

                            Image(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Eliminar",
                                modifier = Modifier
                                    .clickable {
                                        onJuegoEliminarChange(juego.game.appid)
                                        onDialogChange(true)
                                    }
                                    .size(40.dp)
                            )
                        }
                    }


                }

                Divider()
            }
        }

        // 🧾 Diálogo de confirmación
        if (showDialog && juegoAEliminar != null) {
            AlertDialog(
                onDismissRequest = { onDialogChange(false) },
                title = { Text("¿Eliminar juego?") },
                text = { Text("¿Estás seguro de que quieres eliminar este juego del grupo?") },
                confirmButton = {
                    TextButton(onClick = {
                        quitarJuegoAGrupo(grupo, juegoAEliminar!!) {
                            buscarJuegosDelGrupoConDetalles(grupo) { nuevosJuegos ->
                                (juegosGrupo as MutableList).apply {
                                    clear()
                                    addAll(nuevosJuegos)
                                }
                            }
                            onDialogChange(false)
                            onJuegoEliminarChange(null)
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onDialogChange(false)
                        onJuegoEliminarChange(null)
                    }) { Text("Cancelar") }
                }
            )
        }
    }
}

fun participar(
    grupo: Group,
    steamId: String,
    appId: Int,
    onComplete: () -> Unit // <-- nueva función callback
) {
    val firestore = FirebaseFirestore.getInstance()
    val groupRef = firestore.collection("groups").document(grupo.nombre)

    groupRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            val juegos = document.get("listaJuegos") as? MutableList<Map<String, Any>> ?: mutableListOf()

            val juegoActualizado = juegos.map { juegoMap ->
                val juegoId = (juegoMap["juego"] as? Long)?.toInt() ?: return@map juegoMap
                if (juegoId == appId) {
                    val interesados = (juegoMap["listaInteresados"] as? MutableList<String>)?.toMutableList() ?: mutableListOf()

                    if (interesados.contains(steamId)) {
                        interesados.remove(steamId)
                    } else {
                        interesados.add(steamId)
                    }

                    mapOf(
                        "juego" to appId,
                        "listaInteresados" to interesados
                    )
                } else {
                    juegoMap
                }
            }

            groupRef.update("listaJuegos", juegoActualizado)
                .addOnSuccessListener {
                    Log.d("Firestore", "Actualización completada para el juego $appId")
                    onComplete() // <-- refrescar datos
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al actualizar listaInteresados", e)
                    onComplete()
                }

        } else {
            Log.w("Firestore", "Grupo no encontrado: ${grupo.nombre}")
            onComplete()
        }
    }.addOnFailureListener {
        Log.e("Firestore", "Error al leer grupo", it)
        onComplete()
    }
}


fun buscarJuegosDelGrupoConDetalles(
    grupo: Group,
    onResult: (List<SteamGameConInteresados>) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val groupRef = firestore.collection("groups").document(grupo.nombre)

    groupRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            val listaJuegos = document.get("listaJuegos") as? List<Map<String, Any>> ?: emptyList()
            val juegosConInteresados = mutableListOf<SteamGameConInteresados>()
            val steamApi = RetrofitInstance.api

            kotlinx.coroutines.GlobalScope.launch {
                for (juegoMap in listaJuegos) {
                    val appId = (juegoMap["juego"] as? Long)?.toInt() ?: continue
                    val interesados = (juegoMap["listaInteresados"] as? List<String>) ?: emptyList()

                    try {
                        val response = steamApi.getGameDetails(appId)
                        val gameData = response[appId.toString()]?.data
                        gameData?.let {
                            juegosConInteresados.add(
                                SteamGameConInteresados(it.toSteamGame(appId), interesados)
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("SteamAPI", "Error con appId $appId", e)
                    }
                }

                withContext(Dispatchers.Main) {
                    onResult(juegosConInteresados)
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
fun quitarJuegoAGrupo(
    grupo: Group,
    appId: Int,
    onRefresh: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val groupRef = firestore.collection("groups").document(grupo.nombre)

    groupRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            val listaJuegos = document.get("listaJuegos") as? List<Map<String, Any>> ?: emptyList()

            // Quitamos el juego con ese appId
            val nuevaLista = listaJuegos.filterNot {
                val id = (it["juego"] as? Long)?.toInt()
                id == appId
            }

            // Actualizamos Firestore con la nueva lista
            groupRef.update("listaJuegos", nuevaLista)
                .addOnSuccessListener {
                    Log.d("Firestore", "Juego $appId eliminado del grupo")
                    onRefresh() // Llamamos a la función para recargar los datos
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error actualizando listaJuegos", e)
                }
        }
    }.addOnFailureListener {
        Log.e("Firestore", "Error obteniendo grupo", it)
    }
}



