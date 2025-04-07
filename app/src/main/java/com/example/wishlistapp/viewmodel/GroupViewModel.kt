package com.example.wishlistapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.data.GroupDatabase
import com.example.wishlistapp.ui.theme.data.GroupRepository
import com.example.wishlistapp.ui.theme.data.SteamUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupViewModel(application: Application)  : AndroidViewModel(application){

    private val _groupList = MutableStateFlow<List<Group>>(emptyList())

    private  val groupList: StateFlow<List<Group>> = _groupList

    private val dao = GroupDatabase.getDatabase(application).converterDao()
    private val _selectedFriends = mutableStateListOf<SteamUser>()
    val selectedFriends: List<SteamUser> get() = _selectedFriends


    fun addFriendToSelection(friend: SteamUser) {
        if (!selectedFriends.contains(friend)) {
            _selectedFriends.add(friend)
        }
    }

    // Función para eliminar un amigo de la lista seleccionada
    fun removeFriendFromSelection(friend: SteamUser) {
        _selectedFriends.remove(friend)
    }

    // Función para crear el grupo y agregarlo

     fun addGrupo(grupo:Group){
         viewModelScope.launch (Dispatchers.IO){
             dao.insertGroup(grupo)
         }
     }
}