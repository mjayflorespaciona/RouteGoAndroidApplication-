package com.example.delitrack.frontEnd

import android.app.AlertDialog
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
import com.example.delitrack.backEnd.data.Rider
import com.example.delitrack.backEnd.data.Vehicle

class AssignRiderVehicleActivity : AppCompatActivity() {

    private val ridersList = mutableListOf(
        Rider("RDR-101", "Mark Santos", "0917 123 4567", "TRK-001", "Active"),
        Rider("RDR-102", "Ana Garcia", "0918 765 4321", "VAN-002", "Active"),
        Rider("RDR-103", "Carlos Mendoza", "0919 222 3333", "MTR-001", "Active"),
        Rider("RDR-104", "David Reyes", "0920 444 5555", "MTR-002", "Active"),
        Rider("RDR-105", "Elena Torres", "0921 666 7777", "MTR-003", "Active"),
        Rider("RDR-106", "Fernando Cruz", "0922 888 9999", "MTR-004", "Active"),
        Rider("RDR-107", "Grace Ramos", "0923 111 2222", "MTR-005", "Active"),
        Rider("RDR-108", "Hector Lopez", "0924 333 4444", "MTR-006", "Active"),
        Rider("RDR-109", "Isabel Aquino", "0925 555 6666", "MTR-007", "Active"),
        Rider("RDR-110", "Jake Bautista", "0926 777 8888", "MTR-008", "Active"),
        Rider("RDR-111", "Karen Diaz", "0927 999 0000", null, "Available"),
        Rider("RDR-112", "Leo Navarro", "0928 123 1111", null, "Available"),
        Rider("RDR-113", "Mia Castelo", "0929 234 2222", null, "Available"),
        Rider("RDR-114", "Nathan Silva", "0930 345 3333", null, "Available"),
        Rider("RDR-115", "Olivia Perez", "0931 456 4444", null, "Available")
    )

    private val vehiclesList = mutableListOf(
        Vehicle("TRK-001", "NKR-1234", "Delivery Van", "RDR-101", "Assigned"),
        Vehicle("VAN-002", "L300-567", "Delivery Van", "RDR-102", "Assigned"),
        Vehicle("MTR-001", "MC-1011", "Motorcycle", "RDR-103", "Assigned"),
        Vehicle("MTR-002", "MC-1022", "Motorcycle", "RDR-104", "Assigned"),
        Vehicle("MTR-003", "MC-1033", "Motorcycle", "RDR-105", "Assigned"),
        Vehicle("MTR-004", "MC-1044", "Motorcycle", "RDR-106", "Assigned"),
        Vehicle("MTR-005", "MC-1055", "Motorcycle", "RDR-107", "Assigned"),
        Vehicle("MTR-006", "MC-1066", "Motorcycle", "RDR-108", "Assigned"),
        Vehicle("MTR-007", "MC-1077", "Motorcycle", "RDR-109", "Assigned"),
        Vehicle("MTR-008", "MC-1088", "Motorcycle", "RDR-110", "Assigned")
    )

    private lateinit var riderListContainer: LinearLayout
    private lateinit var vehicleListContainer: LinearLayout
    private lateinit var sectionRiders: LinearLayout
    private lateinit var sectionVehicles: LinearLayout
    private lateinit var tabRidersBtn: Button
    private lateinit var tabVehiclesBtn: Button

