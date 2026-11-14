package com.example.delitrack.backEnd.data

data class Vehicle(
    val vehicleId: String = "VHC-${System.currentTimeMillis().toString().takeLast(4)}",
    var plateNumber: String,
    var vehicleType: String, // "Motorcycle", "Delivery Van"
    var assignedRiderId: String? = null,
    var status: String = "Available", // "Available", "In Use", "Under Maintenance"
    var maxWeightKg: Double = 50.0,
    var cargoCapacity: String = "Standard Box"
)
