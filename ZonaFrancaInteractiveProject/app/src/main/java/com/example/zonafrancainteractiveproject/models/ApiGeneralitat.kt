package com.example.ac2_fragmentsmapes.models

data class Equipment(
    val idequipament: String,
    val alies: String,
    val nom: String,
    val categoria: String,
    val via: String,
    val cpostal: String,
    val poblacio: String,
    val codi_municipi: String,
    val comarca: String,
    val telefon1: String,
    val utmx: String,
    val utmy: String,
    val longitud: String,
    val latitud: String,
    val data_modificacio: String,
    val localitzacio: Location
)

data class Location(
    val type: String,
    val coordinates: List<Double>
)
