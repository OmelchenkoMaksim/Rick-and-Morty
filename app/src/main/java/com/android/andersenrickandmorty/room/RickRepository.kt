package com.android.andersenrickandmorty.room

import com.android.andersenrickandmorty.models.CharacterModel
import com.android.andersenrickandmorty.models.EpisodeModel
import com.android.andersenrickandmorty.models.LocationModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RickRepository(
    private val daoCharacter: DaoCharacter,
    private val daoLocation: DaoLocation,
    private val daoEpisode: DaoEpisode
) {

    @Synchronized
    fun insertSingle(character: RoomCharacter): Flow<Unit> {
        return flow { emit(daoCharacter.insertSingleCharacters(character)) }
    }

    @Synchronized
    fun insertCharacters(characters: Collection<CharacterModel>): Flow<Unit> {
        val roomCharacters = characters.map {
            RoomCharacter(objectToJsonCharacterModel(it))
        }
        return flow { emit(daoCharacter.insertCharacters(roomCharacters)) }
    }

    @Synchronized
    fun takeCharacters(): Flow<List<CharacterModel>> {
        return daoCharacter.getCharacters().map { list ->
            list.map { jsonToCharacter(it.jsonCharacter) }
        }
    }


    @Synchronized
    fun insertEpisodes(episodes: Collection<EpisodeModel>): Flow<Unit> {
        val roomEpisodes = episodes.map {
            RoomEpisode(objectToJsonEpisodeModel(it))
        }
        return flow { emit(daoEpisode.insertEpisodes(roomEpisodes)) }
    }

    @Synchronized
    fun takeEpisodes(): Flow<List<EpisodeModel>> {
        return daoEpisode.getEpisodes().map { list ->
            list.map { jsonToEpisode(it.jsonEpisode) }
        }
    }


    @Synchronized
    fun insertLocations(locations: Collection<LocationModel>): Flow<Unit> {
        val roomLocations = locations.map { RoomLocation(objectToJsonLocationModel(it)) }
        return flow { emit(daoLocation.insertLocations(roomLocations)) }
    }

    @Synchronized
    fun takeLocations(): Flow<List<LocationModel>> {
        return daoLocation.getLocations().map { list ->
            list.map { jsonToLocation(it.jsonLocation) }
        }
    }

    private fun objectToJsonCharacterModel(obj: CharacterModel): String {
        return Gson().toJson(obj)
    }

    private fun objectToJsonLocationModel(obj: LocationModel): String {
        return Gson().toJson(obj)
    }

    private fun objectToJsonEpisodeModel(obj: EpisodeModel): String {
        return Gson().toJson(obj)
    }

    private fun jsonToCharacter(fromRoom: String): CharacterModel {
        return Gson().fromJson(fromRoom, CharacterModel::class.java)
    }

    private fun jsonToEpisode(fromRoom: String): EpisodeModel {
        return Gson().fromJson(fromRoom, EpisodeModel::class.java)
    }

    private fun jsonToLocation(fromRoom: String): LocationModel {
        return Gson().fromJson(fromRoom, LocationModel::class.java)
    }
}