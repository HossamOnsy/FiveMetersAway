package com.sam.fivehundredmeters.models.location


data class Venue(
    var categories: List<Category>,
    var id: String,
    var location: Location,
    var name: String,
    var imageUrl: String
)