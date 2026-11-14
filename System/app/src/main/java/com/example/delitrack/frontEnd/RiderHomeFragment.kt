package com.example.delitrack.frontEnd

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RiderHomeFragment : Fragment() {

    private lateinit var tvRiderName: TextView
    private lateinit var tvRiderStatusBadge: TextView
    private lateinit var btnToggleRiderStatus: Button

    private lateinit var tvAssignedDeliveries: TextView
    private lateinit var tvPendingAssignments: TextView
    private lateinit var tvActiveDeliveries: TextView
    private lateinit var tvCompletedDeliveries: TextView

    private lateinit var containerRiderJobs: LinearLayout

    private var currentRiderStatus = "Available"
    private var riderId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rider_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvRiderName = view.findViewById(R.id.tvRiderName)
        tvRiderStatusBadge = view.findViewById(R.id.tvRiderStatusBadge)
        btnToggleRiderStatus = view.findViewById(R.id.btnToggleRiderStatus)

        tvAssignedDeliveries = view.findViewById(R.id.tvAssignedDeliveries)
        tvPendingAssignments = view.findViewById(R.id.tvPendingAssignments)
        tvActiveDeliveries = view.findViewById(R.id.tvActiveDeliveries)
        tvCompletedDeliveries = view.findViewById(R.id.tvCompletedDeliveries)

        containerRiderJobs = view.findViewById(R.id.containerRiderJobs)

        loadRiderProfileAndStats()

        btnToggleRiderStatus.setOnClickListener {
            toggleRiderDutyStatus()
        }
    }

    private fun loadRiderProfileAndStats() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) return

        val db = FirebaseFirestore.getInstance()
        val email = currentUser.email ?: ""

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                val name = doc.getString("fullName") ?: "Rider"
                tvRiderName.text = "Welcome, $name!"

                // Fetch status from riders collection
                db.collection("riders")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { query ->
                        if (!isAdded) return@addOnSuccessListener
                        if (!query.isEmpty) {
                            val riderDoc = query.documents[0]
                            riderId = riderDoc.getString("riderId") ?: ""
                            currentRiderStatus = riderDoc.getString("status") ?: "Available"
                            updateRiderStatusBadgeUI(currentRiderStatus)
                        } else {
                            currentRiderStatus = "Available"
                            updateRiderStatusBadgeUI(currentRiderStatus)
                        }
                        loadRiderDeliveriesFromDatabase(email)
                    }
                    .addOnFailureListener {
                        loadRiderDeliveriesFromDatabase(email)
                    }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                tvRiderName.text = "Welcome, Rider!"
                loadRiderDeliveriesFromDatabase(email)
            }
    }

    private fun updateRiderStatusBadgeUI(status: String) {
        tvRiderStatusBadge.text = status
        when (status.lowercase()) {
            "available" -> {
                tvRiderStatusBadge.setBackgroundColor(Color.parseColor("#DCFCE7"))
                tvRiderStatusBadge.setTextColor(Color.parseColor("#16A34A"))
                btnToggleRiderStatus.text = "Set Off Duty"
                btnToggleRiderStatus.setBackgroundColor(Color.parseColor("#DC2626"))
            }
            "on delivery" -> {
                tvRiderStatusBadge.setBackgroundColor(Color.parseColor("#E0F2FE"))
                tvRiderStatusBadge.setTextColor(Color.parseColor("#0284C7"))
                btnToggleRiderStatus.text = "Set Available"
                btnToggleRiderStatus.setBackgroundColor(Color.parseColor("#16A34A"))
            }
            else -> { // Off Duty
                tvRiderStatusBadge.setBackgroundColor(Color.parseColor("#F1F5F9"))
                tvRiderStatusBadge.setTextColor(Color.parseColor("#64748B"))
                btnToggleRiderStatus.text = "Set Available"
                btnToggleRiderStatus.setBackgroundColor(Color.parseColor("#16A34A"))
            }
        }
    }

    private fun toggleRiderDutyStatus() {
        val newStatus = if (currentRiderStatus.equals("Available", ignoreCase = true)) "Off Duty" else "Available"
        val db = FirebaseFirestore.getInstance()

        if (riderId.isNotEmpty()) {
            db.collection("riders").document(riderId)
                .update("status", newStatus)
                .addOnSuccessListener {
                    if (!isAdded) return@addOnSuccessListener
                    currentRiderStatus = newStatus
                    updateRiderStatusBadgeUI(newStatus)
                    Toast.makeText(requireContext(), "Status updated to $newStatus!", Toast.LENGTH_SHORT).show()
                }
        } else {
            currentRiderStatus = newStatus
            updateRiderStatusBadgeUI(newStatus)
            Toast.makeText(requireContext(), "Status updated to $newStatus!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRiderDeliveriesFromDatabase(riderEmail: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("deliveries")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                var assignedCount = 0
                var pendingCount = 0
                var activeCount = 0
                var completedCount = 0

                containerRiderJobs.removeAllViews()
                var hasJobs = false

                for (doc in snapshot) {
                    val data = doc.data
                    val status = data["status"] as? String ?: "Pending"
                    val deliveryId = data["deliveryId"] as? String ?: doc.id
                    val assignedRider = data["assignedRiderEmail"] as? String ?: data["assignedRiderId"] as? String ?: ""

                    // Riders see only their assigned deliveries (or all if admin testing)
                    val isAssignedToMe = (riderId.isNotEmpty() && assignedRider.contains(riderId, ignoreCase = true)) ||
                            (riderEmail.isNotEmpty() && assignedRider.equals(riderEmail, ignoreCase = true)) ||
                            (riderEmail == "admin@delitrack.com")

                    if (isAssignedToMe) {
                        hasJobs = true
                        assignedCount++
                        when (status.lowercase()) {
                            "pending" -> pendingCount++
                            "in transit", "dispatched", "active", "assigned to rider", "assigned" -> activeCount++
                            "delivered", "completed", "delivered & confirmed" -> completedCount++
                        }

                        // Render job card snippet
                        val cardView = layoutInflater.inflate(R.layout.item_delivery_card_snippet, containerRiderJobs, false) as? LinearLayout
                        if (cardView != null) {
                            cardView.findViewById<TextView>(R.id.tvDeliveryId)?.text = deliveryId
                            val tvStatus = cardView.findViewById<TextView>(R.id.tvDeliveryStatus)
                            tvStatus?.text = status

                            val senderName = data["senderName"] as? String ?: data["customerName"] as? String ?: "N/A"
                            val senderPhone = data["senderContact"] as? String ?: data["contactNumber"] as? String ?: ""
                            val receiver = data["receiverName"] as? String ?: "N/A"
                            val receiverPhone = data["receiverContact"] as? String ?: ""

                            val pickup = data["pickupLocation"] as? String ?: data["pickupAddress"] as? String ?: "Nasugbu, Batangas"
                            val dest = data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "Lian, Batangas"
                            val date = data["dateCreated"] as? String ?: "Today"

                            val packageType = data["packageType"] as? String ?: "Lightweight"
                            val packageDesc = data["packageDescription"] as? String ?: "Parcel"
                            val weightKg = data["packageWeightKg"] ?: data["weightKg"] ?: 1.0

                            cardView.findViewById<TextView>(R.id.tvCustomerName)?.text = "Customer: $senderName ${if (senderPhone.isNotEmpty()) "($senderPhone)" else ""}"
                            cardView.findViewById<TextView>(R.id.tvReceiverName)?.text = "Receiver: $receiver ${if (receiverPhone.isNotEmpty()) "($receiverPhone)" else ""}"
                            cardView.findViewById<TextView>(R.id.tvPickupLocation)?.text = "Pickup: $pickup"
                            cardView.findViewById<TextView>(R.id.tvDeliveryDestination)?.text = "Destination: $dest"
                            cardView.findViewById<TextView>(R.id.tvPackageSummary)?.text = "Package: ${weightKg}kg $packageType • $packageDesc"
                            cardView.findViewById<TextView>(R.id.tvDeliveryDate)?.text = "Date: $date"

                            // Wire Rider Actions
                            val actionsLayout = cardView.findViewById<LinearLayout>(R.id.riderActionsLayout)
                            actionsLayout?.visibility = View.VISIBLE

                            cardView.findViewById<Button>(R.id.btnAcceptJob)?.setOnClickListener {
                                updateJobStatus(deliveryId, "Acknowledged")
                            }

                            cardView.findViewById<Button>(R.id.btnStartTransit)?.setOnClickListener {
                                updateJobStatus(deliveryId, "In Transit")
                            }

                            cardView.findViewById<Button>(R.id.btnMarkDelivered)?.setOnClickListener {
                                updateJobStatus(deliveryId, "Delivered")
                            }

                            cardView.findViewById<Button>(R.id.btnMarkFailed)?.setOnClickListener {
                                val failureInput = EditText(requireContext()).apply { hint = "Reason for delivery failure" }
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Mark Delivery Failed ($deliveryId)")
                                    .setView(failureInput)
                                    .setPositiveButton("Submit Failure") { _, _ ->
                                        val reason = failureInput.text.toString().trim()
                                        updateJobStatus(deliveryId, if (reason.isNotEmpty()) "Failed: $reason" else "Delivery Failed")
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            }

                            cardView.findViewById<Button>(R.id.btnReportIssue)?.setOnClickListener {
                                val issueInput = EditText(requireContext()).apply { hint = "Problem description" }
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Report Delivery Issue ($deliveryId)")
                                    .setView(issueInput)
                                    .setPositiveButton("Submit Issue") { _, _ ->
                                        val reason = issueInput.text.toString().trim()
                                        updateJobStatus(deliveryId, if (reason.isNotEmpty()) "Issue Reported: $reason" else "Issue Reported")
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            }

                            containerRiderJobs.addView(cardView)
                        }
                    }
                }

                // Compute exact statistics dynamically
                tvAssignedDeliveries.text = assignedCount.toString()
                tvPendingAssignments.text = pendingCount.toString()
                tvActiveDeliveries.text = activeCount.toString()
                tvCompletedDeliveries.text = completedCount.toString()

                if (!hasJobs) {
                    val emptyTv = TextView(requireContext()).apply {
                        text = "No deliveries currently assigned to you."
                        setPadding(16, 24, 16, 24)
                        setTextColor(Color.parseColor("#777777"))
                    }
                    containerRiderJobs.addView(emptyTv)
                }
            }
            .addOnFailureListener {
                // handle failure
            }
    }

    private fun updateJobStatus(deliveryId: String, newStatus: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("deliveries").document(deliveryId)
            .update("status", newStatus)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                Toast.makeText(requireContext(), "Order $deliveryId marked as $newStatus!", Toast.LENGTH_SHORT).show()
                val currentUser = FirebaseAuth.getInstance().currentUser
                loadRiderDeliveriesFromDatabase(currentUser?.email ?: "")
            }
    }

    override fun onResume() {
        super.onResume()
        val currentUser = FirebaseAuth.getInstance().currentUser
        loadRiderDeliveriesFromDatabase(currentUser?.email ?: "")
    }
}
