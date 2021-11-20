package com.android.andersenrickandmorty.retrofit

import com.android.andersenrickandmorty.models.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitApi {

    @GET("api/character")
    fun getAllCharactersCall(
        @Query("page") page: Int
    ): Call<AllCharacters>

    @GET("api/character")
    suspend fun getAllCharacters(
        @Query("page") query: Int
    ): AllCharacters

    @GET("api/character")
    fun getFilteredCharactersCall(
        @Query("page") page: Int,
        @Query("name") name: String,
        @Query("status") status: String,
        @Query("species") species: String,
        @Query("type") type: String,
        @Query("gender") gender: String
    ): Call<AllCharacters>


    @GET("api/character/{id}")
    fun getCharacterForDetails(
        @Path("id") id: Int
    ): Call<CharacterModel>

    @GET("api/episode")
    fun getAllEpisodesCall(
        @Query("page") page: Int
    ): Call<AllEpisodes>

    @GET("api/episode")
    suspend fun getAllEpisodes(
        @Query("page") page: Int
    ): AllEpisodes

    @GET("api/episode")
    fun getFilteredEpisodesCall(
        @Query("page") page: Int,
        @Query("name") name: String,
        @Query("episode") episode: String
    ): Call<AllEpisodes>

    @GET("api/episode/{id}")
    fun getEpisodeForDetails(
        @Path("id") id: Int
    ): Call<EpisodeModel>

    @GET("api/location")
    fun getAllLocationsCall(
        @Query("page") page: Int
    ): Call<AllLocations>

    @GET("api/location")
    suspend fun getAllLocations(
        @Query("page") page: Int
    ): AllLocations

    @GET("api/location")
    fun getFilteredLocationsCall(
        @Query("page") page: Int,
        @Query("name") name: String,
        @Query("type") type: String,
        @Query("dimension") dimension: String
    ): Call<AllLocations>
}