package com.example.wishlistapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wishlistapp.viewmodel.GamesViewModel
// GamesViewModelFactory.kt
class GamesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GamesViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
