package com.example.wishlistapp.ui.theme.component

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.TextFieldDefaults
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ComponentActivity
import coil.compose.rememberAsyncImagePainter
import com.example.wishlistapp.GroupActivity
import com.example.wishlistapp.GroupsActivity
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.FriendService
import com.example.wishlistapp.ui.theme.services.SteamGame
import com.example.wishlistapp.viewmodel.GroupViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@SuppressLint("ContextCastToActivity", "RestrictedApi")
@Composable
fun GroupsScreen(
    viewModel: GroupViewModel,
    friendService: FriendService,
    apiKey: String,
    steamUser: SteamUser?
) {
    var showDialog by remember { mutableStateOf(false) }
    var showFriends by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var foundUser by remember { mutableStateOf<SteamUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var mostrarPendientes by remember { mutableStateOf(false) }
    var listItems by remember { mutableStateOf(true) }
    var tamañoItem by remember { mutableStateOf(208) }
    var tamañoLista by remember { mutableStateOf(2) }
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    // Recuperar amigos seleccionados desde el viewModel


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                steamUser?.let { currentUser ->
                    val documentSnapshot =
                        firestore.collection("users").document(currentUser.steamid).get().await()

                    if (documentSnapshot.exists()) {
                        val steamId = documentSnapshot.getString("steamid") ?: "Desconocido"
                        val name = documentSnapshot.getString("name") ?: "Desconocido"
                        val avatar = documentSnapshot.getString("image") ?: ""

                        val gruposNoAceptadosNombres =
                            documentSnapshot.get("gruposNoAceptados") as? List<String>
                                ?: emptyList()
                        val gruposAceptadosNombres =
                            documentSnapshot.get("gruposAceptados") as? List<String> ?: emptyList()

                        val gruposNoAceptados =
                            gruposNoAceptadosNombres.mapNotNull { fetchGroupByName(it) }
                        val gruposAceptados =
                            gruposAceptadosNombres.mapNotNull { fetchGroupByName(it) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF263140),
                        Color(0xFF1E1E1E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E),
                                Color(0xFF3A4651)
                            )
                        )
                    )
                    .padding(start = 20.dp)
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(steamUser?.avatarfull),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = "Mis Grupos",
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.CenterVertically),
                    color = Color.White
                )
            }

            if (!showDialog) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Row(modifier = Modifier.align(Alignment.End)) {
                        foundUser?.let { user ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 40.dp)
                                    .padding(top = 12.dp, end = 12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.group),
                                    contentDescription = "Grupo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            if (user.gruposNoAceptados.isNotEmpty()) {
                                                mostrarPendientes = true
                                            }
                                        }
                                )
                                if (user.gruposNoAceptados.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)

                                            .align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = user.gruposNoAceptados.size.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }

                            Image(
                                painter = painterResource(id = if (listItems) R.drawable.grid else R.drawable.list),
                                contentDescription = "Cambiar Vista",
                                modifier = Modifier
                                    .clickable {
                                        listItems = !listItems
                                        tamañoItem = if (listItems) 208 else 100
                                        tamañoLista = if (listItems) 2 else 1
                                    }
                                    .size(40.dp)
                                    .padding(top = 15.dp, end = 15.dp)
                            )
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(tamañoLista),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp, start = 6.dp, end = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        foundUser?.let { user ->
                            items(user.gruposAceptados) { group ->
                                GroupItem(
                                    group = group,
                                    steamUser = steamUser,
                                    aceptado = true,
                                    modifier = Modifier.size(tamañoItem.dp), tamaño = tamañoItem
                                )
                            }
                        } ?: item {
                            Text(
                                text = "No se encontró información del usuario",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (!showDialog) {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .width(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF263140),
                                Color(0xFF2F3030)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Crear Grupo",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        val activity = (context as? ComponentActivity)
        // Aquí se carga la lista de amigos seleccionados y se pasa al diálogo
        var imageUri by remember { mutableStateOf<Uri?>(null) }

        if (showDialog) {

            CrearGrupoDialog(
                showDialog = showDialog,
                showFriends = showFriends,
                onDismiss = { showDialog = false },
                onCreateGroup = { _, _, _ -> }, // ya no hace falta lógica aquí
                onAddUsers = { showFriends = true },
                viewModel = viewModel,
                friendService = friendService,
                apiKey = apiKey,
                steamUser = steamUser,
                selectedFriends = viewModel.selectedFriends
            )

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
            val listaUsuariosPendientes =
                documentSnapshot.get("listaUsuariosPendientes") as? List<Map<*, *>> ?: emptyList()
            val listaUsuarios =
                documentSnapshot.get("listaUsuarios") as? List<Map<*, *>> ?: emptyList()

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
        SteamUser(
            id = 0,
            steamid = steamId,
            personaname = personaname,
            avatarfull = avatarfull,
            gruposAceptados = emptyList(),
            gruposNoAceptados = emptyList()
        )
    } else {
        null
    }
}

@SuppressLint("ContextCastToActivity", "RestrictedApi")
@Composable
fun GroupItem(
    group: Group,
    steamUser: SteamUser?,
    aceptado: Boolean,
    modifier: Modifier = Modifier,
    tamaño: Int
) {
    val listaNombres = group.listaUsuarios.joinToString(", ") { it.personaname }
    val context = LocalContext.current
    val activity = (context as? ComponentActivity)

    Card(
        modifier = modifier
            .padding(4.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                val intent = Intent(context, GroupActivity::class.java).apply {
                    putExtra("grupo", group)
                }
                context.startActivity(intent)
                activity?.finish()
            }, colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)), // Fondo oscuro
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),

            ) {
            // 🔸 Icono tres puntos arriba a la derecha


            Spacer(modifier = Modifier.weight(1f))
            // 🔹 Nombre del grupo

            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef =
                storageRef.child("group_images/${group.nombre}.jpg") // o el nombre exacto del archivo
            var imageUrl by remember { mutableStateOf<String?>(null) }
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                imageUrl = uri.toString()

            }


            if (imageUrl != null && tamaño == 100) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Imagen del grupo",
                        contentScale = ContentScale.Crop, // <- Esto hace que se recorte bien
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )


                    Spacer(modifier = Modifier.width(12.dp)) // Espacio entre imagen y texto

                    Column {
                        Text(
                            text = group.nombre,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Usuarios: $listaNombres",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }


            } else {
//                Row(
//                     horizontalArrangement = Arrangement.End
//                ) {
//                    Text("⋯", color = Color.White, fontSize = 20.sp)
//                }
                Text(
                    text = group.nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,

                    modifier = Modifier
                        .fillMaxWidth()

                )

                // 🔹 Lista de usuarios justo debajo
                Text(
                    text = "Usuarios: $listaNombres",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()

                )
            }


            // 🔹 Botones si el grupo no está aceptado
            if (!aceptado) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Image(painter = painterResource(id = R.drawable.accept),
                        contentDescription = "Aceptar",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                if (steamUser != null) {
                                    addUserToAcceptedGroups(steamUser, group.nombre)
                                    val intent = Intent(context, GroupActivity::class.java).apply {
                                        putExtra("grupo", group)
                                    }
                                    context.startActivity(intent)
                                    activity?.finish()
                                }
                            })
                    Image(painter = painterResource(id = R.drawable.decline),
                        contentDescription = "Rechazar",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                if (steamUser != null) {
                                    removeGroupFromUserAndUpdateGroup(
                                        steamUser.steamid, group.nombre
                                    )
                                    val intent = Intent(context, GroupsActivity::class.java).apply {
                                        putExtra("grupo", group)
                                    }
                                    context.startActivity(intent)
                                    activity?.finish()
                                }
                            })
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
        val gruposNoAceptados =
            userSnapshot.get("gruposNoAceptados") as? MutableList<String> ?: mutableListOf()
        val listaUsuariosPendientes =
            groupSnapshot.get("listaUsuariosPendientes") as? MutableList<Map<String, Any>>
                ?: mutableListOf()

        Log.d("los no aceptados", gruposNoAceptados.toString())

        // 🟢 Filtrar y eliminar el grupo por nombre en `gruposNoAceptados`
        val newGruposNoAceptados = gruposNoAceptados.filterNot { it == groupName }

        // 🟢 Filtrar y eliminar el usuario de `listaUsuariosPendientes`
        val newListaUsuariosPendientes =
            listaUsuariosPendientes.filterNot { it["steamid"] == steamId }

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
        Log.d("MI SteamUser", steamUser.personaname + " " + groupName)
    };
    firestore.runTransaction { transaction ->
        val userSnapshot = userRef?.let { transaction.get(it) }
        val groupSnapshot = transaction.get(groupRef)

        // 🟢 Obtener las listas actuales como **List<String>**, ya que ahora solo guardamos nombres de grupos
        val gruposNoAceptados =
            userSnapshot?.get("gruposNoAceptados") as? MutableList<String> ?: mutableListOf()
        val gruposAceptados =
            userSnapshot?.get("gruposAceptados") as? MutableList<String> ?: mutableListOf()

        // 🟢 Obtener listas de usuarios dentro del grupo
        val listaUsuariosPendientes =
            groupSnapshot.get("listaUsuariosPendientes") as? MutableList<Map<String, Any>>
                ?: mutableListOf()
        val listaUsuarios =
            groupSnapshot.get("listaUsuarios") as? MutableList<Map<String, Any>> ?: mutableListOf()

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

            val newListaUsuariosPendientes =
                listaUsuariosPendientes.filterNot { it["steamid"] == steamUser?.steamid }
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


@SuppressLint("RestrictedApi")
@Composable
fun CrearGrupoDialog(
    showDialog: Boolean,
    showFriends: Boolean,
    onDismiss: () -> Unit,
    onCreateGroup: (String, String, List<SteamUser>) -> Unit,
    onAddUsers: () -> Unit,
    viewModel: GroupViewModel,
    friendService: FriendService,
    apiKey: String,
    steamUser: SteamUser?,
    selectedFriends: List<SteamUser>
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var showFriendsDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val activity = (context as? ComponentActivity)
    val firestore = FirebaseFirestore.getInstance()

    if (showDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000)) // fondo semitransparente
        ) {
            Image(
                painter = painterResource(id = R.drawable.close_icon), // tu icono de cerrar
                contentDescription = "Cerrar",
      
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(48.dp)
                    .background(colorResource(id = R.color.gris),  shape = RoundedCornerShape(20.dp))
                    .clickable { onDismiss() }
            )

            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = colorResource(id = R.color.gris),
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            result.data?.data?.let { selectedUri ->
                                imageUri = selectedUri
                            }
                        }
                        Card(
                            modifier = Modifier
                                .size(200.dp)
                                .wrapContentSize(Alignment.Center)
                                .padding(bottom = 20.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_PICK).apply {
                                        type = "image/*"
                                    }

                                    launcher.launch(intent)
                                }
                        ) {
                            if (imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .align(Alignment.Center),
                                        painter = painterResource(id = R.drawable.arrowup),
                                        contentDescription = "Grupo"
                                    )
                                }
                            }
                        }

                        TextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = { Text("Nombre del grupo") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(32.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )

                        TextField(
                            value = groupDescription,
                            onValueChange = { groupDescription = it },
                            label = { Text("Descripción del grupo") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )

                        Button(
                            onClick = { showFriendsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.azul))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.user),
                                    contentDescription = "Left Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Agregar Users",
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.add),
                                    contentDescription = "Right Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF263140), Color(0xFF637FA6))
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            onClick = {
                                if (groupName.isNotBlank()) {
                                    if (imageUri != null) {
                                        uploadGroupImageToFirebase(
                                            imageUri = imageUri!!,
                                            groupName = groupName,
                                            onSuccess = { imageUrl ->
                                                val listaAmigos = selectedFriends.toMutableList()
                                                val listaYo = listOfNotNull(steamUser)
                                                val group =
                                                    Group(id = 0, groupName, listaYo, listaAmigos)
                                                viewModel.addGrupo(group)

                                                val groupData = hashMapOf(
                                                    "nombre" to groupName,
                                                    "imagen" to imageUrl,
                                                    "listaUsuarios" to listaYo,
                                                    "listaUsuariosPendientes" to listaAmigos.map { user ->
                                                        mapOf(
                                                            "steamid" to user.steamid,
                                                            "personaname" to user.personaname,
                                                            "avatarfull" to user.avatarfull
                                                        )
                                                    },
                                                    "listaJuegos" to emptyList<SteamGame>().map { juego ->
                                                        mapOf(
                                                            "listaInteresados" to emptyList<String>(),
                                                            "juego" to juego.name
                                                        )
                                                    }
                                                )

                                                val firestore = FirebaseFirestore.getInstance()
                                                val groupRef =
                                                    firestore.collection("groups")
                                                        .document(groupName)

                                                groupRef.get().addOnSuccessListener { document ->
                                                    if (document.exists()) {
                                                        Toast.makeText(
                                                            context,
                                                            "Ya existe un grupo con ese nombre",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {
                                                        groupRef.set(groupData)
                                                            .addOnSuccessListener {
                                                                Log.d(
                                                                    "Firestore",
                                                                    "Grupo creado exitosamente: $groupName"
                                                                )
                                                            }.addOnFailureListener { e ->
                                                            Log.e(
                                                                "Firestore",
                                                                "Error al crear grupo",
                                                                e
                                                            )
                                                        }

                                                        listaAmigos.forEach { user ->
                                                            firestore.collection("users")
                                                                .document(user.steamid).update(
                                                                    "gruposNoAceptados",
                                                                    FieldValue.arrayUnion(groupName)
                                                                )
                                                        }

                                                        listaYo.forEach { user ->
                                                            firestore.collection("users")
                                                                .document(user.steamid).update(
                                                                    "gruposAceptados",
                                                                    FieldValue.arrayUnion(groupName)
                                                                )
                                                        }

                                                        onDismiss()
                                                        val intent =
                                                            Intent(
                                                                context,
                                                                GroupsActivity::class.java
                                                            )
                                                        context.startActivity(intent)
                                                        activity?.finish()
                                                    }
                                                }.addOnFailureListener { e ->
                                                    Log.e(
                                                        "Firestore",
                                                        "Error al verificar si el grupo existe",
                                                        e
                                                    )
                                                }
                                            },
                                            onError = {
                                                Toast.makeText(
                                                    context,
                                                    "Error al subir imagen",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Selecciona una imagen para el grupo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },

                            ) {
                            Text("Crear Grupo", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Cancelar", color = Color.Gray)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {},
                modifier = Modifier.padding(top = 8.dp)
            ) }
    }

    if (showFriendsDialog) {
        AlertDialog(
            onDismissRequest = { showFriendsDialog = false },
            containerColor = colorResource(id = R.color.gris),
            text = {
                FriendsScreen(
                    viewModel = viewModel,
                    friendService = friendService,
                    apiKey = apiKey,
                    steamUser = steamUser,
                    boolean = true
                )
            },
            confirmButton = {
                Button(
                    onClick = { showFriendsDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3030))
                ) {
                    Text("Hecho", color = Color.White)
                }
            }
        )
    }
}


fun uploadGroupImageToFirebase(
    imageUri: Uri,
    groupName: String,
    onSuccess: (String) -> Unit,
    onError: () -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("group_images/$groupName.jpg")

    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }.addOnFailureListener {
                onError()
            }
        }
        .addOnFailureListener {
            onError()
        }
}

