package com.example.delitrack.backEnd.data

data class Rider(
    val riderId: String = "RDR-${System.currentTimeMillis().toString().takeLast(4)}",
    var fullName: String,
    var phone: String,
    var assignedVehicleId: String? = null,
    var status: String = "Available" // "Available", "On Delivery", "Off Duty"
)
