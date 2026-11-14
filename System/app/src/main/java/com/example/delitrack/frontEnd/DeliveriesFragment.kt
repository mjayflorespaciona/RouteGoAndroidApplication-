package com.example.delitrack.frontEnd

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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        recyclerViewDeliveries.layoutManager = LinearLayoutManager(requireContext())
        deliveriesAdapter = DeliveriesAdapter(displayedDeliveries) { delivery ->
            val deliveryId = delivery["deliveryId"] as? String ?: ""
            val intent = Intent(requireContext(), DeliveryTrackingActivity::class.java)
            intent.putExtra("deliveryId", deliveryId)
            startActivity(intent)
        }
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

    private fun loadDeliveries() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "default_customer"
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

                    // Display only deliveries belonging to the logged-in customer (or fallback/admin if testing)
                    val isOwner = (custId == userId) || (currentUser?.email != null && custEmail.equals(currentUser.email, ignoreCase = true))
                    val isDemoOrLegacy = custId.isEmpty() && currentUser?.email == "admin@delitrack.com"

                    if (isOwner || isDemoOrLegacy) {
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
        private val onItemClick: (Map<String, Any>) -> Unit
    ) : RecyclerView.Adapter<DeliveriesAdapter.DeliveryViewHolder>() {

        class DeliveryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDeliveryId: TextView = view.findViewById(R.id.tvDeliveryId)
            val tvDeliveryStatus: TextView = view.findViewById(R.id.tvDeliveryStatus)
            val tvReceiverName: TextView = view.findViewById(R.id.tvReceiverName)
            val tvPickupLocation: TextView = view.findViewById(R.id.tvPickupLocation)
            val tvDeliveryDestination: TextView = view.findViewById(R.id.tvDeliveryDestination)
            val tvDeliveryDate: TextView = view.findViewById(R.id.tvDeliveryDate)
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
            val receiver = data["receiverName"] as? String ?: data["customerName"] as? String ?: "N/A"
            val pickup = data["pickupAddress"] as? String ?: data["pickupLocation"] as? String ?: "Nasugbu, Batangas"
            val dest = data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "Lian, Batangas"
            val date = data["dateCreated"] as? String ?: "Today"

            holder.tvDeliveryId.text = deliveryId
            holder.tvDeliveryStatus.text = status
            holder.tvReceiverName.text = "Receiver: $receiver"
            holder.tvPickupLocation.text = "Pickup: $pickup"
            holder.tvDeliveryDestination.text = "Destination: $dest"
            holder.tvDeliveryDate.text = "Date Created: $date"

            // Status badge color coding
            when (status.lowercase()) {
                "in transit", "dispatched", "active" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFE0F2FE.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF0284C7.toInt())
                }
                "pending" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFFEF3C7.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFFD97706.toInt())
                }
                "confirmed" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFE8F5E9.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF2E7D32.toInt())
                }
                "assigned to rider", "assigned" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFF3E5F5.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF7B1FA2.toInt())
                }
                "delivered", "completed" -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFDCFCE7.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF16A34A.toInt())
                }
                else -> {
                    holder.tvDeliveryStatus.setBackgroundColor(0xFFE5E7E6.toInt())
                    holder.tvDeliveryStatus.setTextColor(0xFF555555.toInt())
                }
            }

            holder.itemView.setOnClickListener {
                onItemClick(data)
            }
        }

        override fun getItemCount() = deliveries.size
    }
}
