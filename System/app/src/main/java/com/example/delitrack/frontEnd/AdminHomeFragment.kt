package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminHomeFragment : Fragment() {

    // Delivery Statistics Views
    private lateinit var tvTotalDeliveries: TextView
    private lateinit var tvPendingDeliveries: TextView
    private lateinit var tvConfirmedDeliveries: TextView
    private lateinit var tvInTransitDeliveries: TextView
    private lateinit var tvDeliveredDeliveries: TextView
    private lateinit var tvFailedDeliveries: TextView

    // Rider Statistics Views
    private lateinit var tvTotalRiders: TextView
    private lateinit var tvAvailableRiders: TextView
    private lateinit var tvOnDeliveryRiders: TextView
    private lateinit var tvOffDutyRiders: TextView

    // Vehicle Statistics Views
    private lateinit var tvTotalVehicles: TextView
    private lateinit var tvAvailableVehicles: TextView
    private lateinit var tvVehiclesInUse: TextView
    private lateinit var tvVehiclesMaintenance: TextView

    private var deliveriesListener: ListenerRegistration? = null
    private var ridersListener: ListenerRegistration? = null
    private var vehiclesListener: ListenerRegistration? = null

    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind Delivery Views
        tvTotalDeliveries = view.findViewById(R.id.tvTotalDeliveries)
        tvPendingDeliveries = view.findViewById(R.id.tvPendingDeliveries)
        tvConfirmedDeliveries = view.findViewById(R.id.tvConfirmedDeliveries)
        tvInTransitDeliveries = view.findViewById(R.id.tvInTransitDeliveries)
        tvDeliveredDeliveries = view.findViewById(R.id.tvDeliveredDeliveries)
        tvFailedDeliveries = view.findViewById(R.id.tvFailedDeliveries)

        // Bind Rider Views
        tvTotalRiders = view.findViewById(R.id.tvTotalRiders)
        tvAvailableRiders = view.findViewById(R.id.tvAvailableRiders)
        tvOnDeliveryRiders = view.findViewById(R.id.tvOnDeliveryRiders)
        tvOffDutyRiders = view.findViewById(R.id.tvOffDutyRiders)

        // Bind Vehicle Views
        tvTotalVehicles = view.findViewById(R.id.tvTotalVehicles)
        tvAvailableVehicles = view.findViewById(R.id.tvAvailableVehicles)
        tvVehiclesInUse = view.findViewById(R.id.tvVehiclesInUse)
        tvVehiclesMaintenance = view.findViewById(R.id.tvVehiclesMaintenance)

        view.findViewById<Button>(R.id.btnAdminMonitorGps)?.setOnClickListener {
            val intent = Intent(requireContext(), AdminGpsMonitoringActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btnAdminManageCustomers)?.setOnClickListener {
            val intent = Intent(requireContext(), AdminCustomerManagementActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btnAdminManageFleet)?.setOnClickListener {
            val intent = Intent(requireContext(), AssignRiderVehicleActivity::class.java)
            startActivity(intent)
        }

        listenToDatabaseChanges()
    }

    private fun listenToDatabaseChanges() {
        // 1. Delivery Statistics Listener
        deliveriesListener = db.collection("deliveries")
            .addSnapshotListener { snapshot, error ->
                if (!isAdded || error != null || snapshot == null) return@addSnapshotListener

                var total = 0
                var pending = 0
                var confirmed = 0
                var inTransit = 0
                var delivered = 0
                var failed = 0

                for (doc in snapshot) {
                    total++
                    val status = doc.getString("status") ?: "Pending"
                    when (status.lowercase()) {
                        "pending" -> pending++
                        "confirmed", "acknowledged" -> confirmed++
                        "in transit", "dispatched", "active", "assigned to rider", "assigned" -> inTransit++
                        "delivered", "completed", "delivered & confirmed" -> delivered++
                        else -> {
                            if (status.lowercase().contains("failed") || status.lowercase().contains("issue") || status.lowercase().contains("cancelled")) {
                                failed++
                            }
                        }
                    }
                }

                tvTotalDeliveries.text = total.toString()
                tvPendingDeliveries.text = pending.toString()
                tvConfirmedDeliveries.text = confirmed.toString()
                tvInTransitDeliveries.text = inTransit.toString()
                tvDeliveredDeliveries.text = delivered.toString()
                tvFailedDeliveries.text = failed.toString()
            }

        // 2. Rider Statistics Listener
        ridersListener = db.collection("riders")
            .addSnapshotListener { snapshot, error ->
                if (!isAdded || error != null || snapshot == null) return@addSnapshotListener

                var totalRiders = 0
                var availableRiders = 0
                var onDeliveryRiders = 0
                var offDutyRiders = 0

                for (doc in snapshot) {
                    totalRiders++
                    val status = doc.getString("status") ?: "Available"
                    when (status.lowercase()) {
                        "available" -> availableRiders++
                        "on delivery" -> onDeliveryRiders++
                        "off duty" -> offDutyRiders++
                        else -> availableRiders++
                    }
                }

                tvTotalRiders.text = totalRiders.toString()
                tvAvailableRiders.text = availableRiders.toString()
                tvOnDeliveryRiders.text = onDeliveryRiders.toString()
                tvOffDutyRiders.text = offDutyRiders.toString()
            }

        // 3. Vehicle Statistics Listener
        vehiclesListener = db.collection("vehicles")
            .addSnapshotListener { snapshot, error ->
                if (!isAdded || error != null || snapshot == null) return@addSnapshotListener

                var totalVehicles = 0
                var availableVehicles = 0
                var vehiclesInUse = 0
                var vehiclesMaintenance = 0

                for (doc in snapshot) {
                    totalVehicles++
                    val status = doc.getString("status") ?: "Available"
                    when (status.lowercase()) {
                        "available" -> availableVehicles++
                        "in use" -> vehiclesInUse++
                        "under maintenance" -> vehiclesMaintenance++
                        else -> availableVehicles++
                    }
                }

                tvTotalVehicles.text = totalVehicles.toString()
                tvAvailableVehicles.text = availableVehicles.toString()
                tvVehiclesInUse.text = vehiclesInUse.toString()
                tvVehiclesMaintenance.text = vehiclesMaintenance.toString()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deliveriesListener?.remove()
        ridersListener?.remove()
        vehiclesListener?.remove()
    }
}
