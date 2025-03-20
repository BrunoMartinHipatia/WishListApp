package com.example.wishlistapp.ui.theme.component

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wishlistapp.FriendsActivity
import com.example.wishlistapp.GroupActivity
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.UserService


@Composable
fun HomeScreen(userService: UserService, apiKey: String, steamUser: SteamUser?)  {
    val context = LocalContext.current
    TextButton(onClick = {

    }, modifier = Modifier.padding(30.dp)) {

        Text(text = "${steamUser?.personaname}", fontSize = 16.sp)
    }
    Column(modifier = Modifier.padding(100.dp)) {
        TextButton(onClick = {
            val intent = Intent(context, FriendsActivity::class.java).apply {

            }
            context.startActivity(intent)


        }) {

            Text(text = "amigos", fontSize = 16.sp)
        }
        TextButton(onClick = {
            val intent = Intent(context, GroupActivity::class.java).apply {

            }
            context.startActivity(intent)

        }) {

            Text(text = "mis grupos", fontSize = 16.sp)
        }
        TextButton(onClick = {}) {

            Text(text = "juegos", fontSize = 16.sp)
        }

    }
}