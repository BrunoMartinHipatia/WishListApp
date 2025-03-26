package com.example.wishlistapp.ui.theme.di


import android.app.Application
import androidx.room.Room
import com.example.wishlistapp.ui.theme.data.GroupDatabase
import com.example.wishlistapp.ui.theme.data.GroupRepository
import com.example.wishlistapp.ui.theme.data.GroupsRepositoryImpl
import com.example.wishlistapp.ui.theme.data.SteamUserDatabase
import com.example.wishlistapp.ui.theme.data.SteamUserRepository
import com.example.wishlistapp.ui.theme.data.SteamUserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConverterDatabase(app: Application) : GroupDatabase {
        return Room.databaseBuilder(
            app,
            GroupDatabase::class.java,
            "group_data_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideConverterRepository(db : GroupDatabase) : GroupRepository
    {
        return GroupsRepositoryImpl(db.converterDao())
    }
    @Provides
    @Singleton
    fun provideConverterDatabaseUser(app: Application) : SteamUserDatabase {
        return Room.databaseBuilder(
            app,
            SteamUserDatabase::class.java,
            "steamuser_data_database"
        ).build()
    }
    @Provides
    @Singleton
    fun provideConverterRepositoryUser(db : SteamUserDatabase) : SteamUserRepository
    {
        return SteamUserRepositoryImpl(db.converterDao())
    }





}