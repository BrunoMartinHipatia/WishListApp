package com.example.wishlistapp.ui.theme.component

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.wishlistapp.FriendsActivity
import com.example.wishlistapp.GameActivity
import com.example.wishlistapp.GroupsActivity
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.services.UserService
@Composable
fun HomeScreen(userService: UserService, apiKey: String, steamUser: SteamUser?) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize().background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF263140),
                        Color(0xFF3E2765)
                    )
                )
            )

            .padding(16.dp)
    ) {


        Text(
            text = "🎮 Steam Group Cart",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        steamUser?.let {
            Text(
                text = "Bienvenido, ${it.personaname}",
                fontSize = 16.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PanelCard(
                title = "Amigos",
                modifier = Modifier.weight(1f),
                backgroundResId = R.drawable.friends,
                onClick = {
                    val intent = Intent(context, FriendsActivity::class.java)
                    context.startActivity(intent)
                }
            )

            PanelCard(
                title = "Grupos",
                modifier = Modifier.weight(1f),
                backgroundResId = R.drawable.friends,
                onClick = {
                    val intent = Intent(context, GroupsActivity::class.java)
                    context.startActivity(intent)
                }
            )

            PanelCard(
                title = "Juegos",
                modifier = Modifier.weight(1f),
                backgroundResId = R.drawable.friends,
                onClick = {
                    val intent = Intent(context, GameActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }

    }
}

@Composable
fun PanelCard(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundResId: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Imagen de fondo local
            Image(
                painter = painterResource(id = backgroundResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
            )

            // Texto encima de la imagen
            Text(
                text = title,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.zIndex(1f).padding(0.dp, 20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(
    @PreviewParameter(SteamUserPreviewProvider::class) user: SteamUser?
) {
    HomeScreen(
        userService = DummyUserService(),
        apiKey = "dummy-api-key",
        steamUser = user
    )
}