    private lateinit var tvTotalRiders: TextView
    private lateinit var tvActiveRiders: TextView
    private lateinit var tvAvailableRiders: TextView
    private lateinit var tvTotalVehicles: TextView
    private lateinit var tvMotorcycles: TextView
    private lateinit var tvVans: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assign_rider_vehicle)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() }

        riderListContainer = findViewById(R.id.riderListContainer)
        vehicleListContainer = findViewById(R.id.vehicleListContainer)
        sectionRiders = findViewById(R.id.sectionRiders)
        sectionVehicles = findViewById(R.id.sectionVehicles)
        tabRidersBtn = findViewById(R.id.tabRidersBtn)
        tabVehiclesBtn = findViewById(R.id.tabVehiclesBtn)

        tvTotalRiders = findViewById(R.id.tvTotalRiders)
        tvActiveRiders = findViewById(R.id.tvActiveRiders)
        tvAvailableRiders = findViewById(R.id.tvAvailableRiders)
        tvTotalVehicles = findViewById(R.id.tvTotalVehicles)
        tvMotorcycles = findViewById(R.id.tvMotorcycles)
        tvVans = findViewById(R.id.tvVans)

        tabRidersBtn.setOnClickListener { switchTab("Riders") }
        tabVehiclesBtn.setOnClickListener { switchTab("Vehicles") }

        findViewById<Button>(R.id.btnAddRider).setOnClickListener { showAddRiderDialog() }
        findViewById<Button>(R.id.btnAddVehicle).setOnClickListener { showAddVehicleDialog() }

        updateOverviewStats()
        renderRidersList()
        renderVehiclesList()
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
        val activeRiders = ridersList.count { it.status == "Active" }
        val availableRiders = ridersList.count { it.status == "Available" }

        val totalVehicles = vehiclesList.size
        val motorcycles = vehiclesList.count { it.vehicleType == "Motorcycle" }
        val vans = vehiclesList.count { it.vehicleType == "Delivery Van" }

        tvTotalRiders.text = totalRiders.toString()
        tvActiveRiders.text = activeRiders.toString()
        tvAvailableRiders.text = availableRiders.toString()

        tvTotalVehicles.text = totalVehicles.toString()
        tvMotorcycles.text = motorcycles.toString()
        tvVans.text = vans.toString()
    }

    private fun renderRidersList() {
        riderListContainer.removeAllViews()

        for (rider in ridersList) {
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
                if (rider.status == "Active") {
                    setBackgroundColor(Color.parseColor("#E0F2FE"))
                    setTextColor(Color.parseColor("#0284C7"))
                } else if (rider.status == "Available") {
                    setBackgroundColor(Color.parseColor("#DCFCE7"))
                    setTextColor(Color.parseColor("#16A34A"))
                } else {
                    setBackgroundColor(Color.parseColor("#F1F5F9"))
                    setTextColor(Color.parseColor("#64748B"))
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
                text = if (vId != null) "Vehicle: $vId" else "Vehicle: None Assigned"
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
                        "Available" -> "Active"
                        "Active" -> "Offline"
                        else -> "Available"
                    }
                    updateOverviewStats()
                    renderRidersList()
                    Toast.makeText(this@AssignRiderVehicleActivity, "${rider.fullName} status: ${rider.status}", Toast.LENGTH_SHORT).show()
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
                    ridersList.remove(rider)
                    updateOverviewStats()
                    renderRidersList()
                    Toast.makeText(this@AssignRiderVehicleActivity, "Rider deleted", Toast.LENGTH_SHORT).show()
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

        for (vehicle in vehiclesList) {
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
                if (vehicle.status == "Assigned") {
                    setBackgroundColor(Color.parseColor("#FEF3C7"))
                    setTextColor(Color.parseColor("#D97706"))
                } else {
                    setBackgroundColor(Color.parseColor("#DCFCE7"))
                    setTextColor(Color.parseColor("#16A34A"))
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
                text = if (assignedRider != null) "Rider: ${assignedRider.fullName} (${assignedRider.riderId})" else "Rider: None Assigned"
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

            val btnDelete = Button(this).apply {
                text = "Delete"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#DC2626"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                layoutParams = lp
                setOnClickListener {
                    vehiclesList.remove(vehicle)
                    updateOverviewStats()
                    renderVehiclesList()
                    Toast.makeText(this@AssignRiderVehicleActivity, "Vehicle deleted", Toast.LENGTH_SHORT).show()
                }
            }

            actionsBar.addView(btnEdit)
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

        layout.addView(nameInput)
        layout.addView(phoneInput)

        AlertDialog.Builder(this)
            .setTitle("Add New Rider")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    val newId = "RDR-${100 + ridersList.size + 1}"
                    ridersList.add(Rider(newId, name, phone, null, "Available"))
                    updateOverviewStats()
                    renderRidersList()
                    Toast.makeText(this, "Rider $name Added Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
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

        layout.addView(nameInput)
        layout.addView(phoneInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Rider Details")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    rider.fullName = name
                    rider.phone = phone
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

        layout.addView(plateInput)
        layout.addView(typeInput)

        AlertDialog.Builder(this)
            .setTitle("Add New Vehicle")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val plate = plateInput.text.toString().trim()
                val type = typeInput.text.toString().trim()
                if (plate.isNotEmpty() && type.isNotEmpty()) {
                    val newId = "VHC-${100 + vehiclesList.size + 1}"
                    vehiclesList.add(Vehicle(newId, plate, type, null, "Available"))
                    updateOverviewStats()
                    renderVehiclesList()
                    Toast.makeText(this, "Vehicle $plate Added Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
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

        layout.addView(plateInput)
        layout.addView(typeInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Vehicle Details")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val plate = plateInput.text.toString().trim()
                val type = typeInput.text.toString().trim()
                if (plate.isNotEmpty() && type.isNotEmpty()) {
                    vehicle.plateNumber = plate
                    vehicle.vehicleType = type
                    renderVehiclesList()
                    Toast.makeText(this, "Vehicle Updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAssignVehicleDialog(rider: Rider) {
        val vehicleOptions = vehiclesList.map { v ->
            val assignedTo = ridersList.find { it.riderId == v.assignedRiderId }
            if (v.assignedRiderId != null && v.assignedRiderId != rider.riderId) {
                "${v.vehicleId} (${v.plateNumber}) - Occupied by ${assignedTo?.fullName ?: v.assignedRiderId}"
            } else {
                "${v.vehicleId} (${v.plateNumber}) - ${v.vehicleType}"
            }
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Assign Vehicle to ${rider.fullName}")
            .setItems(vehicleOptions) { _, which ->
                val selectedV = vehiclesList[which]

                // OCCUPIED VEHICLE GUARD: Prevent assigning an occupied vehicle to another rider!
                if (selectedV.assignedRiderId != null && selectedV.assignedRiderId != rider.riderId) {
                    val currentRiderName = ridersList.find { it.riderId == selectedV.assignedRiderId }?.fullName ?: selectedV.assignedRiderId
                    AlertDialog.Builder(this)
                        .setTitle("Vehicle Occupied!")
                        .setMessage("Vehicle ${selectedV.plateNumber} is already assigned to $currentRiderName. You cannot assign an occupied vehicle to another rider.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    val prevV = vehiclesList.find { it.vehicleId == rider.assignedVehicleId }
                    if (prevV != null) {
                        prevV.assignedRiderId = null
                        prevV.status = "Available"
                    }

                    rider.assignedVehicleId = selectedV.vehicleId
                    rider.status = "Active"
                    selectedV.assignedRiderId = rider.riderId
                    selectedV.status = "Assigned"

                    updateOverviewStats()
                    renderRidersList()
                    renderVehiclesList()

                    Toast.makeText(this, "Vehicle ${selectedV.plateNumber} Assigned to ${rider.fullName}!", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
