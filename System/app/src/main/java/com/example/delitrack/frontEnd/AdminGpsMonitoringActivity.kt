package com.example.delitrack.frontEnd

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
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

class AdminGpsMonitoringActivity : AppCompatActivity() {

    private lateinit var riderGpsContainer: LinearLayout
    private lateinit var tvActiveRidersHeader: TextView

    private var deliveriesListener: ListenerRegistration? = null
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_gps_monitoring)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        verifyAdminSecurity()

        findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() }

        riderGpsContainer = findViewById(R.id.riderGpsContainer)
        tvActiveRidersHeader = findViewById(R.id.tvActiveRidersHeader)

        startRiderGpsMonitoring()
    }

    private fun verifyAdminSecurity() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            finish()
            return
        }

        if (currentUser.email.equals("admin@delitrack.com", ignoreCase = true)) {
            return
        }

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "Customer"
                if (!role.equals("Admin", ignoreCase = true)) {
                    Toast.makeText(this, "Access Denied: Admin privileges required", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
    }

    private fun startRiderGpsMonitoring() {
        // Real-time snapshot listener monitoring rider GPS updates across all active deliveries
        deliveriesListener = db.collection("deliveries")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                riderGpsContainer.removeAllViews()
                var activeCount = 0

                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val riderName = data["assignedRiderName"] as? String ?: data["riderName"] as? String ?: ""
                    val riderContact = data["assignedRiderContact"] as? String ?: ""
                    val vehicleInfo = data["assignedVehicle"] as? String ?: "Assigned Vehicle"

                    if (riderName.isNotEmpty()) {
                        activeCount++
                        val deliveryId = data["deliveryId"] as? String ?: doc.id
                        val status = data["status"] as? String ?: "Pending"
                        val packageDesc = data["packageDescription"] as? String ?: "Parcel"
                        val dest = data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "Lian, Batangas"

                        val gpsLoc = data["riderGpsLocation"] as? String ?: data["riderLastLocation"] as? String
                        val lastUpdate = data["lastGpsTime"] as? String ?: data["riderGpsTimestamp"] as? String ?: "N/A"

                        renderRiderGpsCard(
                            deliveryId, status, riderName, riderContact, vehicleInfo, packageDesc, dest, gpsLoc, lastUpdate
                        )
                    }
                }

                tvActiveRidersHeader.text = "Active Riders & Deliveries ($activeCount)"

                if (activeCount == 0) {
                    val emptyTv = TextView(this).apply {
                        text = "No active riders or assigned deliveries to monitor currently."
                        setTextColor(Color.parseColor("#777777"))
                        textSize = 14f
                        setPadding(16, 24, 16, 24)
                    }
                    riderGpsContainer.addView(emptyTv)
                }
            }
    }

    private fun renderRiderGpsCard(
        deliveryId: String,
        status: String,
        riderName: String,
        riderContact: String,
        vehicleInfo: String,
        packageDesc: String,
        dest: String,
        gpsLoc: String?,
        lastUpdate: String
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            setPadding(24, 24, 24, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
        }

        val headerRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val titleView = TextView(this).apply {
            text = "Rider: $riderName ${if (riderContact.isNotEmpty()) "($riderContact)" else ""}"
            setTextColor(Color.parseColor("#111111"))
            setTypeface(null, Typeface.BOLD)
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val isCompletedOrCancelled = status.equals("Delivered", ignoreCase = true) ||
                status.equals("Completed", ignoreCase = true) ||
                status.equals("Delivered & Confirmed", ignoreCase = true) ||
                status.equals("Cancelled", ignoreCase = true)

        // Clearly distinguish Live GPS from Last-Known Location
        val gpsBadgeText = when {
            isCompletedOrCancelled -> "🛑 STOPPED"
            gpsLoc.isNullOrEmpty() -> "⚪ GPS OFFLINE"
            lastUpdate.contains("Today") || lastUpdate.contains("AM") || lastUpdate.contains("PM") -> "🟢 LIVE GPS"
            else -> "🟡 LAST-KNOWN LOCATION"
        }

        val gpsBadge = TextView(this).apply {
            text = gpsBadgeText
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setPadding(16, 4, 16, 4)
            when (gpsBadgeText) {
                "🟢 LIVE GPS" -> {
                    setBackgroundColor(Color.parseColor("#DCFCE7"))
                    setTextColor(Color.parseColor("#16A34A"))
                }
                "🟡 LAST-KNOWN LOCATION" -> {
                    setBackgroundColor(Color.parseColor("#FEF3C7"))
                    setTextColor(Color.parseColor("#D97706"))
                }
                else -> {
                    setBackgroundColor(Color.parseColor("#F1F5F9"))
                    setTextColor(Color.parseColor("#64748B"))
                }
            }
        }

        headerRow.addView(titleView)
        headerRow.addView(gpsBadge)
        card.addView(headerRow)

        val vehicleView = TextView(this).apply {
            text = "Vehicle: $vehicleInfo • Status: $status"
            setTextColor(Color.parseColor("#0B4F2C"))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 6, 0, 0)
        }
        card.addView(vehicleView)

        val deliveryView = TextView(this).apply {
            text = "Tracking ID: $deliveryId • Package: $packageDesc"
            setTextColor(Color.parseColor("#333333"))
            textSize = 13f
            setPadding(0, 4, 0, 0)
        }
        card.addView(deliveryView)

        val destView = TextView(this).apply {
            text = "Destination: $dest"
            setTextColor(Color.parseColor("#555555"))
            textSize = 12f
            setPadding(0, 2, 0, 0)
        }
        card.addView(destView)

        // GPS Location & Timestamp Display (Only actual data received from rider)
        val gpsDetailsView = TextView(this).apply {
            text = if (isCompletedOrCancelled) {
                "Monitoring Stopped: Delivery is $status."
            } else if (!gpsLoc.isNullOrEmpty()) {
                "GPS Location: $gpsLoc\nLast Update Time: $lastUpdate"
            } else {
                "GPS Status: Awaiting rider location update."
            }
            setTextColor(Color.parseColor(if (!gpsLoc.isNullOrEmpty() && !isCompletedOrCancelled) "#0284C7" else "#777777"))
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 8, 0, 0)
        }
        card.addView(gpsDetailsView)

        val actionsBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)
        }

        val btnLocateMap = Button(this).apply {
            text = "Locate on GIS Map"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#0B4F2C"))
            setTextColor(Color.WHITE)
            val lp = LinearLayout.LayoutParams(0, 90, 1f)
            lp.setMargins(0, 0, 8, 0)
            layoutParams = lp
            setOnClickListener {
                val intent = Intent(this@AdminGpsMonitoringActivity, SelectRouteActivity::class.java)
                intent.putExtra("deliveryId", deliveryId)
                startActivity(intent)
            }
        }

        val btnViewTracking = Button(this).apply {
            text = "View Tracking Details"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#0284C7"))
            setTextColor(Color.WHITE)
            val lp = LinearLayout.LayoutParams(0, 90, 1f)
            layoutParams = lp
            setOnClickListener {
                val intent = Intent(this@AdminGpsMonitoringActivity, DeliveryTrackingActivity::class.java)
                intent.putExtra("deliveryId", deliveryId)
                startActivity(intent)
            }
        }

        actionsBar.addView(btnLocateMap)
        actionsBar.addView(btnViewTracking)
        card.addView(actionsBar)

        riderGpsContainer.addView(card)
    }

    override fun onDestroy() {
        super.onDestroy()
        deliveriesListener?.remove()
    }
}
