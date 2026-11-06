package com.example.delitrack.backEnd.data

data class Vehicle(
    val vehicleId: String,
    var plateNumber: String,
    var vehicleType: String, // "Motorcycle", "Delivery Van"
    var assignedRiderId: String? = null,
    var status: String = "Available" // "Assigned", "Available"
)
