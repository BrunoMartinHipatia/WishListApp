package com.example.wishlistapp.ui.theme.data

class GroupsRepositoryImpl(private val dao: GroupDao): GroupRepository {
    override suspend fun insertResult(result: Group) {
        dao.insertGroup(result)
    }

    override suspend fun deleteResult(result: Group) {
        dao.deleteGroup(result)
    }
}