package com.example.delitrack.backEnd.data

data class Vehicle(
    val vehicleId: String,
    val plateNumber: String,
    val vehicleType: String, // e.g. "Delivery Van", "Motorcycle"
    val assignedRiderId: String? = null
)
