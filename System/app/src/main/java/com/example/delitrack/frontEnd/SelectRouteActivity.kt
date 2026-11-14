package com.example.delitrack.frontEnd

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.delitrack.R
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class SelectRouteActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var tvRouteTitle: TextView
    private lateinit var tvPickupText: TextView
    private lateinit var tvDestinationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContentView(R.layout.activity_select_route)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() }

        tvRouteTitle = findViewById(R.id.tvRouteTitle)
        tvPickupText = findViewById(R.id.tvPickupText)
        tvDestinationText = findViewById(R.id.tvDestinationText)

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val deliveryId = intent.getStringExtra("deliveryId")
        if (!deliveryId.isNullOrEmpty()) {
            loadDeliveryDetails(deliveryId)
        } else {
            setupRouteMap("Nasugbu, Batangas", "Lian, Batangas")
        }

        findViewById<Button>(R.id.btnUseThisRoute).setOnClickListener {
            Toast.makeText(this, "Route Selected! Returning to Dashboard", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loadDeliveryDetails(deliveryId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("deliveries").document(deliveryId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val pickup = document.getString("pickupAddress") ?: document.getString("pickupLocation") ?: "Nasugbu, Batangas"
                    val dest = document.getString("destination") ?: document.getString("deliveryAddress") ?: "Lian, Batangas"
                    setupRouteMap(pickup, dest)
                } else {
                    setupRouteMap("Nasugbu, Batangas", "Lian, Batangas")
                }
            }
            .addOnFailureListener {
                setupRouteMap("Nasugbu, Batangas", "Lian, Batangas")
            }
    }

    private fun setupRouteMap(pickupStr: String, destStr: String) {
        tvRouteTitle.text = "$pickupStr ➔ $destStr"
        tvPickupText.text = "Pickup: $pickupStr"
        tvDestinationText.text = "Destination: $destStr"

        // Coordinates for Nasugbu and Lian, Batangas
        val nasugbuPoint = GeoPoint(14.0754, 120.6311)
        val lianPoint = GeoPoint(14.0384, 120.6508)

        mapView.overlays.clear()

        // 1. Pickup Location Marker (Nasugbu)
        val pickupMarker = Marker(mapView)
        pickupMarker.position = nasugbuPoint
        pickupMarker.title = "Pickup: $pickupStr"
        pickupMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(pickupMarker)

        // 2. Delivery Destination Marker (Lian)
        val destMarker = Marker(mapView)
        destMarker.position = lianPoint
        destMarker.title = "Destination: $destStr"
        destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(destMarker)

        // 3. Route Polyline connecting Pickup and Destination
        val routeLine = Polyline()
        routeLine.addPoint(nasugbuPoint)
        routeLine.addPoint(GeoPoint(14.0620, 120.6380)) // Palico - Lian Hwy waypoint
        routeLine.addPoint(GeoPoint(14.0500, 120.6430))
        routeLine.addPoint(lianPoint)
        routeLine.outlinePaint.color = Color.parseColor("#0B4F2C")
        routeLine.outlinePaint.strokeWidth = 10f
        mapView.overlays.add(routeLine)

        // Center map view between the two points
        val centerPoint = GeoPoint((14.0754 + 14.0384) / 2, (120.6311 + 120.6508) / 2)
        mapView.controller.setZoom(12.5)
        mapView.controller.setCenter(centerPoint)
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
