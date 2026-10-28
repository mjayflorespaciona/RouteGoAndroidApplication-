package com.example.delitrack.backEnd.data

data class  Rider(
    val riderId: String,
    val name: String,
    val phone: String,
    val isAvailable: Boolean = true
)
