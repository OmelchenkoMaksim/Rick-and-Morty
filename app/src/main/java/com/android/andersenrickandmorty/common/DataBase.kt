package com.android.andersenrickandmorty.common

import com.android.andersenrickandmorty.models.CharacterModel
import com.android.andersenrickandmorty.models.EpisodeModel
import com.android.andersenrickandmorty.models.LocationModel

object DataBase {

    // variable contains network availability
    var online = true

    const val BASE_URL = "https://rickandmortyapi.com/"

    // filters queries
    const val CHARACTER_OPTION_NAME = "NAME"
    const val CHARACTER_OPTION_SPECIES = "SPECIES"
    const val CHARACTER_OPTION_STATUS = "STATUS"
    const val CHARACTER_OPTION_TYPE = "TYPE"
    const val CHARACTER_OPTION_GENDER = "GENDER"
    const val LOCATION_OPTION_NAME = "NAME"
    const val LOCATION_OPTION_TYPE = "TYPE"
    const val LOCATION_OPTION_DIMENSION = "DIMENSION"
    const val EPISODE_OPTION_NAME = "NAME"
    const val EPISODE_OPTION_EPISODE = "EPISODE"

    // main local base of elements
    val charactersList: LinkedHashSet<CharacterModel> = linkedSetOf()
    val locationsList: LinkedHashSet<LocationModel> = linkedSetOf()
    val episodesList: LinkedHashSet<EpisodeModel> = linkedSetOf()

    // check is filtering data or details data
    var useCharacterFilters: Boolean = false
    var useCharacterForDetails: Boolean = false
    val filteredDataCharacter: LinkedHashSet<CharacterModel> = linkedSetOf()
    val detailsDataCharacter: LinkedHashSet<CharacterModel> = linkedSetOf()

    var useLocationFilters: Boolean = false
    val filteredDataLocation: LinkedHashSet<LocationModel> = linkedSetOf()

    var useEpisodeFilters: Boolean = false
    var useEpisodeForDetails: Boolean = false
    val filteredDataEpisode: LinkedHashSet<EpisodeModel> = linkedSetOf()
    val detailsDataEpisode: LinkedHashSet<EpisodeModel> = linkedSetOf()

    // it contains all filters for elements in local base
    val charactersStatusesOffline = sortedSetOf<String>()
    val charactersSpeciesOffline = sortedSetOf<String>()
    val charactersGendersOffline = sortedSetOf<String>()
    val charactersTypesOffline = sortedSetOf<String>()
    val locationsTypesOffline = sortedSetOf<String>()
    val locationsDimensionsOffline = sortedSetOf<String>()
    val episodesEpisodesOffline = sortedSetOf<String>()

    // storage for filter queries
    val filterCharactersOptions: MutableMap<String, String> = mutableMapOf(
        CHARACTER_OPTION_NAME to "",
        CHARACTER_OPTION_SPECIES to "",
        CHARACTER_OPTION_STATUS to "",
        CHARACTER_OPTION_TYPE to "",
        CHARACTER_OPTION_GENDER to ""
    )
    val filterLocationsOptions: MutableMap<String, String> = mutableMapOf(
        LOCATION_OPTION_NAME to "",
        LOCATION_OPTION_TYPE to "",
        LOCATION_OPTION_DIMENSION to ""
    )
    val filterEpisodesOptions: MutableMap<String, String> = mutableMapOf(
        EPISODE_OPTION_NAME to "",
        EPISODE_OPTION_EPISODE to ""
    )

    fun resetAllPagingAttributes() {
        useCharacterFilters = false
        useCharacterForDetails = false

        useLocationFilters = false

        useEpisodeFilters = false
        useEpisodeForDetails = false

        clearEpisodeOptions()
        clearCharacterOptions()
        clearLocationOptions()

        filteredDataCharacter.clear()
        filteredDataLocation.clear()
        filteredDataEpisode.clear()
    }

    fun clearCharacterOptions() {
        filterCharactersOptions[CHARACTER_OPTION_NAME] = ""
        filterCharactersOptions[CHARACTER_OPTION_STATUS] = ""
        filterCharactersOptions[CHARACTER_OPTION_SPECIES] = ""
        filterCharactersOptions[CHARACTER_OPTION_TYPE] = ""
        filterCharactersOptions[CHARACTER_OPTION_GENDER] = ""
    }

    fun clearLocationOptions() {
        filterLocationsOptions[LOCATION_OPTION_NAME] = ""
        filterLocationsOptions[LOCATION_OPTION_TYPE] = ""
        filterLocationsOptions[LOCATION_OPTION_DIMENSION] = ""
    }

    fun clearEpisodeOptions() {
        filterEpisodesOptions[EPISODE_OPTION_NAME] = ""
        filterEpisodesOptions[EPISODE_OPTION_EPISODE] = ""
    }

    // search by id use for open details fragment
    fun findCharacterById(characterId: Int): CharacterModel {
        return charactersList.first { characterModel ->
            characterModel.id == characterId
        }
    }

    fun findLocationById(locationId: Int): LocationModel {
        return locationsList.first { locationModel ->
            locationModel.id == locationId
        }
    }

    fun findEpisodeById(episodeId: Int): EpisodeModel {
        return episodesList.first { episodeModel ->
            episodeModel.id == episodeId
        }
    }

    // methods below create lists by queries
    fun getCharactersByOptions(options: MutableMap<String, String>): LinkedHashSet<CharacterModel> {
        val list = charactersList.asSequence().filter {
            it.name.contains(options[CHARACTER_OPTION_NAME]!!, true)
        }.filter {
            it.type.contains(options[CHARACTER_OPTION_TYPE]!!, true)
        }.filter {
            it.species.contains(options[CHARACTER_OPTION_SPECIES]!!, true)
        }.filter {
            it.gender.contains(options[CHARACTER_OPTION_GENDER]!!, true)
        }.filter {
            it.status.contains(options[CHARACTER_OPTION_STATUS]!!, true)
        }
        val result = linkedSetOf<CharacterModel>()
        result.addAll(list)
        return result
    }

    fun getLocationsByOptions(options: MutableMap<String, String>): LinkedHashSet<LocationModel> {
        val list = locationsList.asSequence().filter {
            it.name.contains(options[LOCATION_OPTION_NAME]!!, true)
        }.filter {
            it.type.contains(options[LOCATION_OPTION_TYPE]!!, true)
        }.filter {
            it.dimension.contains(options[LOCATION_OPTION_DIMENSION]!!, true)
        }
        val result = linkedSetOf<LocationModel>()
        result.addAll(list)
        return result
    }

    fun getEpisodesByOptions(options: MutableMap<String, String>): LinkedHashSet<EpisodeModel> {
        val list = episodesList.asSequence().filter {
            it.name.contains(options[EPISODE_OPTION_NAME]!!, true)
        }.filter {
            it.episode.contains(options[EPISODE_OPTION_EPISODE]!!, true)
        }
        val result = linkedSetOf<EpisodeModel>()
        result.addAll(list)
        return result
    }

    // hold actual set of filters for offline mode
    fun updateFilters() {
        locationsList.forEach {
            locationsTypesOffline.add(it.type)
            locationsDimensionsOffline.add(it.dimension)
        }
        charactersList.forEach {
            charactersStatusesOffline.add(it.status)
            charactersSpeciesOffline.add(it.species)
            charactersTypesOffline.add(it.type)
            charactersGendersOffline.add(it.gender)
        }
        episodesList.forEach {
            episodesEpisodesOffline.add(it.episode)
        }
    }
}