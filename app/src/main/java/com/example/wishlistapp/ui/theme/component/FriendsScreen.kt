package com.example.wishlistapp.ui.theme.component


import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.wishlistapp.GroupsActivity
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
                    SteamUser(0, steamId, name, avatar, gruposNoAceptados, gruposAceptados)
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
            CrearGrupo(viewModel, friendUsers, boolean, steamUser)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(friends!!) { friend ->
                    if (steamUser != null) {
                        FriendItem(
                            steamUser = friendDetails!![friend.steamid],
                            boolean = boolean,
                            friendUsers = friendUsers,
                            miSteamId =  steamUser.steamid

                        )
                    }
                }
            }

        }
    }
    var showList by remember { mutableStateOf(false) }
    Log.d("mi usuario", steamUser.toString())
}
@Composable
fun FriendItem(
    steamUser: SteamUser?,
    boolean: Boolean,
    friendUsers: MutableList<SteamUser>,
    miSteamId: String // 🔹 Pasamos el SteamID del usuario actual
) {
    if (steamUser == null || steamUser.personaname == "Desconocido") return

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var showGroupDialog by remember { mutableStateOf(false) }
    var gruposAceptados by remember { mutableStateOf<List<String>>(emptyList()) }

    // 🔹 UI de cada amigo
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
                // 🔹 Al pulsar, buscar grupos aceptados del usuario actual
                coroutineScope.launch {
                    firestore.collection("users").document(miSteamId).get()
                        .addOnSuccessListener { document ->
                            val grupoList = document.get("gruposAceptados") as? List<String> ?: emptyList()
                            gruposAceptados = grupoList
                            showGroupDialog = true
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al cargar grupos", Toast.LENGTH_SHORT).show()
                        }
                }
            }) {
                Text(text = "Añadir a grupo", fontSize = 14.sp)
            }
        } else {
            Checkbox(
                checked = friendUsers.contains(steamUser),
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        if (!friendUsers.contains(steamUser)) friendUsers.add(steamUser)
                    } else {
                        friendUsers.remove(steamUser)
                    }
                    Log.d("los friends", friendUsers.toString())
                }
            )
        }
    }

    // 🔹 Diálogo para mostrar los gruposAceptados
    if (showGroupDialog) {
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("Selecciona un grupo") },
            text = {
                Column {
                    if (gruposAceptados.isEmpty()) {
                        Text("No tienes grupos aceptados aún.")
                    } else {
                        gruposAceptados.forEach { groupName ->
                            TextButton(onClick = {
                                // Aquí puedes manejar la lógica de agregar al grupo
                                usuarioPendiente(groupName, steamUser, context)
                                showGroupDialog = false
                            }) {
                                Text(groupName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
fun usuarioPendiente(groupName: String, steamUser: SteamUser, context: Context) {
    val firestore = FirebaseFirestore.getInstance()
    val groupRef = firestore.collection("groups").document(groupName)

    groupRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            val listaPendientes = document.get("listaUsuariosPendientes") as? List<Map<String, Any>> ?: emptyList()
            val listaAceptados = document.get("listaUsuarios") as? List<Map<String, Any>> ?: emptyList()


            val yaEnPendientes = listaPendientes.any { it["steamid"] == steamUser.steamid }
            val yaEnAceptados = listaAceptados.any { it["steamid"] == steamUser.steamid }

            if (yaEnPendientes || yaEnAceptados) {
                Toast.makeText(context, "El usuario ya pertenece al grupo", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Si no está en ninguna lista, lo agregamos a listaUsuariosPendientes
            val userMap = mapOf(
                "steamid" to steamUser.steamid,
                "personaname" to steamUser.personaname,
                "avatarfull" to steamUser.avatarfull
            )
            val updatedList = listaPendientes.toMutableList().apply { add(userMap) }

            // 🔹 Añadir el grupo a `gruposNoAceptados` del usuario
            firestore.collection("users")
                .document(steamUser.steamid)
                .update("gruposNoAceptados", FieldValue.arrayUnion(groupName))

            // 🔹 Actualizar Firestore
            groupRef.update("listaUsuariosPendientes", updatedList)
                .addOnSuccessListener {
                    Log.d("Firestore", "Usuario añadido a listaUsuariosPendientes del grupo $groupName")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al actualizar listaUsuariosPendientes", e)
                }
        } else {
            Log.w("Firestore", "El grupo $groupName no existe.")
        }
    }.addOnFailureListener { e ->
        Log.e("Firestore", "Error al buscar el grupo $groupName", e)
    }
}
