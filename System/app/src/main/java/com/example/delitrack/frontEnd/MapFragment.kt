package com.example.delitrack.frontEnd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var searchDeliveryIdField: EditText

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

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val startPoint = GeoPoint(14.0754, 120.6311)
        mapView.controller.setZoom(13.0)
        mapView.controller.setCenter(startPoint)

        val openTracking: () -> Unit = {
            val queryId = searchDeliveryIdField.text.toString().trim().ifEmpty { "DLV-1001" }
            val intent = Intent(requireContext(), DeliveryTrackingActivity::class.java)
            intent.putExtra("deliveryId", queryId)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btnSearchMapTracking)?.setOnClickListener { openTracking() }
        view.findViewById<Button>(R.id.btnViewTracking)?.setOnClickListener { openTracking() }
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
