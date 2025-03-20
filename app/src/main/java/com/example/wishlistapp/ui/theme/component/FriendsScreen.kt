package com.example.wishlistapp.ui.theme.component

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.wishlistapp.GroupActivity
import com.example.wishlistapp.ui.theme.data.Friend
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.services.FriendService
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.viewmodel.GroupViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
@Composable
fun FriendsScreen(
    viewModel: GroupViewModel,
    friendService: FriendService,
    apiKey: String,
    steamUser: SteamUser?,
    boolean: Boolean
) {
    var friends by remember { mutableStateOf<List<Friend>?>(null) }
    var friendDetails by remember { mutableStateOf<Map<String, SteamUser>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // 🔹 Estado que almacena la lista de usuarios seleccionados
    val friendUsers = remember { mutableStateListOf<SteamUser>() }
    if (steamUser != null && friendUsers.none { it.steamid == steamUser.steamid }) {
        friendUsers.add(steamUser) // 🔹 Agrega el usuario actual si no está en la lista
    }

    val userList = remember { mutableStateListOf<SteamUser>() } // 🔹 Usa mutableStateListOf()

    // 🔹 Obtener usuarios de Firestore y filtrar solo los amigos
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val firestore = FirebaseFirestore.getInstance()
            try {
                val snapshot = firestore.collection("users").get().await()
                val fetchedUsers = snapshot.documents.mapNotNull { document ->
                    val steamId = document.id
                    val name = document.getString("name") ?: "Desconocido"
                    val avatar = document.getString("image") ?: ""
                    val gruposNoAceptados = document.get("gruposNoAceptados") as? List<Group> ?: emptyList()
                    val gruposAceptados = document.get("gruposAceptados") as? List<Group> ?: emptyList()
                    SteamUser(steamId, name, avatar, gruposNoAceptados, gruposAceptados)
                }

                userList.clear() // 🔹 Limpiamos antes de agregar nuevos
                userList.addAll(fetchedUsers)

                Log.d("userList", userList.toString())
            } catch (e: Exception) {
                Log.e("Firestore", "Error al obtener usuarios", e)
            }
        }
    }

    // 🔹 Obtener la lista de amigos
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            steamUser?.steamid?.let { steamId ->
                friends = friendService.fetchFriendList(steamId, apiKey)

                // 🔹 Filtramos `userList` para quedarnos solo con los amigos
                val filteredFriends = friends?.mapNotNull { friend ->
                    userList.find { it.steamid == friend.steamid }
                } ?: emptyList()

                friendDetails = filteredFriends.associateBy { it.steamid }

                Log.d("Filtered Friends", friendDetails.toString())
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Lista de Amigos", fontSize = 24.sp, modifier = Modifier.padding(16.dp))

        if (friends == null || friendDetails == null) {
            CircularProgressIndicator()
        } else {
            CrearGrupo(viewModel, friendUsers, boolean)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(friends!!) { friend ->
                    FriendItem(
                        steamUser = friendDetails!![friend.steamid],
                        boolean = boolean,
                        friendUsers = friendUsers // 🔹 Pasamos el estado a `FriendItem`
                    )
                }
            }

        }
    }
}
@Composable
fun FriendItem(
    steamUser: SteamUser?,
    boolean: Boolean,
    friendUsers: MutableList<SteamUser>
) {
    // 🔹 Si el usuario es nulo o tiene el nombre "Desconocido", no lo mostramos
    if (steamUser == null || steamUser.personaname == "Desconocido") {
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(steamUser.avatarfull),
            contentDescription = "Avatar de Steam",
            modifier = Modifier.size(50.dp).padding(end = 8.dp)
        )

        // 🔹 Centra el nombre del usuario
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = steamUser.personaname,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        if (!boolean) {
            TextButton(onClick = {
                // Acción de añadir a grupo
            }) {
                Text(text = "Añadir a grupo", fontSize = 14.sp)
            }
        } else {
            Checkbox(
                checked = friendUsers.contains(steamUser),
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        if (!friendUsers.contains(steamUser)) {
                            friendUsers.add(steamUser) // 🔹 Agrega el usuario a la lista
                        }
                    } else {
                        friendUsers.remove(steamUser) // 🔹 Elimina el usuario de la lista
                    }
                    Log.d("los friends", friendUsers.toString())
                }
            )
        }
    }
}
