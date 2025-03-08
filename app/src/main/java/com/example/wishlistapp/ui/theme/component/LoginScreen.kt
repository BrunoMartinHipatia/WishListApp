package com.example.wishlistapp.ui.theme.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

import com.example.wishlistapp.LoginActivity
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.theme.services.SteamUser
import com.example.wishlistapp.ui.theme.services.UserService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(userService: UserService, apiKey: String, steamUser: SteamUser?) {
    Log.d("LoginScreen", "LoginScreen Composable iniciado")
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(30.dp)
    ) {
        val userMap = hashMapOf(
            "steamId" to steamUser?.steamid,
            "name" to steamUser?.personaname,
            "image" to steamUser?.avatarfull
        )
        Text(text = "Iniciar sesión", modifier = Modifier.padding(30.dp), fontSize = 30.sp)
        firestore.collection("users").document(steamUser?.steamid!!).set(userMap)
            .addOnSuccessListener {
                Log.d("LoginScreen", "Usuario registrado en Firestore: ${steamUser.personaname}")
            }
            .addOnFailureListener { e ->
                Log.e("LoginScreen", "Error al registrar usuario en Firestore", e)
            }


        if (steamUser != null) {
            // Mostrar la información del usuario
            Text(text = "Nombre: ${steamUser.personaname}", fontSize = 18.sp, modifier = Modifier.padding(10.dp))
            Text(text = "Steam ID: ${steamUser.steamid}", fontSize = 18.sp, modifier = Modifier.padding(10.dp))
            Image(
                painter = rememberAsyncImagePainter(steamUser.avatarfull),
                contentDescription = "Avatar de Steam",
                modifier = Modifier.size(100.dp).padding(10.dp)
            )
        } else {
            // Botón para iniciar sesión en Steam
            Button(
                onClick = {
                    Log.d("LoginScreen", "Iniciando autenticación con Steam")
                    val steamLoginUrl = SteamOpenIDService.getSteamLoginUrl("https://wishlistbruno.web.app/steamcallback")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(steamLoginUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(text = "Iniciar sesión con Steam")
            }
        }
    }
}

fun handleSteamResponse(uri: Uri, context: Context) {
    Log.d("SteamLogin", "handleSteamResponse llamado con URI: $uri")
    val steamId = uri.getQueryParameter("steamid")
    if (steamId != null) {
        Log.d("SteamLogin", "Usuario autenticado con SteamID: $steamId")
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("SteamID", steamId)
            .apply()
        Log.d("SteamLogin", "SteamID guardado en SharedPreferences")
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    } else {
        Log.e("SteamLogin", "Error al autenticar con Steam")
    }
}
