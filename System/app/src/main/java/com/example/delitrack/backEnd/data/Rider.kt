package com.example.delitrack.backEnd.data

data class Rider(
    val riderId: String,
    var fullName: String,
    var phone: String,
    var assignedVehicleId: String? = null,
    var status: String = "Available" // "Active", "Available", "Offline"
)
