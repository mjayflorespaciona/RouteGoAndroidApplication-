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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeliveriesFragment : Fragment() {

    private var currentFilter = "All"
    private var searchQuery = ""

    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterPending: Button
    private lateinit var btnFilterInTransit: Button
    private lateinit var btnFilterDelivered: Button
    private lateinit var searchField: EditText
    private lateinit var containerDeliveriesList: LinearLayout

    private val allDeliveries = mutableListOf<Map<String, Any>>()

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
        btnFilterInTransit = view.findViewById(R.id.btnFilterInTransit)
        btnFilterDelivered = view.findViewById(R.id.btnFilterDelivered)
        searchField = view.findViewById(R.id.searchField)
        containerDeliveriesList = view.findViewById(R.id.containerDeliveriesList)

        btnFilterAll.setOnClickListener { setFilter("All") }
        btnFilterPending.setOnClickListener { setFilter("Pending") }
        btnFilterInTransit.setOnClickListener { setFilter("In Transit") }
        btnFilterDelivered.setOnClickListener { setFilter("Delivered") }

        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                renderDeliveries()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadDeliveries()
    }

    private fun setFilter(filter: String) {
        currentFilter = filter
        updateFilterButtonStyles()
        renderDeliveries()
    }

    private fun updateFilterButtonStyles() {
        val activeBg = ColorStateList.valueOf(Color.parseColor("#0B4F2C"))
        val inactiveBg = ColorStateList.valueOf(Color.parseColor("#E5E7E6"))
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#555555")

        btnFilterAll.backgroundTintList = if (currentFilter == "All") activeBg else inactiveBg
        btnFilterAll.setTextColor(if (currentFilter == "All") activeText else inactiveText)

        btnFilterPending.backgroundTintList = if (currentFilter == "Pending") activeBg else inactiveBg
        btnFilterPending.setTextColor(if (currentFilter == "Pending") activeText else inactiveText)

        btnFilterInTransit.backgroundTintList = if (currentFilter == "In Transit") activeBg else inactiveBg
        btnFilterInTransit.setTextColor(if (currentFilter == "In Transit") activeText else inactiveText)

        btnFilterDelivered.backgroundTintList = if (currentFilter == "Delivered") activeBg else inactiveBg
        btnFilterDelivered.setTextColor(if (currentFilter == "Delivered") activeText else inactiveText)
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
                    if (custId.isEmpty() || custId == userId || currentUser?.email == "admin@delitrack.com") {
                        allDeliveries.add(data)
                    }
                }
                renderDeliveries()
            }
            .addOnFailureListener {
                // handle failure
            }
    }

    private fun renderDeliveries() {
        if (!isAdded) return
        containerDeliveriesList.removeAllViews()

        val filtered = allDeliveries.filter { data ->
            val status = data["status"] as? String ?: "Pending"
            val deliveryId = (data["deliveryId"] as? String ?: "").lowercase()
            val pickup = (data["pickupAddress"] as? String ?: "").lowercase()
            val dest = (data["destination"] as? String ?: "").lowercase()
            val customer = (data["customerName"] as? String ?: "").lowercase()

            val matchesFilter = when (currentFilter) {
                "All" -> true
                "Pending" -> status.equals("Pending", ignoreCase = true)
                "In Transit" -> status.equals("In Transit", ignoreCase = true) || status.equals("Dispatched", ignoreCase = true) || status.equals("Active", ignoreCase = true)
                "Delivered" -> status.equals("Delivered", ignoreCase = true) || status.equals("Completed", ignoreCase = true)
                else -> true
            }

            val matchesSearch = searchQuery.isEmpty() ||
                    deliveryId.contains(searchQuery.lowercase()) ||
                    pickup.contains(searchQuery.lowercase()) ||
                    dest.contains(searchQuery.lowercase()) ||
                    customer.contains(searchQuery.lowercase()) ||
                    status.contains(searchQuery.lowercase())

            matchesFilter && matchesSearch
        }

        if (filtered.isEmpty()) {
            val emptyTv = TextView(requireContext()).apply {
                text = "No deliveries match your filter or search."
                setPadding(16, 32, 16, 16)
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
            }
            containerDeliveriesList.addView(emptyTv)
            return
        }

        for (data in filtered) {
            val deliveryId = data["deliveryId"] as? String ?: "DLV-XXXX"
            val status = data["status"] as? String ?: "Pending"
            val pickup = data["pickupAddress"] as? String ?: "Nasugbu"
            val dest = data["destination"] as? String ?: "Lian"
            val date = data["dateCreated"] as? String ?: "Today"

            val cardView = layoutInflater.inflate(R.layout.item_delivery_card_snippet, containerDeliveriesList, false) as? LinearLayout
            if (cardView != null) {
                cardView.findViewById<TextView>(R.id.tvDeliveryId)?.text = deliveryId
                val tvStatus = cardView.findViewById<TextView>(R.id.tvDeliveryStatus)
                tvStatus?.text = status

                when (status.lowercase()) {
                    "in transit", "dispatched", "active" -> {
                        tvStatus?.setBackgroundColor(0xFFE0F2FE.toInt())
                        tvStatus?.setTextColor(0xFF0284C7.toInt())
                    }
                    "pending" -> {
                        tvStatus?.setBackgroundColor(0xFFFEF3C7.toInt())
                        tvStatus?.setTextColor(0xFFD97706.toInt())
                    }
                    "delivered", "completed" -> {
                        tvStatus?.setBackgroundColor(0xFFDCFCE7.toInt())
                        tvStatus?.setTextColor(0xFF16A34A.toInt())
                    }
                }

                cardView.findViewById<TextView>(R.id.tvDeliveryRoute)?.text = "$pickup ➔ $dest"
                cardView.findViewById<TextView>(R.id.tvDeliveryDate)?.text = date

                cardView.setOnClickListener {
                    val intent = Intent(requireContext(), DeliveryTrackingActivity::class.java)
                    intent.putExtra("deliveryId", deliveryId)
                    startActivity(intent)
                }

                containerDeliveriesList.addView(cardView)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDeliveries()
    }
}
