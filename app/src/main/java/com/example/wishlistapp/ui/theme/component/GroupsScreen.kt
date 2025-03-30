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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ComponentActivity
import com.example.wishlistapp.GroupActivity
import com.example.wishlistapp.GroupsActivity
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.services.FriendService
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.SteamGame
import com.example.wishlistapp.viewmodel.GroupViewModel
import com.example.wishlistapp.viewmodel.SteamUserViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
@Composable
fun GroupsScreen(
    viewModel: GroupViewModel,
    friendService: FriendService,
    apiKey: String,
    steamUser: SteamUser?
) {
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var foundUser by remember { mutableStateOf<SteamUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                steamUser?.let { currentUser ->
                    val documentSnapshot = firestore.collection("users")
                        .document(currentUser.steamid)
                        .get()
                        .await()

                    if (documentSnapshot.exists()) {
                        val steamId = documentSnapshot.getString("steamid") ?: "Desconocido"
                        val name = documentSnapshot.getString("name") ?: "Desconocido"
                        val avatar = documentSnapshot.getString("image") ?: ""

                        // 🔹 Obtener nombres de grupos
                        val gruposNoAceptadosNombres = documentSnapshot.get("gruposNoAceptados") as? List<String> ?: emptyList()
                        val gruposAceptadosNombres = documentSnapshot.get("gruposAceptados") as? List<String> ?: emptyList()

                        // 🔹 Buscar información de los grupos
                        val gruposNoAceptados = gruposNoAceptadosNombres.mapNotNull { fetchGroupByName(it) }
                        val gruposAceptados = gruposAceptadosNombres.mapNotNull { fetchGroupByName(it) }

                        foundUser = SteamUser(
                            id = 0,
                            steamid = steamId,
                            personaname = name,
                            avatarfull = avatar,
                            gruposNoAceptados = gruposNoAceptados,
                            gruposAceptados = gruposAceptados
                        )
                    } else {
                        Log.d("Usuario no encontrado", "No se encontró el usuario en Firestore")
                    }
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error al obtener usuario", e)
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Mis Grupos", fontSize = 22.sp, modifier = Modifier.padding(bottom = 8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            foundUser?.let { user ->
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(user.gruposAceptados) { group ->
                        GroupItem(group, steamUser, true)
                    }
                }

                Text(text = "Grupos pendientes", fontSize = 22.sp, modifier = Modifier.padding(bottom = 8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(user.gruposNoAceptados) { group ->
                        GroupItem(group, steamUser, false)
                    }
                }
            } ?: Text(
                text = "No se encontró información del usuario",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (showDialog) {
            FriendsScreen(viewModel, friendService, apiKey, steamUser, true)
        } else {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            ) {
                Text("Crear Grupo")
            }
        }
    }
}

/**
 * 🔹 Busca un grupo por su nombre en Firestore
 */
suspend fun fetchGroupByName(groupName: String): Group? {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val documentSnapshot = firestore.collection("groups").document(groupName).get().await()
        if (documentSnapshot.exists()) {
            val nombre = documentSnapshot.getString("nombre") ?: return null
            val listaUsuariosPendientes = documentSnapshot.get("listaUsuariosPendientes") as? List<Map<*, *>> ?: emptyList()
            val listaUsuarios = documentSnapshot.get("listaUsuarios") as? List<Map<*, *>> ?: emptyList()

            val usuariosPendientes = listaUsuariosPendientes.mapNotNull {
                Log.d("la lsita de i", it.toString())
                mapToSteamUser(it)

            }
            val usuariosAceptados = listaUsuarios.mapNotNull { mapToSteamUser(it) }

            Group(
                id = 0,
                nombre = nombre,
                listaUsuarios = usuariosAceptados,
                listaUsuariosPendientes = usuariosPendientes
            )
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener grupo $groupName", e)
        null
    }
}

/**
 * 🔹 Convierte un mapa de Firestore a un SteamUser
 */
fun mapToSteamUser(userMap: Map<*, *>): SteamUser? {
    val steamId = userMap["steamid"] as? String
    val personaname = userMap["personaname"] as? String
    val avatarfull = userMap["avatarfull"] as? String


    return if (steamId != null && personaname != null && avatarfull != null) {
        Log.d("el user", personaname)
        SteamUser(id = 0, steamid = steamId, personaname = personaname, avatarfull = avatarfull, gruposAceptados = emptyList(), gruposNoAceptados = emptyList())
    } else {
        null
    }
}

