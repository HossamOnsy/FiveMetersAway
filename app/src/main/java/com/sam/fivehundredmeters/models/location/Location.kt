package com.sam.fivehundredmeters.models.location

data class Location(
    val address: String,
    val cc: String,
    val city: String,
    val country: String,
    val formattedAddress: List<String>
)