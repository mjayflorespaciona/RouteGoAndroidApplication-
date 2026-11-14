package com.example.delitrack.frontEnd

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.delitrack.backEnd.data.Rider
import com.example.delitrack.backEnd.data.Vehicle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AssignRiderVehicleActivity : AppCompatActivity() {

    private val ridersList = mutableListOf(
        Rider("RDR-101", "Mark Santos", "0917 123 4567", "TRK-001", "On Delivery"),
        Rider("RDR-102", "Ana Garcia", "0918 765 4321", "VAN-002", "On Delivery"),
        Rider("RDR-103", "Carlos Mendoza", "0919 222 3333", "MTR-001", "On Delivery"),
        Rider("RDR-104", "Juan Dela Cruz", "0920 444 5555", null, "Available"),
        Rider("RDR-105", "Maria Santos", "0921 666 7777", null, "Available"),
        Rider("RDR-106", "Jose Rizal", "0922 888 9999", null, "Available"),
        Rider("RDR-107", "Andres Bonifacio", "0923 111 2222", null, "Available"),
        Rider("RDR-108", "Gabriela Silang", "0924 333 4444", null, "Available"),
        Rider("RDR-109", "Emilio Aguinaldo", "0925 555 6666", null, "Available"),
        Rider("RDR-110", "Apolinario Mabini", "0926 777 8888", null, "Available"),
        Rider("RDR-111", "Melchora Aquino", "0927 999 0000", null, "Available"),
        Rider("RDR-112", "Antonio Luna", "0928 123 1111", null, "Available"),
        Rider("RDR-113", "Diego Silang", "0929 234 2222", null, "Available"),
        Rider("RDR-114", "Miguel Malvar", "0930 345 3333", null, "Off Duty"),
        Rider("RDR-115", "Teresa Magbanua", "0931 456 4444", null, "Off Duty")
    )

    private val vehiclesList = mutableListOf(
        Vehicle("TRK-001", "NKR-1234", "Delivery Van", "RDR-101", "In Use"),
        Vehicle("VAN-002", "L300-567", "Delivery Van", "RDR-102", "In Use"),
        Vehicle("MTR-001", "MC-1011", "Motorcycle", "RDR-103", "In Use"),
        Vehicle("MTR-002", "MC-1022", "Motorcycle", null, "Available"),
        Vehicle("MTR-003", "MC-1033", "Motorcycle", null, "Available"),
        Vehicle("MTR-004", "MC-1044", "Motorcycle", null, "Available"),
        Vehicle("MTR-005", "MC-1055", "Motorcycle", null, "Available"),
        Vehicle("MTR-006", "MC-1066", "Motorcycle", null, "Available"),
        Vehicle("MTR-007", "MC-1077", "Motorcycle", null, "Under Maintenance"),
        Vehicle("MTR-008", "MC-1088", "Motorcycle", null, "Available")
    )

    private lateinit var riderListContainer: LinearLayout
    private lateinit var vehicleListContainer: LinearLayout
    private lateinit var sectionRiders: LinearLayout
    private lateinit var sectionVehicles: LinearLayout
    private lateinit var tabRidersBtn: Button
    private lateinit var tabVehiclesBtn: Button

    // Rider Filters & Search
    private lateinit var searchRiderField: EditText
    private lateinit var btnFilterRiderAll: Button
    private lateinit var btnFilterRiderAvailable: Button
    private lateinit var btnFilterRiderOnDelivery: Button
    private lateinit var btnFilterRiderOffDuty: Button

    // Vehicle Filters & Search
    private lateinit var searchVehicleField: EditText
    private lateinit var btnFilterVehicleAll: Button
    private lateinit var btnFilterVehicleMotorcycle: Button
    private lateinit var btnFilterVehicleVan: Button
    private lateinit var btnFilterVehicleAvailable: Button
    private lateinit var btnFilterVehicleInUse: Button
    private lateinit var btnFilterVehicleMaintenance: Button

    // Dashboard Statistics views
    private lateinit var tvTotalRiders: TextView
    private lateinit var tvAvailableRiders: TextView
    private lateinit var tvOnDeliveryRiders: TextView
    private lateinit var tvOffDutyRiders: TextView

    private lateinit var tvTotalVehicles: TextView
    private lateinit var tvAvailableVehicles: TextView
    private lateinit var tvVehiclesInUse: TextView
    private lateinit var tvVehiclesMaintenance: TextView

    private var currentRiderFilter = "All"
    private var riderSearchQuery = ""

    private var currentVehicleFilter = "All"
    private var vehicleSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assign_rider_vehicle)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Admin Security Guard
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && !currentUser.email.equals("admin@delitrack.com", ignoreCase = true)) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role") ?: "Customer"
                    if (!role.equals("Admin", ignoreCase = true)) {
                        Toast.makeText(this, "Access Denied: Admin privileges required", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
        }

        findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() }

        riderListContainer = findViewById(R.id.riderListContainer)
        vehicleListContainer = findViewById(R.id.vehicleListContainer)
        sectionRiders = findViewById(R.id.sectionRiders)
        sectionVehicles = findViewById(R.id.sectionVehicles)
        tabRidersBtn = findViewById(R.id.tabRidersBtn)
        tabVehiclesBtn = findViewById(R.id.tabVehiclesBtn)

        searchRiderField = findViewById(R.id.searchRiderField)
        btnFilterRiderAll = findViewById(R.id.btnFilterRiderAll)
        btnFilterRiderAvailable = findViewById(R.id.btnFilterRiderAvailable)
        btnFilterRiderOnDelivery = findViewById(R.id.btnFilterRiderOnDelivery)
        btnFilterRiderOffDuty = findViewById(R.id.btnFilterRiderOffDuty)

        searchVehicleField = findViewById(R.id.searchVehicleField)
        btnFilterVehicleAll = findViewById(R.id.btnFilterVehicleAll)
        btnFilterVehicleMotorcycle = findViewById(R.id.btnFilterVehicleMotorcycle)
        btnFilterVehicleVan = findViewById(R.id.btnFilterVehicleVan)
        btnFilterVehicleAvailable = findViewById(R.id.btnFilterVehicleAvailable)
        btnFilterVehicleInUse = findViewById(R.id.btnFilterVehicleInUse)
        btnFilterVehicleMaintenance = findViewById(R.id.btnFilterVehicleMaintenance)

        // Bind statistics views
        tvTotalRiders = findViewById(R.id.tvTotalRiders)
        tvAvailableRiders = findViewById(R.id.tvAvailableRiders)
        tvOnDeliveryRiders = findViewById(R.id.tvOnDeliveryRiders)
        tvOffDutyRiders = findViewById(R.id.tvOffDutyRiders)

        tvTotalVehicles = findViewById(R.id.tvTotalVehicles)
        tvAvailableVehicles = findViewById(R.id.tvAvailableVehicles)
        tvVehiclesInUse = findViewById(R.id.tvVehiclesInUse)
        tvVehiclesMaintenance = findViewById(R.id.tvVehiclesMaintenance)

        tabRidersBtn.setOnClickListener { switchTab("Riders") }
        tabVehiclesBtn.setOnClickListener { switchTab("Vehicles") }

        // Rider Search & Filter listeners
        btnFilterRiderAll.setOnClickListener { setRiderFilter("All") }
        btnFilterRiderAvailable.setOnClickListener { setRiderFilter("Available") }
        btnFilterRiderOnDelivery.setOnClickListener { setRiderFilter("On Delivery") }
        btnFilterRiderOffDuty.setOnClickListener { setRiderFilter("Off Duty") }

        searchRiderField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                riderSearchQuery = s?.toString()?.trim() ?: ""
                renderRidersList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Vehicle Search & Filter listeners
        btnFilterVehicleAll.setOnClickListener { setVehicleFilter("All") }
        btnFilterVehicleMotorcycle.setOnClickListener { setVehicleFilter("Motorcycle") }
        btnFilterVehicleVan.setOnClickListener { setVehicleFilter("Delivery Van") }
        btnFilterVehicleAvailable.setOnClickListener { setVehicleFilter("Available") }
        btnFilterVehicleInUse.setOnClickListener { setVehicleFilter("In Use") }
        btnFilterVehicleMaintenance.setOnClickListener { setVehicleFilter("Under Maintenance") }

        searchVehicleField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                vehicleSearchQuery = s?.toString()?.trim() ?: ""
                renderVehiclesList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<Button>(R.id.btnAddRider).setOnClickListener { showAddRiderDialog() }
        findViewById<Button>(R.id.btnAddVehicle).setOnClickListener { showAddVehicleDialog() }

        updateOverviewStats()
        renderRidersList()
        renderVehiclesList()
    }

    private fun setRiderFilter(filter: String) {
        currentRiderFilter = filter
        updateRiderFilterButtonStyles()
        renderRidersList()
    }

    private fun updateRiderFilterButtonStyles() {
        val activeBg = ColorStateList.valueOf(Color.parseColor("#0B4F2C"))
        val inactiveBg = ColorStateList.valueOf(Color.parseColor("#E5E7E6"))
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#555555")

        val buttons = listOf(btnFilterRiderAll, btnFilterRiderAvailable, btnFilterRiderOnDelivery, btnFilterRiderOffDuty)
        val filters = listOf("All", "Available", "On Delivery", "Off Duty")

        for (i in buttons.indices) {
            val btn = buttons[i]
            val f = filters[i]
            val isActive = (currentRiderFilter == f)
            btn.backgroundTintList = if (isActive) activeBg else inactiveBg
            btn.setTextColor(if (isActive) activeText else inactiveText)
        }
    }

    private fun setVehicleFilter(filter: String) {
        currentVehicleFilter = filter
        updateVehicleFilterButtonStyles()
        renderVehiclesList()
    }

    private fun updateVehicleFilterButtonStyles() {
        val activeBg = ColorStateList.valueOf(Color.parseColor("#0B4F2C"))
        val inactiveBg = ColorStateList.valueOf(Color.parseColor("#E5E7E6"))
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#555555")

        val buttons = listOf(
            btnFilterVehicleAll, btnFilterVehicleMotorcycle, btnFilterVehicleVan,
            btnFilterVehicleAvailable, btnFilterVehicleInUse, btnFilterVehicleMaintenance
        )
        val filters = listOf("All", "Motorcycle", "Delivery Van", "Available", "In Use", "Under Maintenance")

        for (i in buttons.indices) {
            val btn = buttons[i]
            val f = filters[i]
            val isActive = (currentVehicleFilter == f)
            btn.backgroundTintList = if (isActive) activeBg else inactiveBg
            btn.setTextColor(if (isActive) activeText else inactiveText)
        }
    }

    private fun switchTab(tab: String) {
        val activeBg = Color.parseColor("#0B4F2C")
        val inactiveBg = Color.TRANSPARENT
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#666666")

        if (tab == "Riders") {
            tabRidersBtn.setBackgroundColor(activeBg)
            tabRidersBtn.setTextColor(activeText)
            tabVehiclesBtn.setBackgroundColor(inactiveBg)
            tabVehiclesBtn.setTextColor(inactiveText)

            sectionRiders.visibility = View.VISIBLE
            sectionVehicles.visibility = View.GONE
        } else {
            tabVehiclesBtn.setBackgroundColor(activeBg)
            tabVehiclesBtn.setTextColor(activeText)
            tabRidersBtn.setBackgroundColor(inactiveBg)
            tabRidersBtn.setTextColor(inactiveText)

            sectionRiders.visibility = View.GONE
            sectionVehicles.visibility = View.VISIBLE
        }
    }

    private fun updateOverviewStats() {
        val totalRiders = ridersList.size
        val availableRiders = ridersList.count { it.status.equals("Available", ignoreCase = true) }
        val onDeliveryRiders = ridersList.count { it.status.equals("On Delivery", ignoreCase = true) }
        val offDutyRiders = ridersList.count { it.status.equals("Off Duty", ignoreCase = true) }

        val totalVehicles = vehiclesList.size
        val availableVehicles = vehiclesList.count { it.status.equals("Available", ignoreCase = true) }
        val vehiclesInUse = vehiclesList.count { it.status.equals("In Use", ignoreCase = true) }
        val vehiclesMaintenance = vehiclesList.count { it.status.equals("Under Maintenance", ignoreCase = true) }

        tvTotalRiders.text = totalRiders.toString()
        tvAvailableRiders.text = availableRiders.toString()
        tvOnDeliveryRiders.text = onDeliveryRiders.toString()
        tvOffDutyRiders.text = offDutyRiders.toString()

        tvTotalVehicles.text = totalVehicles.toString()
        tvAvailableVehicles.text = availableVehicles.toString()
        tvVehiclesInUse.text = vehiclesInUse.toString()
        tvVehiclesMaintenance.text = vehiclesMaintenance.toString()
    }

    private fun renderRidersList() {
        riderListContainer.removeAllViews()

        val filteredList = ridersList.filter { rider ->
            val matchesFilter = when (currentRiderFilter) {
                "All" -> true
                "Available" -> rider.status.equals("Available", ignoreCase = true)
                "On Delivery" -> rider.status.equals("On Delivery", ignoreCase = true)
                "Off Duty" -> rider.status.equals("Off Duty", ignoreCase = true)
                else -> true
            }

            val query = riderSearchQuery.lowercase()
            val matchesSearch = query.isEmpty() ||
                    rider.fullName.lowercase().contains(query) ||
                    rider.riderId.lowercase().contains(query) ||
                    rider.phone.contains(query)

            matchesFilter && matchesSearch
        }

        if (filteredList.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No riders match your search or filter."
                setTextColor(Color.parseColor("#777777"))
                textSize = 14f
                setPadding(16, 24, 16, 24)
            }
            riderListContainer.addView(emptyTv)
            return
        }

        for (rider in filteredList) {
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

            val headerRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val titleView = TextView(this).apply {
                text = "${rider.riderId} • ${rider.fullName}"
                setTextColor(Color.parseColor("#111111"))
                setTypeface(null, Typeface.BOLD)
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val statusBadge = TextView(this).apply {
                text = rider.status
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
                setPadding(16, 4, 16, 4)
                when (rider.status) {
                    "On Delivery" -> {
                        setBackgroundColor(Color.parseColor("#E0F2FE"))
                        setTextColor(Color.parseColor("#0284C7"))
                    }
                    "Available" -> {
                        setBackgroundColor(Color.parseColor("#DCFCE7"))
                        setTextColor(Color.parseColor("#16A34A"))
                    }
                    else -> { // Off Duty
                        setBackgroundColor(Color.parseColor("#F1F5F9"))
                        setTextColor(Color.parseColor("#64748B"))
                    }
                }
            }

            headerRow.addView(titleView)
            headerRow.addView(statusBadge)
            card.addView(headerRow)

            val phoneView = TextView(this).apply {
                text = "Phone: ${rider.phone}"
                setTextColor(Color.parseColor("#555555"))
                textSize = 13f
                setPadding(0, 8, 0, 0)
            }
            card.addView(phoneView)

            val vehicleView = TextView(this).apply {
                val vId = rider.assignedVehicleId
                val assignedV = vehiclesList.find { it.vehicleId == vId }
                text = if (vId != null) "Assigned Vehicle: $vId (${assignedV?.plateNumber ?: ""})" else "Assigned Vehicle: None"
                setTextColor(Color.parseColor("#0B4F2C"))
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 4, 0, 0)
            }
            card.addView(vehicleView)

            val actionsBar = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 0)
            }

            val btnViewEdit = Button(this).apply {
                text = "Edit"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#0B4F2C"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                lp.setMargins(0, 0, 8, 0)
                layoutParams = lp
                setOnClickListener { showEditRiderDialog(rider) }
            }

            val btnToggleStatus = Button(this).apply {
                text = "Status"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#0284C7"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                lp.setMargins(0, 0, 8, 0)
                layoutParams = lp
                setOnClickListener {
                    rider.status = when (rider.status) {
                        "Available" -> "On Delivery"
                        "On Delivery" -> "Off Duty"
                        else -> "Available"
                    }
                    updateOverviewStats()
                    renderRidersList()
                    Toast.makeText(this@AssignRiderVehicleActivity, "${rider.fullName} status updated: ${rider.status}", Toast.LENGTH_SHORT).show()
                }
            }

            val btnAssignVehicle = Button(this).apply {
                text = "Assign"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#16A34A"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                lp.setMargins(0, 0, 8, 0)
                layoutParams = lp
                setOnClickListener { showAssignVehicleDialog(rider) }
            }

            val btnDelete = Button(this).apply {
                text = "Delete"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#DC2626"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                layoutParams = lp
                setOnClickListener {
                    // Active delivery & vehicle assignment deletion guard
                    if (rider.status.equals("On Delivery", ignoreCase = true) || rider.assignedVehicleId != null) {
                        AlertDialog.Builder(this@AssignRiderVehicleActivity)
                            .setTitle("Cannot Delete Rider!")
                            .setMessage("Rider ${rider.fullName} currently has active deliveries or an assigned vehicle (${rider.assignedVehicleId ?: ""}). Please resolve active assignments before deleting.")
                            .setPositiveButton("OK", null)
                            .show()
                        return@setOnClickListener
                    }

                    ridersList.remove(rider)
                    updateOverviewStats()
                    renderRidersList()
                    renderVehiclesList()
                    Toast.makeText(this@AssignRiderVehicleActivity, "Rider ${rider.fullName} deleted", Toast.LENGTH_SHORT).show()
                }
            }

            actionsBar.addView(btnViewEdit)
            actionsBar.addView(btnToggleStatus)
            actionsBar.addView(btnAssignVehicle)
            actionsBar.addView(btnDelete)

            card.addView(actionsBar)
            riderListContainer.addView(card)
        }
    }

    private fun renderVehiclesList() {
        vehicleListContainer.removeAllViews()

        val filteredList = vehiclesList.filter { vehicle ->
            val matchesFilter = when (currentVehicleFilter) {
                "All" -> true
                "Motorcycle" -> vehicle.vehicleType.equals("Motorcycle", ignoreCase = true)
                "Delivery Van" -> vehicle.vehicleType.equals("Delivery Van", ignoreCase = true)
                "Available" -> vehicle.status.equals("Available", ignoreCase = true)
                "In Use" -> vehicle.status.equals("In Use", ignoreCase = true)
                "Under Maintenance" -> vehicle.status.equals("Under Maintenance", ignoreCase = true)
                else -> true
            }

            val query = vehicleSearchQuery.lowercase()
            val matchesSearch = query.isEmpty() ||
                    vehicle.plateNumber.lowercase().contains(query) ||
                    vehicle.vehicleId.lowercase().contains(query) ||
                    vehicle.vehicleType.lowercase().contains(query)

            matchesFilter && matchesSearch
        }

        if (filteredList.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No vehicles match your search or filter."
                setTextColor(Color.parseColor("#777777"))
                textSize = 14f
                setPadding(16, 24, 16, 24)
            }
            vehicleListContainer.addView(emptyTv)
            return
        }

        for (vehicle in filteredList) {
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

            val headerRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val titleView = TextView(this).apply {
                text = "${vehicle.vehicleId} • ${vehicle.plateNumber}"
                setTextColor(Color.parseColor("#111111"))
                setTypeface(null, Typeface.BOLD)
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val statusBadge = TextView(this).apply {
                text = vehicle.status
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
                setPadding(16, 4, 16, 4)
                when (vehicle.status) {
                    "In Use" -> {
                        setBackgroundColor(Color.parseColor("#FEF3C7"))
                        setTextColor(Color.parseColor("#D97706"))
                    }
                    "Under Maintenance" -> {
                        setBackgroundColor(Color.parseColor("#FEE2E2"))
                        setTextColor(Color.parseColor("#DC2626"))
                    }
                    else -> { // "Available"
                        setBackgroundColor(Color.parseColor("#DCFCE7"))
                        setTextColor(Color.parseColor("#16A34A"))
                    }
                }
            }

            headerRow.addView(titleView)
            headerRow.addView(statusBadge)
            card.addView(headerRow)

            val typeView = TextView(this).apply {
                text = "Type: ${vehicle.vehicleType}"
                setTextColor(Color.parseColor("#555555"))
                textSize = 13f
                setPadding(0, 8, 0, 0)
            }
            card.addView(typeView)

            val riderView = TextView(this).apply {
                val assignedRider = ridersList.find { it.riderId == vehicle.assignedRiderId }
                text = if (assignedRider != null) "Assigned Rider: ${assignedRider.fullName} (${assignedRider.riderId})" else "Assigned Rider: None"
                setTextColor(Color.parseColor("#0B4F2C"))
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 4, 0, 0)
            }
            card.addView(riderView)

            val actionsBar = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 0)
            }

            val btnEdit = Button(this).apply {
                text = "Edit"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#0B4F2C"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                lp.setMargins(0, 0, 8, 0)
                layoutParams = lp
                setOnClickListener { showEditVehicleDialog(vehicle) }
            }

            val btnToggleStatus = Button(this).apply {
                text = "Status"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#0284C7"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                lp.setMargins(0, 0, 8, 0)
                layoutParams = lp
                setOnClickListener {
                    vehicle.status = when (vehicle.status) {
                        "Available" -> "In Use"
                        "In Use" -> "Under Maintenance"
                        else -> "Available"
                    }
                    updateOverviewStats()
                    renderVehiclesList()
                    Toast.makeText(this@AssignRiderVehicleActivity, "${vehicle.plateNumber} status: ${vehicle.status}", Toast.LENGTH_SHORT).show()
                }
            }

            val btnAssignRider = Button(this).apply {
                text = "Assign"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#16A34A"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                lp.setMargins(0, 0, 8, 0)
                layoutParams = lp
                setOnClickListener { showAssignRiderToVehicleDialog(vehicle) }
            }

            val btnDelete = Button(this).apply {
                text = "Delete"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#DC2626"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                layoutParams = lp
                setOnClickListener {
                    // Active delivery & assignment deletion guard
                    if (vehicle.status.equals("In Use", ignoreCase = true) || vehicle.assignedRiderId != null) {
                        AlertDialog.Builder(this@AssignRiderVehicleActivity)
                            .setTitle("Cannot Delete Vehicle!")
                            .setMessage("Vehicle ${vehicle.plateNumber} is currently in use or assigned to an active delivery (${vehicle.assignedRiderId ?: ""}). Please resolve active deliveries before deleting.")
                            .setPositiveButton("OK", null)
                            .show()
                        return@setOnClickListener
                    }

                    vehiclesList.remove(vehicle)
                    updateOverviewStats()
                    renderRidersList()
                    renderVehiclesList()
                    Toast.makeText(this@AssignRiderVehicleActivity, "Vehicle ${vehicle.plateNumber} deleted", Toast.LENGTH_SHORT).show()
                }
            }

            actionsBar.addView(btnEdit)
            actionsBar.addView(btnToggleStatus)
            actionsBar.addView(btnAssignRider)
            actionsBar.addView(btnDelete)

            card.addView(actionsBar)
            vehicleListContainer.addView(card)
        }
    }

    private fun showAddRiderDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val nameInput = EditText(this).apply { hint = "Full Name" }
        val phoneInput = EditText(this).apply { hint = "Phone Number" }
        val emailInput = EditText(this).apply { hint = "Email Address (Optional)" }

        layout.addView(nameInput)
        layout.addView(phoneInput)
        layout.addView(emailInput)

        AlertDialog.Builder(this)
            .setTitle("Add New Rider")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    val newId = "RDR-${System.currentTimeMillis().toString().takeLast(4)}"
                    ridersList.add(Rider(newId, name, phone, null, "Available", email))
                    updateOverviewStats()
                    renderRidersList()
                    Toast.makeText(this, "Rider $name Added Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditRiderDialog(rider: Rider) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val nameInput = EditText(this).apply { setText(rider.fullName) }
        val phoneInput = EditText(this).apply { setText(rider.phone) }
        val emailInput = EditText(this).apply { setText(rider.email) }

        layout.addView(nameInput)
        layout.addView(phoneInput)
        layout.addView(emailInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Rider Details")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    rider.fullName = name
                    rider.phone = phone
                    rider.email = email
                    renderRidersList()
                    Toast.makeText(this, "Rider Updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddVehicleDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val plateInput = EditText(this).apply { hint = "Plate Number (e.g. ABC-123)" }
        val typeInput = EditText(this).apply { hint = "Type (Motorcycle / Delivery Van)" }
        val weightInput = EditText(this).apply { hint = "Max Cargo Weight in kg (e.g. 50)" }
        val capacityInput = EditText(this).apply { hint = "Cargo Dimensions / Capacity" }

        layout.addView(plateInput)
        layout.addView(typeInput)
        layout.addView(weightInput)
        layout.addView(capacityInput)

        AlertDialog.Builder(this)
            .setTitle("Add New Vehicle")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val plate = plateInput.text.toString().trim()
                val type = typeInput.text.toString().trim()
                val maxW = weightInput.text.toString().trim().toDoubleOrNull() ?: 50.0
                val cap = capacityInput.text.toString().trim().ifEmpty { "Standard Box" }

                if (plate.isNotEmpty() && type.isNotEmpty()) {
                    val isDuplicate = vehiclesList.any { it.plateNumber.equals(plate, ignoreCase = true) }
                    if (isDuplicate) {
                        Toast.makeText(this, "Vehicle with plate number $plate already exists!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    val newId = "VHC-${System.currentTimeMillis().toString().takeLast(4)}"
                    val finalType = if (type.lowercase().contains("van")) "Delivery Van" else "Motorcycle"
                    vehiclesList.add(Vehicle(newId, plate, finalType, null, "Available", maxW, cap))
                    updateOverviewStats()
                    renderVehiclesList()
                    Toast.makeText(this, "Vehicle $plate Added Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditVehicleDialog(vehicle: Vehicle) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val plateInput = EditText(this).apply { setText(vehicle.plateNumber) }
        val typeInput = EditText(this).apply { setText(vehicle.vehicleType) }
        val weightInput = EditText(this).apply { setText(vehicle.maxWeightKg.toString()) }
        val capacityInput = EditText(this).apply { setText(vehicle.cargoCapacity) }

        layout.addView(plateInput)
        layout.addView(typeInput)
        layout.addView(weightInput)
        layout.addView(capacityInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Vehicle Details")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val plate = plateInput.text.toString().trim()
                val type = typeInput.text.toString().trim()
                val maxW = weightInput.text.toString().trim().toDoubleOrNull() ?: vehicle.maxWeightKg
                val cap = capacityInput.text.toString().trim().ifEmpty { vehicle.cargoCapacity }

                if (plate.isNotEmpty() && type.isNotEmpty()) {
                    val isDuplicate = vehiclesList.any { 
                        it.vehicleId != vehicle.vehicleId && it.plateNumber.equals(plate, ignoreCase = true) 
                    }
                    if (isDuplicate) {
                        Toast.makeText(this, "Vehicle with plate number $plate already exists!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    vehicle.plateNumber = plate
                    vehicle.vehicleType = if (type.lowercase().contains("van")) "Delivery Van" else "Motorcycle"
                    vehicle.maxWeightKg = maxW
                    vehicle.cargoCapacity = cap
                    renderVehiclesList()
                    Toast.makeText(this, "Vehicle Updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAssignVehicleDialog(rider: Rider) {
        if (rider.status.equals("Off Duty", ignoreCase = true)) {
            AlertDialog.Builder(this)
                .setTitle("Off Duty Rider!")
                .setMessage("Rider ${rider.fullName} is currently Off Duty. Off Duty riders cannot receive vehicle assignments. Please change rider status to Available first.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val optionsList = mutableListOf<String>()
        val selectableVehicles = mutableListOf<Vehicle?>()

        optionsList.add("[Clear / Remove Vehicle Assignment]")
        selectableVehicles.add(null)

        for (v in vehiclesList) {
            if (v.status.equals("Under Maintenance", ignoreCase = true)) {
                continue
            }

            val assignedTo = ridersList.find { it.riderId == v.assignedRiderId }
            if (v.assignedRiderId != null && v.assignedRiderId != rider.riderId) {
                optionsList.add("${v.vehicleId} (${v.plateNumber}) - Occupied by ${assignedTo?.fullName ?: v.assignedRiderId}")
            } else {
                optionsList.add("${v.vehicleId} (${v.plateNumber}) - ${v.vehicleType} [${v.status}]")
            }
            selectableVehicles.add(v)
        }

        AlertDialog.Builder(this)
            .setTitle("Assign Vehicle to ${rider.fullName}")
            .setItems(optionsList.toTypedArray()) { _, which ->
                val selectedV = selectableVehicles[which]

                if (selectedV == null) {
                    val prevV = vehiclesList.find { t -> t.vehicleId == rider.assignedVehicleId }
                    if (prevV != null) {
                        prevV.assignedRiderId = null
                        prevV.status = "Available"
                    }
                    rider.assignedVehicleId = null
                    rider.status = "Available"

                    updateOverviewStats()
                    renderRidersList()
                    renderVehiclesList()
                    Toast.makeText(this, "Vehicle assignment cleared for ${rider.fullName}!", Toast.LENGTH_LONG).show()
                } else {
                    if (selectedV.assignedRiderId != null && selectedV.assignedRiderId != rider.riderId) {
                        val currentRiderName = ridersList.find { it.riderId == selectedV.assignedRiderId }?.fullName ?: selectedV.assignedRiderId
                        AlertDialog.Builder(this)
                            .setTitle("Vehicle Occupied!")
                            .setMessage("Vehicle ${selectedV.plateNumber} is already assigned to $currentRiderName. You cannot assign an occupied vehicle to another rider.")
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        val deliveryId = intent.getStringExtra("deliveryId")
                        val pkgWeight = intent.getDoubleExtra("packageWeightKg", 1.0)

                        if (selectedV != null && !deliveryId.isNullOrEmpty()) {
                            // Cargo weight & capacity validation
                            if (pkgWeight > selectedV.maxWeightKg) {
                                AlertDialog.Builder(this)
                                    .setTitle("Vehicle Capacity Exceeded!")
                                    .setMessage("Package weight (${pkgWeight}kg) exceeds maximum capacity of ${selectedV.plateNumber} (${selectedV.maxWeightKg}kg). Please select a Delivery Van or larger capacity vehicle.")
                                    .setPositiveButton("OK", null)
                                    .show()
                                return@setItems
                            }

                            // Update Firestore Delivery record
                            val updates = mapOf(
                                "status" to "Assigned to Rider",
                                "assignedRiderId" to rider.riderId,
                                "assignedRiderName" to rider.fullName,
                                "assignedRiderContact" to rider.phone,
                                "assignedRiderEmail" to rider.email,
                                "assignedVehicle" to "${selectedV.vehicleId} (${selectedV.plateNumber})"
                            )

                            FirebaseFirestore.getInstance().collection("deliveries").document(deliveryId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Delivery $deliveryId assigned to ${rider.fullName} on ${selectedV.plateNumber}!", Toast.LENGTH_LONG).show()
                                }
                        }

                        val prevV = vehiclesList.find { t -> t.vehicleId == rider.assignedVehicleId }
                        if (prevV != null) {
                            prevV.assignedRiderId = null
                            prevV.status = "Available"
                        }

                        rider.assignedVehicleId = selectedV.vehicleId
                        rider.status = "On Delivery"
                        selectedV.assignedRiderId = rider.riderId
                        selectedV.status = "In Use"

                        updateOverviewStats()
                        renderRidersList()
                        renderVehiclesList()

                        Toast.makeText(this, "Vehicle ${selectedV.plateNumber} Assigned to ${rider.fullName}!", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAssignRiderToVehicleDialog(vehicle: Vehicle) {
        if (vehicle.status.equals("Under Maintenance", ignoreCase = true)) {
            AlertDialog.Builder(this)
                .setTitle("Vehicle Under Maintenance!")
                .setMessage("Vehicle ${vehicle.plateNumber} is currently Under Maintenance and cannot be assigned to any rider. Please change vehicle status to Available first.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val optionsList = mutableListOf<String>()
        val selectableRiders = mutableListOf<Rider?>()

        optionsList.add("[Clear / Remove Rider Assignment]")
        selectableRiders.add(null)

        for (r in ridersList) {
            if (r.status.equals("Off Duty", ignoreCase = true)) {
                continue
            }

            if (r.assignedVehicleId != null && r.assignedVehicleId != vehicle.vehicleId) {
                optionsList.add("${r.riderId} • ${r.fullName} (Already assigned to ${r.assignedVehicleId})")
            } else {
                optionsList.add("${r.riderId} • ${r.fullName} [${r.status}]")
            }
            selectableRiders.add(r)
        }

        AlertDialog.Builder(this)
            .setTitle("Assign Rider to Vehicle ${vehicle.plateNumber}")
            .setItems(optionsList.toTypedArray()) { _, which ->
                val selectedRider = selectableRiders[which]

                if (selectedRider == null) {
                    val prevRider = ridersList.find { it.riderId == vehicle.assignedRiderId }
                    if (prevRider != null) {
                        prevRider.assignedVehicleId = null
                        prevRider.status = "Available"
                    }
                    vehicle.assignedRiderId = null
                    vehicle.status = "Available"

                    updateOverviewStats()
                    renderRidersList()
                    renderVehiclesList()
                    Toast.makeText(this, "Rider assignment cleared for vehicle ${vehicle.plateNumber}!", Toast.LENGTH_LONG).show()
                } else {
                    val prevV = vehiclesList.find { it.vehicleId == selectedRider.assignedVehicleId }
                    if (prevV != null) {
                        prevV.assignedRiderId = null
                        prevV.status = "Available"
                    }

                    selectedRider.assignedVehicleId = vehicle.vehicleId
                    selectedRider.status = "On Delivery"
                    vehicle.assignedRiderId = selectedRider.riderId
                    vehicle.status = "In Use"

                    updateOverviewStats()
                    renderRidersList()
                    renderVehiclesList()

                    Toast.makeText(this, "Rider ${selectedRider.fullName} Assigned to Vehicle ${vehicle.plateNumber}!", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