@SuppressLint("ContextCastToActivity", "RestrictedApi")
@Composable
fun GroupItem(group: Group, steamUser: SteamUser?, aceptado: Boolean) {
    val listaNombres = group.listaUsuarios.joinToString(", ") { it.personaname }

    val context = LocalContext.current
    val activity = (LocalContext.current as? ComponentActivity)
    ;
    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp).clickable {
                val intent = Intent(context, GroupActivity::class.java).apply {
                    putExtra("grupo",group)
                }

                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // 🔹 Centra todo el contenido
        ) {
            Text(
                text = group.nombre,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp)) // 🔹 Espacio entre el título y la lista

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically, // 🔹 Asegura alineación vertical
                horizontalArrangement = Arrangement.SpaceBetween // 🔹 Distribuye elementos uniformemente
            ) {
                Text(
                    text = "Usuarios: $listaNombres",
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f) // 🔹 Hace que el texto ocupe el espacio necesario
                )

                // 🔹 Solo mostrar botones si el grupo NO está en "gruposAceptados"
                if (!aceptado) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // 🔹 Espacio entre imágenes
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.accept),
                            contentDescription = "Aceptar",
                            modifier = Modifier.clickable {
                                if (steamUser != null) {
                                    Log.d("el usuario", steamUser.personaname)
                                    addUserToAcceptedGroups(steamUser, group.nombre)
                                    val intent = Intent(context, GroupsActivity::class.java)
                                    context.startActivity(intent)
                                    activity?.finish()
                                }
                            }.size(40.dp),
                        )
                        Image(
                            painter = painterResource(id = R.drawable.decline),
                            contentDescription = "Rechazar",
                            modifier = Modifier.clickable {
                                if (steamUser != null) {
                                    Log.d("el usuario", steamUser.personaname)
                                    removeGroupFromUserAndUpdateGroup(steamUser.steamid, group.nombre)
                                    val intent = Intent(context, GroupsActivity::class.java)
                                    context.startActivity(intent)
                                    activity?.finish()
                                }
                            }.size(40.dp),
                        )
                    }
                }
            }
        }
    }
}
fun removeGroupFromUserAndUpdateGroup(steamId: String, groupName: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userRef = firestore.collection("users").document(steamId)
    val groupRef = firestore.collection("groups").document(groupName)

    firestore.runTransaction { transaction ->
        val userSnapshot = transaction.get(userRef)
        val groupSnapshot = transaction.get(groupRef)

        // 🟢 Obtener la lista de nombres de grupos en `gruposNoAceptados` (ahora es una lista de Strings)
        val gruposNoAceptados = userSnapshot.get("gruposNoAceptados") as? MutableList<String> ?: mutableListOf()
        val listaUsuariosPendientes = groupSnapshot.get("listaUsuariosPendientes") as? MutableList<Map<String, Any>> ?: mutableListOf()

        Log.d("los no aceptados", gruposNoAceptados.toString())

        // 🟢 Filtrar y eliminar el grupo por nombre en `gruposNoAceptados`
        val newGruposNoAceptados = gruposNoAceptados.filterNot { it == groupName }

        // 🟢 Filtrar y eliminar el usuario de `listaUsuariosPendientes`
        val newListaUsuariosPendientes = listaUsuariosPendientes.filterNot { it["steamid"] == steamId }

        // 🟢 Guardar las listas actualizadas
        transaction.update(userRef, "gruposNoAceptados", newGruposNoAceptados)
        transaction.update(groupRef, "listaUsuariosPendientes", newListaUsuariosPendientes)
    }.addOnSuccessListener {
        Log.d("Firestore", "Grupo eliminado correctamente")
    }.addOnFailureListener { e ->
        Log.e("Firestore", "Error al eliminar el grupo", e)
    }
}

