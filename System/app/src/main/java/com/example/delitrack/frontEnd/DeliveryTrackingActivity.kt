package com.example.delitrack.frontEnd

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.delitrack.R
import com.google.firebase.firestore.FirebaseFirestore

class DeliveryTrackingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delivery_tracking)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        val deliveryId = intent.getStringExtra("deliveryId") ?: "DLV-1001"
        findViewById<TextView>(R.id.tvTrackingId).text = deliveryId

        val tvTrackingStatus = findViewById<TextView>(R.id.tvTrackingStatus)
        val tvPickupAddress = findViewById<TextView>(R.id.tvPickupAddress)
        val tvDestinationAddress = findViewById<TextView>(R.id.tvDestinationAddress)
        val tvCustomerInfo = findViewById<TextView>(R.id.tvCustomerInfo)

        val db = FirebaseFirestore.getInstance()
        db.collection("deliveries").document(deliveryId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val status = document.getString("status") ?: "In Transit"
                    val customer = document.getString("customerName") ?: "Juan Dela Cruz"
                    val contact = document.getString("contactNumber") ?: "0917 555 1234"
                    val pickup = document.getString("pickupAddress") ?: "Nasugbu, Batangas"
                    val destination = document.getString("destination") ?: "Lian, Batangas"

                    tvTrackingStatus.text = status
                    tvPickupAddress.text = pickup
                    tvDestinationAddress.text = destination
                    tvCustomerInfo.text = "$customer\n$contact"
                }
            }
    }
}
