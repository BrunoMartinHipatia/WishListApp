package com.example.wishlistapp.ui.theme.component

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ComponentActivity
import coil.compose.rememberAsyncImagePainter
import com.example.wishlistapp.GroupActivity
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.services.FriendService
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.UserService
import com.example.wishlistapp.viewmodel.GroupViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun GroupScreen(
    viewModel: GroupViewModel,
    friendService: FriendService,

    apiKey: String,

     steamUser: SteamUser?,

) {
    var showDialog by remember { mutableStateOf(false) }
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.getFlashCards {
            groups = it
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Mis Grupos", fontSize = 22.sp, modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
            items(groups) { group ->
                GroupItem(group)
            }
        }



        if (showDialog) {

            FriendsScreen(viewModel,friendService, apiKey, steamUser, true)

        }else {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            ) {
                Text("Crear Grupo")
            }
        }
    }
}

@Composable
fun GroupItem(group: Group) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.nombre, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(text = "Usuarios: ${group.listaUsuarios.size}", fontSize = 14.sp)
        }
    }
}
@SuppressLint("ContextCastToActivity", "RestrictedApi")
@Composable
fun CrearGrupo(viewModel: GroupViewModel, friendUsers: List<SteamUser>, boolean: Boolean) {
    var showDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val activity = (LocalContext.current as? ComponentActivity)

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
                            val groupData = hashMapOf(
                                "nombre" to groupName,
                                "listaUsuarios" to friendUsers.map { user ->
                                    mapOf(
                                        "steamid" to user.steamid,
                                        "personaname" to user.personaname,
                                        "avatarfull" to user.avatarfull
                                    )
                                }
                            )
                            val grupo = Group(
                                id = 0,  // 🔹 Dejar que Room genere el ID automáticamente
                                nombre = groupName,
                                listaUsuarios = friendUsers
                            )
                            viewModel.addGrupo(grupo) //
                            // 🔹 Agregar grupo a Firebase
                            val groupRef = firestore.collection("groups").document(groupName)

                            groupRef.get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    Toast.makeText(context, "Ya existe un grupo con ese nombre", Toast.LENGTH_LONG).show()
                                } else {
                                    // Si no existe, crearlo
                                    showDialog = false
                                    groupRef.set(groupData)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "Grupo creado exitosamente: $groupName")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore", "Error al crear grupo", e)
                                        }
                                    friendUsers.forEach { user ->
                                        firestore.collection("users")
                                            .document(user.steamid)
                                            .update("gruposNoAceptados", FieldValue.arrayUnion(groupName))
                                            .addOnSuccessListener {
                                                Log.d("FirestoreUpdate", "Grupo agregado a gruposNoAceptados de ${user.steamid}")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("FirestoreUpdate", "Error al actualizar usuario ${user.steamid}", e)
                                            }
                                    }
                                    val intent = Intent(context, GroupActivity::class.java)
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
