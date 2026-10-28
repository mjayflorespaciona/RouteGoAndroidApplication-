package com.example.delitrack.backEnd.data

data class Route(
    val routeId: String,
    val routeName: String,
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val checkpoints: List<Checkpoint>
)
