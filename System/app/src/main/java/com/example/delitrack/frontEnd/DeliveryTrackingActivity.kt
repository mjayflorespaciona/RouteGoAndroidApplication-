package com.example.delitrack.frontEnd

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DeliveryTrackingActivity : AppCompatActivity() {

    private lateinit var searchDeliveryIdField: EditText
    private lateinit var btnSearchTracking: Button
    private lateinit var tvTrackingId: TextView
    private lateinit var tvTrackingStatus: TextView
    private lateinit var tvTrackingError: TextView

    // Timeline elements
    private lateinit var stepPendingDot: TextView
    private lateinit var stepPendingText: TextView
    private lateinit var stepConfirmedDot: TextView
    private lateinit var stepConfirmedText: TextView
    private lateinit var stepAssignedDot: TextView
    private lateinit var stepAssignedText: TextView
    private lateinit var stepInTransitDot: TextView
    private lateinit var stepInTransitText: TextView
    private lateinit var stepDeliveredDot: TextView
    private lateinit var stepDeliveredText: TextView

    // Package Info elements
    private lateinit var cardPackageInfo: LinearLayout
    private lateinit var tvPackageDescription: TextView
    private lateinit var tvPackageType: TextView
    private lateinit var tvPickupAddress: TextView
    private lateinit var tvDestinationAddress: TextView
    private lateinit var tvDeliveryNotes: TextView

    // Rider & ETA Cards
    private lateinit var cardRiderInfo: LinearLayout
    private lateinit var tvRiderInfo: TextView
    private lateinit var tvVehicleInfo: TextView
    private lateinit var cardEtaInfo: LinearLayout
    private lateinit var tvEstimatedDate: TextView

    private var trackingListener: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delivery_tracking)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() }

        searchDeliveryIdField = findViewById(R.id.searchDeliveryIdField)
        btnSearchTracking = findViewById(R.id.btnSearchTracking)
        tvTrackingId = findViewById(R.id.tvTrackingId)
        tvTrackingStatus = findViewById(R.id.tvTrackingStatus)
        tvTrackingError = findViewById(R.id.tvTrackingError)

        stepPendingDot = findViewById(R.id.stepPendingDot)
        stepPendingText = findViewById(R.id.stepPendingText)
        stepConfirmedDot = findViewById(R.id.stepConfirmedDot)
        stepConfirmedText = findViewById(R.id.stepConfirmedText)
        stepAssignedDot = findViewById(R.id.stepAssignedDot)
        stepAssignedText = findViewById(R.id.stepAssignedText)
        stepInTransitDot = findViewById(R.id.stepInTransitDot)
        stepInTransitText = findViewById(R.id.stepInTransitText)
        stepDeliveredDot = findViewById(R.id.stepDeliveredDot)
        stepDeliveredText = findViewById(R.id.stepDeliveredText)

        cardPackageInfo = findViewById(R.id.cardPackageInfo)
        tvPackageDescription = findViewById(R.id.tvPackageDescription)
        tvPackageType = findViewById(R.id.tvPackageType)
        tvPickupAddress = findViewById(R.id.tvPickupAddress)
        tvDestinationAddress = findViewById(R.id.tvDestinationAddress)
        tvDeliveryNotes = findViewById(R.id.tvDeliveryNotes)

        cardRiderInfo = findViewById(R.id.cardRiderInfo)
        tvRiderInfo = findViewById(R.id.tvRiderInfo)
        tvVehicleInfo = findViewById(R.id.tvVehicleInfo)
        cardEtaInfo = findViewById(R.id.cardEtaInfo)
        tvEstimatedDate = findViewById(R.id.tvEstimatedDate)

        val initialDeliveryId = intent.getStringExtra("deliveryId") ?: "DLV-1001"
        searchDeliveryIdField.setText(initialDeliveryId)
        startTracking(initialDeliveryId)

        btnSearchTracking.setOnClickListener {
            val queryId = searchDeliveryIdField.text.toString().trim()
            if (queryId.isEmpty()) {
                Toast.makeText(this, "Please enter a Delivery ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startTracking(queryId)
        }
    }

    private fun startTracking(deliveryId: String) {
        trackingListener?.remove()
        tvTrackingId.text = deliveryId
        tvTrackingError.visibility = View.GONE

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""
        val userEmail = currentUser?.email ?: ""

        // Real-time snapshot listener so Admin status updates update customer tracking live
        trackingListener = db.collection("deliveries").document(deliveryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    showError("Delivery ID $deliveryId not found.")
                    return@addSnapshotListener
                }

                val data = snapshot.data ?: return@addSnapshotListener
                val custId = data["customerId"] as? String ?: ""
                val custEmail = data["customerEmail"] as? String ?: ""

                // Customer Privacy Check: Ensure record belongs to logged-in user (or admin)
                val isOwner = (custId.isEmpty() && userEmail == "admin@delitrack.com") ||
                        (custId == userId) ||
                        (userEmail.isNotEmpty() && custEmail.equals(userEmail, ignoreCase = true)) ||
                        (userEmail == "admin@delitrack.com")

                if (!isOwner) {
                    showError("Access Denied: You can only track your own deliveries.")
                    return@addSnapshotListener
                }

                tvTrackingError.visibility = View.GONE
                cardPackageInfo.visibility = View.VISIBLE
                updateTrackingUI(data)
            }
    }

    private fun showError(message: String) {
        tvTrackingError.text = message
        tvTrackingError.visibility = View.VISIBLE
        cardPackageInfo.visibility = View.GONE
        cardRiderInfo.visibility = View.GONE
        cardEtaInfo.visibility = View.GONE
        tvTrackingStatus.text = "Status: Unknown"
        tvTrackingStatus.setBackgroundColor(0xFFE5E7E6.toInt())
        tvTrackingStatus.setTextColor(0xFF555555.toInt())
        resetTimeline()
    }

    private fun updateTrackingUI(data: Map<String, Any>) {
        val status = data["status"] as? String ?: "Pending"
        val packageDesc = data["packageDescription"] as? String ?: "Standard Parcel"
        val packageType = data["packageType"] as? String ?: "Standard"
        val pickup = data["pickupAddress"] as? String ?: data["pickupLocation"] as? String ?: "Nasugbu, Batangas"
        val destination = data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "Lian, Batangas"
        val notes = data["deliveryNotes"] as? String ?: "None"

        val riderName = data["assignedRiderName"] as? String ?: data["riderName"] as? String
        val riderContact = data["assignedRiderContact"] as? String ?: data["riderContact"] as? String
        val vehicleInfo = data["assignedVehicle"] as? String ?: data["vehicleInfo"] as? String
        val eta = data["estimatedDeliveryDate"] as? String ?: data["eta"] as? String

        tvTrackingStatus.text = "Status: $status"
        updateStatusBadgeColor(status)

        tvPackageDescription.text = "Package Description: $packageDesc"
        tvPackageType.text = "Package Type: $packageType"
        tvPickupAddress.text = "Pickup Location: $pickup"
        tvDestinationAddress.text = "Destination: $destination"
        tvDeliveryNotes.text = "Delivery Notes: $notes"

        // Update 5-Step Timeline
        updateTimelineProgress(status)

        val gpsLoc = data["riderGpsLocation"] as? String ?: data["riderLastLocation"] as? String
        val gpsTime = data["riderGpsTimestamp"] as? String ?: data["lastGpsTime"] as? String ?: data["lastUpdated"] as? String

        // Assigned Rider Information (when available)
        val tvRiderGpsLocation = findViewById<TextView>(R.id.tvRiderGpsLocation)
        val tvRiderGpsTime = findViewById<TextView>(R.id.tvRiderGpsTime)

        if (!riderName.isNullOrEmpty()) {
            cardRiderInfo.visibility = View.VISIBLE
            tvRiderInfo.text = "Rider: $riderName ${if (!riderContact.isNullOrEmpty()) "($riderContact)" else ""}"
            tvVehicleInfo.text = "Vehicle: ${vehicleInfo ?: "Assigned Vehicle"}"

            if (!gpsLoc.isNullOrEmpty()) {
                tvRiderGpsLocation.visibility = View.VISIBLE
                tvRiderGpsLocation.text = "GPS Location: $gpsLoc"

                if (!gpsTime.isNullOrEmpty()) {
                    tvRiderGpsTime.visibility = View.VISIBLE
                    tvRiderGpsTime.text = "Last Updated: $gpsTime"
                } else {
                    tvRiderGpsTime.visibility = View.GONE
                }
            } else {
                tvRiderGpsLocation.visibility = View.GONE
                tvRiderGpsTime.visibility = View.GONE
            }
        } else {
            cardRiderInfo.visibility = View.GONE
        }

        // Estimated Delivery Date / ETA (only when provided by Admin)
        if (!eta.isNullOrEmpty()) {
            cardEtaInfo.visibility = View.VISIBLE
            tvEstimatedDate.text = "Est. Delivery: $eta"
        } else {
            cardEtaInfo.visibility = View.GONE
        }

        // Confirm Receipt Button
        val btnConfirmReceipt = findViewById<Button>(R.id.btnConfirmReceipt)
        if (status.equals("Delivered", ignoreCase = true)) {
            btnConfirmReceipt.visibility = View.VISIBLE
            btnConfirmReceipt.isEnabled = true
            btnConfirmReceipt.text = "✓ Confirm Receipt of Parcel"
            btnConfirmReceipt.setOnClickListener {
                val currentDeliveryId = tvTrackingId.text.toString().trim()
                db.collection("deliveries").document(currentDeliveryId)
                    .update("status", "Delivered & Confirmed")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Receipt Confirmed! Thank you for using RoutGo.", Toast.LENGTH_LONG).show()
                    }
            }
        } else if (status.equals("Delivered & Confirmed", ignoreCase = true)) {
            btnConfirmReceipt.visibility = View.VISIBLE
            btnConfirmReceipt.isEnabled = false
            btnConfirmReceipt.text = "✓ Receipt Confirmed by Customer"
        } else {
            btnConfirmReceipt.visibility = View.GONE
        }

        // View Delivery Receipt Button
        val btnViewReceipt = findViewById<Button>(R.id.btnViewReceipt)
        btnViewReceipt.setOnClickListener {
            val currentDeliveryId = tvTrackingId.text.toString().trim()
            val intent = Intent(this, DeliveryReceiptActivity::class.java)
            intent.putExtra("deliveryId", currentDeliveryId)
            startActivity(intent)
        }
    }

    private fun updateStatusBadgeColor(status: String) {
        when (status.lowercase()) {
            "pending" -> {
                tvTrackingStatus.setBackgroundColor(0xFFFEF3C7.toInt())
                tvTrackingStatus.setTextColor(0xFFD97706.toInt())
            }
            "confirmed" -> {
                tvTrackingStatus.setBackgroundColor(0xFFE8F5E9.toInt())
                tvTrackingStatus.setTextColor(0xFF2E7D32.toInt())
            }
            "assigned to rider", "assigned" -> {
                tvTrackingStatus.setBackgroundColor(0xFFF3E5F5.toInt())
                tvTrackingStatus.setTextColor(0xFF7B1FA2.toInt())
            }
            "in transit", "dispatched", "active" -> {
                tvTrackingStatus.setBackgroundColor(0xFFE0F2FE.toInt())
                tvTrackingStatus.setTextColor(0xFF0284C7.toInt())
            }
            "delivered", "completed" -> {
                tvTrackingStatus.setBackgroundColor(0xFFDCFCE7.toInt())
                tvTrackingStatus.setTextColor(0xFF16A34A.toInt())
            }
            else -> {
                tvTrackingStatus.setBackgroundColor(0xFFE5E7E6.toInt())
                tvTrackingStatus.setTextColor(0xFF555555.toInt())
            }
        }
    }

    private fun updateTimelineProgress(status: String) {
        val level = when (status.lowercase()) {
            "pending" -> 1
            "confirmed" -> 2
            "assigned to rider", "assigned" -> 3
            "in transit", "dispatched", "active" -> 4
            "delivered", "completed", "delivered & confirmed" -> 5
            else -> 1
        }

        setStepState(stepPendingDot, stepPendingText, level >= 1)
        setStepState(stepConfirmedDot, stepConfirmedText, level >= 2)
        setStepState(stepAssignedDot, stepAssignedText, level >= 3)
        setStepState(stepInTransitDot, stepInTransitText, level >= 4)
        setStepState(stepDeliveredDot, stepDeliveredText, level >= 5)
    }

    private fun resetTimeline() {
        setStepState(stepPendingDot, stepPendingText, false)
        setStepState(stepConfirmedDot, stepConfirmedText, false)
        setStepState(stepAssignedDot, stepAssignedText, false)
        setStepState(stepInTransitDot, stepInTransitText, false)
        setStepState(stepDeliveredDot, stepDeliveredText, false)
    }

    private fun setStepState(dot: TextView, label: TextView, isCompleted: Boolean) {
        if (isCompleted) {
            dot.setBackgroundColor(0xFF0B4F2C.toInt())
            dot.setTextColor(Color.WHITE)
            label.setTextColor(Color.parseColor("#111111"))
            label.setTypeface(null, Typeface.BOLD)
        } else {
            dot.setBackgroundColor(0xFFE5E7E6.toInt())
            dot.setTextColor(Color.parseColor("#555555"))
            label.setTextColor(Color.parseColor("#888888"))
            label.setTypeface(null, Typeface.NORMAL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        trackingListener?.remove()
    }
}
