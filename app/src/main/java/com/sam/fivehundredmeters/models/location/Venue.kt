package com.sam.fivehundredmeters.models.location


data class Venue(
    var categories: List<Category>? = null,
    var id: String? = null,
    var location: Location? = null,
    var name: String? = null,
    var imageUrl: String? = null
)