package com.example.delitrack.frontEnd

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeliveriesFragment : Fragment() {

    private var currentFilter = "All"
    private var searchQuery = ""

    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterPending: Button
    private lateinit var btnFilterConfirmed: Button
    private lateinit var btnFilterAssigned: Button
    private lateinit var btnFilterInTransit: Button
    private lateinit var btnFilterDelivered: Button
    private lateinit var searchField: EditText
    private lateinit var recyclerViewDeliveries: RecyclerView
    private lateinit var tvEmptyDeliveries: TextView

    private val allDeliveries = mutableListOf<Map<String, Any>>()
    private val displayedDeliveries = mutableListOf<Map<String, Any>>()
    private lateinit var deliveriesAdapter: DeliveriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deliveries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnFilterAll = view.findViewById(R.id.btnFilterAll)
        btnFilterPending = view.findViewById(R.id.btnFilterPending)
        btnFilterConfirmed = view.findViewById(R.id.btnFilterConfirmed)
        btnFilterAssigned = view.findViewById(R.id.btnFilterAssigned)
        btnFilterInTransit = view.findViewById(R.id.btnFilterInTransit)
        btnFilterDelivered = view.findViewById(R.id.btnFilterDelivered)
        searchField = view.findViewById(R.id.searchField)
        recyclerViewDeliveries = view.findViewById(R.id.recyclerViewDeliveries)
        tvEmptyDeliveries = view.findViewById(R.id.tvEmptyDeliveries)

        val isAdmin = activity is AdminDashboardActivity
        val isRider = activity is RiderDashboardActivity

        recyclerViewDeliveries.layoutManager = LinearLayoutManager(requireContext())
        deliveriesAdapter = DeliveriesAdapter(displayedDeliveries, isAdmin, isRider, { delivery ->
            val deliveryId = delivery["deliveryId"] as? String ?: ""
            val intent = Intent(requireContext(), DeliveryTrackingActivity::class.java)
            intent.putExtra("deliveryId", deliveryId)
            startActivity(intent)
        }, { deliveryId, newStatus ->
            updateDeliveryStatusInFirestore(deliveryId, newStatus)
        })
        recyclerViewDeliveries.adapter = deliveriesAdapter

        btnFilterAll.setOnClickListener { setFilter("All") }
        btnFilterPending.setOnClickListener { setFilter("Pending") }
        btnFilterConfirmed.setOnClickListener { setFilter("Confirmed") }
        btnFilterAssigned.setOnClickListener { setFilter("Assigned to Rider") }
        btnFilterInTransit.setOnClickListener { setFilter("In Transit") }
        btnFilterDelivered.setOnClickListener { setFilter("Delivered") }

        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterAndRenderDeliveries()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadDeliveries()
    }

    private fun setFilter(filter: String) {
        currentFilter = filter
        updateFilterButtonStyles()
        filterAndRenderDeliveries()
    }

    private fun updateFilterButtonStyles() {
        val activeBg = ColorStateList.valueOf(Color.parseColor("#0B4F2C"))
        val inactiveBg = ColorStateList.valueOf(Color.parseColor("#E5E7E6"))
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#555555")

        val buttons = listOf(btnFilterAll, btnFilterPending, btnFilterConfirmed, btnFilterAssigned, btnFilterInTransit, btnFilterDelivered)
        val filters = listOf("All", "Pending", "Confirmed", "Assigned to Rider", "In Transit", "Delivered")

        for (i in buttons.indices) {
            val btn = buttons[i]
            val f = filters[i]
            val isActive = (currentFilter == f)
            btn.backgroundTintList = if (isActive) activeBg else inactiveBg
            btn.setTextColor(if (isActive) activeText else inactiveText)
        }
    }

    private fun updateDeliveryStatusInFirestore(deliveryId: String, newStatus: String) {
        val db = FirebaseFirestore.getInstance()
        val timestampStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
        val historyEntry = mapOf("status" to newStatus, "timestamp" to timestampStr)

        val updates = mapOf(
            "status" to newStatus,
            "lastUpdated" to timestampStr,
            "statusHistory" to FieldValue.arrayUnion(historyEntry)
        )

        db.collection("deliveries").document(deliveryId)
            .update(updates)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                Toast.makeText(requireContext(), "Delivery $deliveryId updated to $newStatus!", Toast.LENGTH_SHORT).show()
                loadDeliveries()
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Toast.makeText(requireContext(), "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDeliveries() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "default_customer"
        val isRiderOrAdmin = activity is RiderDashboardActivity || activity is AdminDashboardActivity || currentUser?.email == "admin@delitrack.com"
        val db = FirebaseFirestore.getInstance()

        db.collection("deliveries")
            .get()
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                allDeliveries.clear()

                for (document in result) {
                    val data = document.data
                    val custId = data["customerId"] as? String ?: ""
                    val custEmail = data["customerEmail"] as? String ?: ""

                    val isOwner = (custId == userId) || (currentUser?.email != null && custEmail.equals(currentUser.email, ignoreCase = true))

                    if (isOwner || isRiderOrAdmin) {
                        allDeliveries.add(data)
                    }
                }
                filterAndRenderDeliveries()
            }
            .addOnFailureListener {
                // handle failure gracefully
            }
    }

    private fun filterAndRenderDeliveries() {
        if (!isAdded) return

        val filtered = allDeliveries.filter { data ->
            val status = data["status"] as? String ?: "Pending"
            val deliveryId = (data["deliveryId"] as? String ?: "").lowercase()
            val pickup = (data["pickupAddress"] as? String ?: data["pickupLocation"] as? String ?: "").lowercase()
            val dest = (data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "").lowercase()
            val receiver = (data["receiverName"] as? String ?: data["customerName"] as? String ?: "").lowercase()

            val matchesFilter = when (currentFilter) {
                "All" -> true
                "Pending" -> status.equals("Pending", ignoreCase = true)
                "Confirmed" -> status.equals("Confirmed", ignoreCase = true)
                "Assigned to Rider" -> status.equals("Assigned to Rider", ignoreCase = true) || status.equals("Assigned", ignoreCase = true)
                "In Transit" -> status.equals("In Transit", ignoreCase = true) || status.equals("Dispatched", ignoreCase = true) || status.equals("Active", ignoreCase = true)
                "Delivered" -> status.equals("Delivered", ignoreCase = true) || status.equals("Completed", ignoreCase = true)
                else -> true
            }

            val matchesSearch = searchQuery.isEmpty() ||
                    deliveryId.contains(searchQuery.lowercase()) ||
                    pickup.contains(searchQuery.lowercase()) ||
                    dest.contains(searchQuery.lowercase()) ||
                    receiver.contains(searchQuery.lowercase()) ||
                    status.contains(searchQuery.lowercase())

            matchesFilter && matchesSearch
        }

        displayedDeliveries.clear()
        displayedDeliveries.addAll(filtered)
        deliveriesAdapter.notifyDataSetChanged()

        if (displayedDeliveries.isEmpty()) {
            tvEmptyDeliveries.visibility = View.VISIBLE
            recyclerViewDeliveries.visibility = View.GONE
        } else {
            tvEmptyDeliveries.visibility = View.GONE
            recyclerViewDeliveries.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadDeliveries()
    }

    // RecyclerView Adapter for Deliveries
    class DeliveriesAdapter(
        private val deliveries: List<Map<String, Any>>,
        private val isAdmin: Boolean,
        private val isRider: Boolean,
        private val onItemClick: (Map<String, Any>) -> Unit,
        private val onStatusUpdate: (String, String) -> Unit
    ) : RecyclerView.Adapter<DeliveriesAdapter.DeliveryViewHolder>() {

        class DeliveryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDeliveryId: TextView = view.findViewById(R.id.tvDeliveryId)
            val tvDeliveryStatus: TextView = view.findViewById(R.id.tvDeliveryStatus)
            val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
            val tvReceiverName: TextView = view.findViewById(R.id.tvReceiverName)
            val tvPickupLocation: TextView = view.findViewById(R.id.tvPickupLocation)
            val tvDeliveryDestination: TextView = view.findViewById(R.id.tvDeliveryDestination)
            val tvPackageSummary: TextView = view.findViewById(R.id.tvPackageSummary)
            val tvDeliveryDate: TextView = view.findViewById(R.id.tvDeliveryDate)

            // Admin Action Bar
            val adminActionsLayout: LinearLayout = view.findViewById(R.id.adminActionsLayout)
            val btnAdminConfirm: Button = view.findViewById(R.id.btnAdminConfirm)
            val btnAdminAssign: Button = view.findViewById(R.id.btnAdminAssign)
            val btnAdminPrepare: Button = view.findViewById(R.id.btnAdminPrepare)
            val btnAdminReject: Button = view.findViewById(R.id.btnAdminReject)
            val btnAdminReceipt: Button = view.findViewById(R.id.btnAdminReceipt)

            // Rider Action Bar
            val riderActionsLayout: LinearLayout = view.findViewById(R.id.riderActionsLayout)
            val btnAcceptJob: Button = view.findViewById(R.id.btnAcceptJob)
            val btnStartTransit: Button = view.findViewById(R.id.btnStartTransit)
            val btnMarkDelivered: Button = view.findViewById(R.id.btnMarkDelivered)
            val btnMarkFailed: Button = view.findViewById(R.id.btnMarkFailed)
            val btnReportIssue: Button = view.findViewById(R.id.btnReportIssue)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_delivery_card_snippet, parent, false)
            return DeliveryViewHolder(view)
        }

        override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
            val data = deliveries[position]
            val deliveryId = data["deliveryId"] as? String ?: "DLV-XXXX"
            val status = data["status"] as? String ?: "Pending"

            val senderName = data["senderName"] as? String ?: data["customerName"] as? String ?: "N/A"
            val senderPhone = data["senderContact"] as? String ?: data["contactNumber"] as? String ?: ""
            val receiver = data["receiverName"] as? String ?: "N/A"
            val receiverPhone = data["receiverContact"] as? String ?: ""

            val pickup = data["pickupLocation"] as? String ?: data["pickupAddress"] as? String ?: "Nasugbu, Batangas"
            val dest = data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "Lian, Batangas"
            val date = data["dateCreated"] as? String ?: "Today"

            val packageType = data["packageType"] as? String ?: "Lightweight"
            val packageDesc = data["packageDescription"] as? String ?: "Parcel"
            val weightKg = (data["packageWeightKg"] as? Number)?.toDouble() ?: (data["weightKg"] as? Number)?.toDouble() ?: 1.0

            holder.tvDeliveryId.text = deliveryId
            holder.tvDeliveryStatus.text = status
            holder.tvCustomerName.text = "Customer: $senderName ${if (senderPhone.isNotEmpty()) "($senderPhone)" else ""}"
            holder.tvReceiverName.text = "Receiver: $receiver ${if (receiverPhone.isNotEmpty()) "($receiverPhone)" else ""}"
            holder.tvPickupLocation.text = "Pickup: $pickup"
            holder.tvDeliveryDestination.text = "Destination: $dest"
            holder.tvPackageSummary.text = "Package: ${weightKg}kg $packageType • $packageDesc"
            holder.tvDeliveryDate.text = "Date Created: $date"

            // Admin Action Controls
            if (isAdmin) {
                holder.adminActionsLayout.visibility = View.VISIBLE
                holder.riderActionsLayout.visibility = View.GONE

                holder.btnAdminConfirm.setOnClickListener {
                    onStatusUpdate(deliveryId, "Confirmed")
                }

                holder.btnAdminAssign.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, AssignRiderVehicleActivity::class.java)
                    intent.putExtra("deliveryId", deliveryId)
                    intent.putExtra("packageWeightKg", weightKg)
                    context.startActivity(intent)
                }

                holder.btnAdminPrepare.setOnClickListener {
                    onStatusUpdate(deliveryId, "Ready for Pickup")
                }

                holder.btnAdminReject.setOnClickListener {
                    val context = holder.itemView.context
                    val rejectInput = EditText(context).apply { hint = "Reason for rejection" }

                    AlertDialog.Builder(context)
                        .setTitle("Reject Request ($deliveryId)")
                        .setView(rejectInput)
                        .setPositiveButton("Reject Request") { _, _ ->
                            val reason = rejectInput.text.toString().trim()
                            val finalStatus = if (reason.isNotEmpty()) "Cancelled: $reason" else "Cancelled"
                            onStatusUpdate(deliveryId, finalStatus)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                holder.btnAdminReceipt.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, DeliveryReceiptActivity::class.java)
                    intent.putExtra("deliveryId", deliveryId)
                    context.startActivity(intent)
                }
            } else if (isRider) {
                holder.adminActionsLayout.visibility = View.GONE
                holder.riderActionsLayout.visibility = View.VISIBLE

                holder.btnAcceptJob.setOnClickListener {
                    onStatusUpdate(deliveryId, "Acknowledged")
                }

                holder.btnStartTransit.setOnClickListener {
                    onStatusUpdate(deliveryId, "In Transit")
                }

                holder.btnMarkDelivered.setOnClickListener {
                    onStatusUpdate(deliveryId, "Delivered")
                }

                holder.btnMarkFailed.setOnClickListener {
                    val context = holder.itemView.context
                    val reasonInput = EditText(context).apply { hint = "Reason for delivery failure" }

                    AlertDialog.Builder(context)
                        .setTitle("Mark Delivery Failed ($deliveryId)")
                        .setView(reasonInput)
                        .setPositiveButton("Submit Failure") { _, _ ->
                            val reason = reasonInput.text.toString().trim()
                            val finalStatus = if (reason.isNotEmpty()) "Failed: $reason" else "Delivery Failed"
                            onStatusUpdate(deliveryId, finalStatus)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                holder.btnReportIssue.setOnClickListener {
                    val context = holder.itemView.context
                    val issueInput = EditText(context).apply { hint = "Reason / Problem details" }

                    AlertDialog.Builder(context)
                        .setTitle("Report Delivery Issue ($deliveryId)")
                        .setView(issueInput)
                        .setPositiveButton("Submit Issue") { _, _ ->
                            val reason = issueInput.text.toString().trim()
                            val finalStatus = if (reason.isNotEmpty()) "Issue Reported: $reason" else "Issue Reported"
                            onStatusUpdate(deliveryId, finalStatus)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } else {
                holder.adminActionsLayout.visibility = View.GONE
                holder.riderActionsLayout.visibility = View.GONE
            }

            // Status badge color coding for all 9 statuses
            when (status.lowercase()) {
                "in transit", "dispatched", "active" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFE0F2FE.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF0284C7.toInt())
                }
                "pending" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFFEF3C7.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFFD97706.toInt())
                }
                "confirmed", "acknowledged" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFE8F5E9.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF2E7D32.toInt())
                }
                "preparing", "ready for pickup" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFFFF7ED.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFFEA580C.toInt())
                }
                "assigned to rider", "assigned" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFF3E5F5.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF7B1FA2.toInt())
                }
                "delivered", "completed", "delivered & confirmed" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFDCFCE7.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF16A34A.toInt())
                }
                else -> { // Failed or Cancelled
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFFEE2E2.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFFDC2626.toInt())
                }
            }

            holder.itemView.setOnClickListener {
                onItemClick(data)
            }
        }

        override fun getItemCount(): Int = deliveries.size
    }
}