fun addUserToAcceptedGroups(steamUser: SteamUser?, groupName: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userRef = steamUser?.let { firestore.collection("users").document(it.steamid) }
    val groupRef = firestore.collection("groups").document(groupName)
    if (steamUser != null) {
        Log.d("SteamUser", steamUser.personaname + " "+ groupName)
    };
    firestore.runTransaction { transaction ->
        val userSnapshot = userRef?.let { transaction.get(it) }
        val groupSnapshot = transaction.get(groupRef)

        // 🟢 Obtener las listas actuales como **List<String>**, ya que ahora solo guardamos nombres de grupos
        val gruposNoAceptados = userSnapshot?.get("gruposNoAceptados") as? MutableList<String> ?: mutableListOf()
        val gruposAceptados = userSnapshot?.get("gruposAceptados") as? MutableList<String> ?: mutableListOf()

        // 🟢 Obtener listas de usuarios dentro del grupo
        val listaUsuariosPendientes = groupSnapshot.get("listaUsuariosPendientes") as? MutableList<Map<String, Any>> ?: mutableListOf()
        val listaUsuarios = groupSnapshot.get("listaUsuarios") as? MutableList<Map<String, Any>> ?: mutableListOf()

        // 🟢 Si el grupo está en `gruposNoAceptados`, lo movemos a `gruposAceptados`
        if (gruposNoAceptados.contains(groupName)) {
            val newGruposNoAceptados = gruposNoAceptados.filterNot { it == groupName }
            val newGruposAceptados = gruposAceptados.toMutableList().apply { add(groupName) }

            // 🟢 Actualizar `gruposNoAceptados` y `gruposAceptados` en el usuario
            if (userRef != null) {
                transaction.update(userRef, "gruposNoAceptados", newGruposNoAceptados)
            }
            if (userRef != null) {
                transaction.update(userRef, "gruposAceptados", newGruposAceptados)
            }

            // 🟢 Mover el usuario de `listaUsuariosPendientes` a `listaUsuarios`
            val usuarioData = mapOf(
                "steamid" to (steamUser?.steamid ?: 0),
                "personaname" to (userSnapshot?.getString("name") ?: "Sin Nombre"),
                "avatarfull" to (userSnapshot?.getString("image") ?: "")
            ) as Map<String, Any>

            val newListaUsuariosPendientes = listaUsuariosPendientes.filterNot { it["steamid"] == steamUser?.steamid }
            val newListaUsuarios = listaUsuarios.toMutableList().apply { add(usuarioData) }

            transaction.update(groupRef, "listaUsuariosPendientes", newListaUsuariosPendientes)
            transaction.update(groupRef, "listaUsuarios", newListaUsuarios)
        }
    }.addOnSuccessListener {
        Log.d("Firestore", "Usuario aceptó el grupo correctamente y fue movido a listaUsuarios")
    }.addOnFailureListener { e ->
        Log.e("Firestore", "Error al actualizar grupos", e)
    }
}

    @SuppressLint("ContextCastToActivity", "RestrictedApi")
    @Composable
    fun CrearGrupo(viewModel: GroupViewModel, friendUsers: List<SteamUser>, boolean: Boolean, steamUser: SteamUser?) {
        var showDialog by remember { mutableStateOf(false) }
        var groupName by remember { mutableStateOf("") }
        val firestore = FirebaseFirestore.getInstance()
        val context = LocalContext.current
        val activity = (LocalContext.current as? ComponentActivity)
        val listaAmigos = arrayListOf<SteamUser>()
        val listaYo = arrayListOf<SteamUser>()
        friendUsers.forEach{user->
            listaAmigos.add(user)
        }

        if (steamUser != null) {
            listaYo.add(steamUser)
        }
        listaAmigos.removeAt(0)
        if (boolean) {
            Button(onClick = { showDialog = true }) {
                Text("Crear Grupo")
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nombre del Grupo") },
                text = {
                    TextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Ingrese el nombre del grupo") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                val group = Group(id = 0 ,groupName, listaYo, listaAmigos)
                                viewModel.addGrupo(group)
                                val groupData = hashMapOf(
                                    "nombre" to groupName,
                                    "listaUsuarios" to listaYo, // 🟢 Inicialmente vacío
                                    "listaUsuariosPendientes" to listaAmigos.map { user ->
                                        mapOf(
                                            "steamid" to user.steamid,
                                            "personaname" to user.personaname,
                                            "avatarfull" to user.avatarfull
                                        )
                                    },
                                    "listaJuegos" to emptyList<SteamGame>().map { juego->
                                        mapOf(
                                            "listaInteresados" to emptyList<String>(),
                                            "juego" to juego.name
                                        )

                                    }
                                )

                                val groupRef = firestore.collection("groups").document(groupName)

                                groupRef.get().addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        Toast.makeText(context, "Ya existe un grupo con ese nombre", Toast.LENGTH_LONG).show()
                                    } else {
                                        // 🟢 Si el grupo NO existe, lo crea
                                        groupRef.set(groupData)
                                            .addOnSuccessListener {
                                                Log.d("Firestore", "Grupo creado exitosamente: $groupName")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "Error al crear grupo", e)
                                            }

                                        // 🟢 Agregar grupo a `gruposNoAceptados` de cada usuario
                                        listaAmigos.forEach { user ->
                                            firestore.collection("users")
                                                .document(user.steamid)
                                                .update("gruposNoAceptados", FieldValue.arrayUnion(groupName))
                                        }
                                        listaYo.forEach { user ->
                                            firestore.collection("users")
                                                .document(user.steamid)
                                                .update("gruposAceptados", FieldValue.arrayUnion(groupName))
                                        }

                                        // 🟢 Finaliza la actividad después de la creación del grupo
                                        showDialog = false
                                        val intent = Intent(context, GroupsActivity::class.java)
                                        context.startActivity(intent)
                                        activity?.finish()
                                    }
                                }.addOnFailureListener { e ->
                                    Log.e("Firestore", "Error al verificar si el grupo existe", e)
                                }
                            }
                        }
                    ) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
