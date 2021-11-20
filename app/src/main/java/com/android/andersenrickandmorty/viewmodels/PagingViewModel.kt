package com.android.andersenrickandmorty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.models.CharacterModel
import com.android.andersenrickandmorty.models.EpisodeModel
import com.android.andersenrickandmorty.models.LocationModel
import com.android.andersenrickandmorty.retrofit.RetrofitApi
import com.android.andersenrickandmorty.retrofit.RetrofitInstance
import com.android.andersenrickandmorty.room.RickRepository
import com.android.andersenrickandmorty.sources.CharacterPagingSource
import com.android.andersenrickandmorty.sources.EpisodePagingSource
import com.android.andersenrickandmorty.sources.LocationPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class PagingViewModel(
    private val rickRepository: RickRepository,
    private val database: DataBase
) : ViewModel() {

    private val retroService: RetrofitApi = RetrofitInstance.retrofitGetInstance()

    fun initDatabase() {
        viewModelScope.launch {
            rickRepository.takeCharacters().collectLatest {
                database.charactersList.addAll(it)
            }
        }
        viewModelScope.launch {
            rickRepository.takeEpisodes().collectLatest {
                database.episodesList.addAll(it)
            }
        }
        viewModelScope.launch {
            rickRepository.takeLocations().collectLatest {
                database.locationsList.addAll(it)
            }
        }
    }

    fun getCharactersListData(): Flow<PagingData<CharacterModel>> {
        return Pager(
            config = PagingConfig(pageSize = 60, enablePlaceholders = true),
            pagingSourceFactory = {
                CharacterPagingSource(retroService) { characters ->
                    database.charactersList.addAll(characters)
                    rickRepository.insertCharacters(database.charactersList)
                        .launchIn(viewModelScope)
                }
            }
        ).flow.cachedIn(viewModelScope)
    }

    fun getEpisodesListData(): Flow<PagingData<EpisodeModel>> {
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = false),
            pagingSourceFactory = {
                EpisodePagingSource(retroService) { episodes ->
                    database.episodesList.addAll(episodes)
                    rickRepository.insertEpisodes(database.episodesList)
                        .launchIn(viewModelScope)
                }
            }
        ).flow.cachedIn(viewModelScope)
    }

    fun getLocationsListData(): Flow<PagingData<LocationModel>> {
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = false),
            pagingSourceFactory = {
                LocationPagingSource(retroService) { locations ->
                    database.locationsList.addAll(locations)
                    rickRepository.insertLocations(database.locationsList)
                        .launchIn(viewModelScope)
                }
            }
        ).flow.cachedIn(viewModelScope)
    }
}