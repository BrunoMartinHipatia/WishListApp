package com.example.wishlistapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.ui.theme.data.Group
import com.example.wishlistapp.ui.theme.data.GroupDatabase
import com.example.wishlistapp.ui.theme.data.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupViewModel(application: Application)  : AndroidViewModel(application){

    private val _groupList = MutableStateFlow<List<Group>>(emptyList())

    private  val groupList: StateFlow<List<Group>> = _groupList

    private val dao = GroupDatabase.getDatabase(application).converterDao()

    private fun cargarGrupos(){

    }
    fun getFlashCards(callback: (List<Group>) -> Unit) {
        viewModelScope.launch {
            callback(dao.getAllGrupos())
        }
    }
     fun addGrupo(grupo:Group){
         viewModelScope.launch (Dispatchers.IO){
             dao.insertGroup(grupo)
         }
     }
}