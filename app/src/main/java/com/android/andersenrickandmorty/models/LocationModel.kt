package com.android.andersenrickandmorty.models

data class AllLocations(
    val info: Info,
    val results: List<LocationModel>
)

data class LocationModel(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: List<String>,
    val url: String,
    val created: String
)
