package com.example.wishlistapp.ui.theme.data

import androidx.room.Query

interface GroupRepository {

suspend fun insertResult(result: Group)
suspend fun deleteResult(result: Group)


}