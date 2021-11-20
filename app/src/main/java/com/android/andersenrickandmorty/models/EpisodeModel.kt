package com.android.andersenrickandmorty.models

data class AllEpisodes(
    val info: Info,
    val results: List<EpisodeModel>
)

data class EpisodeModel(
    val id: Int,
    val name: String,
    val air_date: String,
    val episode: String,
    val characters: List<String>,
    val url: String,
    val created: String
)
