package com.android.andersenrickandmorty.room

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [RoomCharacter::class, RoomEpisode::class, RoomLocation::class],
    version = 1,
    exportSchema = false
)
abstract class RickRoomDatabase : RoomDatabase() {

    abstract fun daoCharacter(): DaoCharacter
    abstract fun daoLocation(): DaoLocation
    abstract fun daoEpisode(): DaoEpisode

}