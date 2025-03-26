package com.example.wishlistapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.data.GroupDatabase
import com.example.wishlistapp.ui.theme.data.GroupRepository
import com.example.wishlistapp.ui.theme.data.SteamUser
import com.example.wishlistapp.ui.theme.data.SteamUserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SteamUserViewModel(application: Application)  : AndroidViewModel(application){

    private val _groupList = MutableStateFlow<List<SteamUser>>(emptyList())

    private  val groupList: StateFlow<List<SteamUser>> = _groupList

    private val dao = SteamUserDatabase.getDatabase(application).converterDao()

    private fun cargarGrupos(){

    }
    fun getFlashCards(callback: (List<SteamUser>) -> Unit) {
        viewModelScope.launch {
            callback(dao.getAllSteamUsers())
        }
    }
     fun addUser(user:SteamUser){
         viewModelScope.launch (Dispatchers.IO){
             dao.insertSteamUser(user)
         }
     }
}