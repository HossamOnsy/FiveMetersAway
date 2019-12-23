package com.sam.fivehundredmeters.models.photo

data class PhotoItem(
    val createdAt: Int,
    val height: Int,
    val id: String,
    val prefix: String,
    val suffix: String,
    val width: Int
)