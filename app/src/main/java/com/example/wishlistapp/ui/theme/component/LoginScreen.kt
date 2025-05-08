package com.example.wishlistapp.ui.theme.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

import com.example.wishlistapp.LoginActivity
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.UserService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(steamUser: SteamUser?) {
    Log.d("LoginScreen", "LoginScreen Composable iniciado")
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }

    // Lanzar animación al entrar
    LaunchedEffect(Unit) {
        visible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
         .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF213045),
                        Color(0xFF1A1A1F)
                    )
                )
            )
    ) {

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -80 })
        ) {
            Text(
                text = "¡Bienvenido a Steam App!",
                modifier = Modifier.padding(30.dp),
                fontSize = 25.sp
            )
            Image(
                painter = painterResource(id = R.drawable.fotologin),
                contentDescription = "Comprar",
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(400.dp)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -80 })
        ) {
            Text(
                text = "Para iniciar sesión, necesitas una cuenta de Steam!",
                modifier = Modifier.padding(30.dp),
                fontSize = 20.sp
            )
        }

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
        Text(
            text = "O",
            modifier = Modifier.padding(top = 20.dp).align(Alignment.CenterHorizontally),
            fontSize = 25.sp
        )
        Button(
            onClick = {
                Log.d("LoginScreen", "Abriendo página de crear cuenta de Steam")
                val steamSignupUrl = "https://store.steampowered.com/join/"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(steamSignupUrl))
                context.startActivity(intent)
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = "Crear cuenta de Steam")
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
