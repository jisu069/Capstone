package com.example.navigationwalkers

import com.google.gson.annotations.SerializedName

data class ServerResponse(
    @SerializedName("type")
    val type: String,

    @SerializedName("features")
    val features: List<Feature>
)

data class Feature (

    @SerializedName("type")
    val type: String,

    @SerializedName("geometry")
    val geometry: Geometry
)

data class Geometry(
    @SerializedName("type")
    val type: String,

    @SerializedName("coordinates")
    val coordinates: Any
)








