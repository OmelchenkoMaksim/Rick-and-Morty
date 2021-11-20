package com.android.andersenrickandmorty.room

import android.app.Application
import androidx.room.Room


class RickApplication : Application() {

    val database: RickRoomDatabase by lazy {
        Room.databaseBuilder(this, RickRoomDatabase::class.java, "database")
            .allowMainThreadQueries()
            .build()
    }
    val repository by lazy {
        RickRepository(database.daoCharacter(), database.daoLocation(), database.daoEpisode())
    }
}