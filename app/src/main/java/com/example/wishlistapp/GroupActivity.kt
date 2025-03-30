package com.example.wishlistapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wishlistapp.ui.theme.component.GroupScreen
import com.example.wishlistapp.ui.theme.component.GroupsScreen
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.services.FriendServiceImpl
import com.example.wishlistapp.ui.theme.services.SteamFriendService
import com.example.wishlistapp.ui.theme.services.SteamUserService
import com.example.wishlistapp.ui.theme.services.UserServiceImpl

import com.example.wishlistapp.viewmodel.GroupViewModel
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch

class GroupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val grupo: Group? = intent.getParcelableExtra("grupo")


        lifecycleScope.launch {
            val steamId = getStoredSteamId()
            setContent {

                if (grupo != null) {
                    if (steamId != null) {
                        GroupScreen(

                            grupo = grupo, steamId = steamId
                        )
                    }
                }

            }
        }

    }

    private fun getStoredSteamId(): String? {
        return getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("SteamID", null)
    }
}
