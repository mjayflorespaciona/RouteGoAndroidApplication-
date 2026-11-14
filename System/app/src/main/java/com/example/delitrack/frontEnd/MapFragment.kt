package com.example.delitrack.frontEnd

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var searchDeliveryIdField: EditText
    private lateinit var tvGpsStatusBadge: TextView
    private lateinit var tvLastGpsUpdate: TextView
    private lateinit var btnShareGpsLocation: Button

    // Actual Geographic Coordinates for Nasugbu and Lian, Batangas
    private val nasugbuPoint = GeoPoint(14.0754, 120.6311)
    private val lianPoint = GeoPoint(14.0384, 120.6508)
    private var riderGpsMarker: Marker? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(
            requireContext().applicationContext,
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        searchDeliveryIdField = view.findViewById(R.id.searchDeliveryIdField)
        tvGpsStatusBadge = view.findViewById(R.id.tvGpsStatusBadge)
        tvLastGpsUpdate = view.findViewById(R.id.tvLastGpsUpdate)
        btnShareGpsLocation = view.findViewById(R.id.btnShareGpsLocation)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        setupRouteMap()
        loadDeliveryMarkersFromFirestore()

        val openTracking: () -> Unit = {
            val queryId = searchDeliveryIdField.text.toString().trim().ifEmpty { "DLV-1001" }
            val intent = Intent(requireContext(), DeliveryTrackingActivity::class.java)
            intent.putExtra("deliveryId", queryId)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btnSearchMapTracking)?.setOnClickListener { openTracking() }
        view.findViewById<Button>(R.id.btnViewTracking)?.setOnClickListener { openTracking() }

        btnShareGpsLocation.setOnClickListener {
            checkAndShareRiderGpsLocation()
        }
    }

    private fun setupRouteMap() {
        mapView.overlays.clear()

        // 1. Pickup Location Marker (Nasugbu, Batangas)
        val pickupMarker = Marker(mapView).apply {
            position = nasugbuPoint
            title = "Pickup Point: Nasugbu, Batangas"
            snippet = "Main Pickup Terminal (14.0754, 120.6311)"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(pickupMarker)

        // 2. Destination Location Marker (Lian, Batangas)
        val destMarker = Marker(mapView).apply {
            position = lianPoint
            title = "Destination Point: Lian, Batangas"
            snippet = "Main Delivery Destination (14.0384, 120.6508)"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(destMarker)

        // 3. Nasugbu–Lian Route Polyline
        val routeLine = Polyline().apply {
            addPoint(nasugbuPoint)
            addPoint(GeoPoint(14.0620, 120.6380)) // Palico-Lian Hwy Waypoint
            addPoint(GeoPoint(14.0500, 120.6430))
            addPoint(lianPoint)
            outlinePaint.color = Color.parseColor("#0B4F2C")
            outlinePaint.strokeWidth = 10f
        }
        mapView.overlays.add(routeLine)

        val centerPoint = GeoPoint((14.0754 + 14.0384) / 2, (120.6311 + 120.6508) / 2)
        mapView.controller.setZoom(12.5)
        mapView.controller.setCenter(centerPoint)
        mapView.invalidate()
    }

    private fun loadDeliveryMarkersFromFirestore() {
        db.collection("deliveries")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || snapshot == null) return@addOnSuccessListener

                for ((index, doc) in snapshot.documents.withIndex()) {
                    val data = doc.data ?: continue
                    val deliveryId = data["deliveryId"] as? String ?: doc.id
                    val status = data["status"] as? String ?: "Pending"
                    val receiver = data["receiverName"] as? String ?: "Receiver"
                    val pickup = data["pickupAddress"] as? String ?: data["pickupLocation"] as? String ?: "Nasugbu"
                    val dest = data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "Lian"
                    val riderName = data["assignedRiderName"] as? String ?: "Unassigned"
                    val gpsLoc = data["riderGpsLocation"] as? String ?: "No GPS Data"
                    val lastUpdate = data["lastGpsTime"] as? String ?: data["riderGpsTimestamp"] as? String ?: "N/A"

                    // Compute geographic waypoint position along the Nasugbu-Lian route
                    val fraction = (index % 5) / 5.0
                    val deliveryLat = 14.0754 - (fraction * (14.0754 - 14.0384))
                    val deliveryLng = 120.6311 + (fraction * (120.6508 - 120.6311))

                    val deliveryMarker = Marker(mapView).apply {
                        position = GeoPoint(deliveryLat, deliveryLng)
                        title = "$deliveryId • Status: $status"
                        snippet = "Receiver: $receiver | Route: $pickup ➔ $dest"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        setOnMarkerClickListener { _, _ ->
                            showDeliveryMarkerDetailsDialog(
                                deliveryId, status, receiver, pickup, dest, riderName, gpsLoc, lastUpdate
                            )
                            true
                        }
                    }
                    mapView.overlays.add(deliveryMarker)
                }
                mapView.invalidate()
            }
    }

    private fun showDeliveryMarkerDetailsDialog(
        deliveryId: String,
        status: String,
        receiver: String,
        pickup: String,
        dest: String,
        riderName: String,
        gpsLoc: String,
        lastUpdate: String
    ) {
        val detailsMessage = "Tracking ID: $deliveryId\n" +
                "Status: $status\n\n" +
                "Receiver: $receiver\n" +
                "Pickup Location: $pickup\n" +
                "Destination Address: $dest\n\n" +
                "Assigned Rider: $riderName\n" +
                "Rider GPS Location: $gpsLoc\n" +
                "Last GPS Update Time: $lastUpdate"

        AlertDialog.Builder(requireContext())
            .setTitle("Delivery Details ($deliveryId)")
            .setMessage(detailsMessage)
            .setPositiveButton("Open Full Tracking") { _, _ ->
                val intent = Intent(requireContext(), DeliveryTrackingActivity::class.java)
                intent.putExtra("deliveryId", deliveryId)
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun checkAndShareRiderGpsLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        performGpsShare()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performGpsShare()
            } else {
                Toast.makeText(requireContext(), "Location permission denied. Cannot share GPS location.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performGpsShare() {
        val deliveryId = searchDeliveryIdField.text.toString().trim().ifEmpty { "DLV-1001" }

        db.collection("deliveries").document(deliveryId)
            .get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                if (doc != null && doc.exists()) {
                    val status = doc.getString("status") ?: "Pending"

                    if (status.equals("Delivered", ignoreCase = true) ||
                        status.equals("Completed", ignoreCase = true) ||
                        status.equals("Delivered & Confirmed", ignoreCase = true) ||
                        status.equals("Cancelled", ignoreCase = true)) {

                        tvGpsStatusBadge.text = "GPS Stopped"
                        tvGpsStatusBadge.setBackgroundColor(Color.parseColor("#F1F5F9"))
                        tvGpsStatusBadge.setTextColor(Color.parseColor("#64748B"))
                        Toast.makeText(requireContext(), "Location sharing stopped. Delivery is $status.", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    var lastKnownLoc: Location? = null

                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        lastKnownLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }

                    val actualLat = lastKnownLoc?.latitude ?: 14.0562
                    val actualLng = lastKnownLoc?.longitude ?: 120.6402
                    val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    val gpsLocString = "Nasugbu-Lian Hwy (${String.format(Locale.US, "%.4f, %.4f", actualLat, actualLng)})"

                    val updates = mapOf(
                        "riderGpsLocation" to gpsLocString,
                        "riderGpsTimestamp" to formattedTime,
                        "lastGpsTime" to formattedTime
                    )

                    db.collection("deliveries").document(deliveryId)
                        .update(updates)
                        .addOnSuccessListener {
                            if (!isAdded) return@addOnSuccessListener
                            tvGpsStatusBadge.text = "GPS Active"
                            tvGpsStatusBadge.setBackgroundColor(Color.parseColor("#DCFCE7"))
                            tvGpsStatusBadge.setTextColor(Color.parseColor("#16A34A"))
                            tvLastGpsUpdate.text = formattedTime

                            if (riderGpsMarker == null) {
                                riderGpsMarker = Marker(mapView).apply {
                                    title = "Rider GPS: $gpsLocString"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                mapView.overlays.add(riderGpsMarker)
                            }
                            riderGpsMarker?.position = GeoPoint(actualLat, actualLng)
                            mapView.invalidate()

                            Toast.makeText(requireContext(), "GPS Location Shared: $gpsLocString at $formattedTime", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Delivery ID $deliveryId not found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized) mapView.onPause()
    }
}
