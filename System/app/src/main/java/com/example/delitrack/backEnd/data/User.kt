package com.example.delitrack.backEnd.data

data class User(
    val id: String = "",
    val name: String,
    val email: String,
    val phone: String,
    val role: String // "Admin", "Dispatcher", "Rider"
)
