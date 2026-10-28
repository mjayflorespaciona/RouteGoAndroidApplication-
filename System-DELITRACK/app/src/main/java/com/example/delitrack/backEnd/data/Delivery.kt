package com.example.delitrack.backEnd.data
import com.example.delitrack.backEnd.data.enums.DeliveryStatus

data class Delivery(
    val deliveryId: String,
    val trackingNumber: String,
    val senderName: String,
    val recipientName: String,
    val recipientAddress: String,
    val recipientPhone: String,
    val itemDescription: String,
    val weightKg: Double,
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val assignedRiderId: String? = null,
    val assignedVehicleId: String? = null,
    val routeName: String = "Nasugbu - Lian Route"
)