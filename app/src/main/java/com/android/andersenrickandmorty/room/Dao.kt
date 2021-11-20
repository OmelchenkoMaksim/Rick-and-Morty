package com.android.andersenrickandmorty.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoCharacter {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleCharacters(character: RoomCharacter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertCharacters(list: List<RoomCharacter>)

    @Query("SELECT * FROM character")
    fun getCharacters(): Flow<List<RoomCharacter>>

}

@Dao
interface DaoEpisode {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertEpisodes(list: List<RoomEpisode>)

    @Query("SELECT * FROM episode")
    fun getEpisodes(): Flow<List<RoomEpisode>>
}

@Dao
interface DaoLocation {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertLocations(list: List<RoomLocation>)

    @Query("SELECT * FROM location")
    fun getLocations(): Flow<List<RoomLocation>>
}


@Entity(tableName = "character")
data class RoomCharacter(
    @PrimaryKey
    val jsonCharacter: String
)

@Entity(tableName = "episode")
data class RoomEpisode(
    @PrimaryKey
    val jsonEpisode: String
)

@Entity(tableName = "location")
data class RoomLocation(
    @PrimaryKey
    val jsonLocation: String
)