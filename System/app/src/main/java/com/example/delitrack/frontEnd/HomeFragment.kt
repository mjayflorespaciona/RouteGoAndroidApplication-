package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var tvWelcomeName: TextView
    private lateinit var tvTotalRequests: TextView
    private lateinit var tvPendingDeliveries: TextView
    private lateinit var tvInTransitDeliveries: TextView
    private lateinit var tvCompletedDeliveries: TextView
    private lateinit var containerRecentDeliveries: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvWelcomeName = view.findViewById(R.id.tvWelcomeName)
        tvTotalRequests = view.findViewById(R.id.tvTotalRequests)
        tvPendingDeliveries = view.findViewById(R.id.tvPendingDeliveries)
        tvInTransitDeliveries = view.findViewById(R.id.tvInTransitDeliveries)
        tvCompletedDeliveries = view.findViewById(R.id.tvCompletedDeliveries)
        containerRecentDeliveries = view.findViewById(R.id.containerRecentDeliveries)

        loadCustomerProfile()
        loadCustomerDeliveries()

        // Button / Card Click Navigation
        view.findViewById<Button>(R.id.btnNewDelivery)?.setOnClickListener {
            startActivity(Intent(requireContext(), NewDeliveryActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.cardTrackPackage)?.setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_map)
        }

        view.findViewById<LinearLayout>(R.id.cardMyDeliveries)?.setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
        }

        view.findViewById<TextView>(R.id.viewAllDeliveries)?.setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
        }

        view.findViewById<LinearLayout>(R.id.cardTotalDeliveries)?.setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
        }

        view.findViewById<LinearLayout>(R.id.cardPendingDeliveries)?.setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
        }

        view.findViewById<LinearLayout>(R.id.cardInTransitDeliveries)?.setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
        }

        view.findViewById<LinearLayout>(R.id.cardCompletedDeliveries)?.setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
        }
    }

    private fun loadCustomerProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (!isAdded) return@addOnSuccessListener
                    if (document != null && document.exists()) {
                        val name = document.getString("fullName") ?: "Customer"
                        tvWelcomeName.text = "Welcome, $name!"
                    } else {
                        val email = currentUser.email ?: "Customer"
                        val displayName = email.substringBefore("@")
                        tvWelcomeName.text = "Welcome, ${displayName.replaceFirstChar { it.uppercase() }}!"
                    }
                }
                .addOnFailureListener {
                    if (!isAdded) return@addOnFailureListener
                    tvWelcomeName.text = "Welcome, Customer!"
                }
        } else {
            tvWelcomeName.text = "Welcome, Customer!"
        }
    }

    private fun loadCustomerDeliveries() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "default_customer"
        val db = FirebaseFirestore.getInstance()

        db.collection("deliveries")
            .get()
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener

                var totalCount = 0
                var pendingCount = 0
                var inTransitCount = 0
                var completedCount = 0

                val deliveryList = mutableMapOf<String, Map<String, Any>>()

                for (document in result) {
                    val data = document.data
                    val custId = data["customerId"] as? String ?: ""
                    val custEmail = data["customerEmail"] as? String ?: ""
                    val isOwner = (custId == userId) || (currentUser?.email != null && custEmail.equals(currentUser.email, ignoreCase = true))
                    val isDemoOrLegacy = custId.isEmpty() && currentUser?.email == "admin@delitrack.com"

                    if (isOwner || isDemoOrLegacy) {
                        val deliveryId = data["deliveryId"] as? String ?: document.id
                        val status = data["status"] as? String ?: "Pending"

                        totalCount++
                        when (status.lowercase()) {
                            "in transit", "dispatched", "active" -> inTransitCount++
                            "pending" -> pendingCount++
                            "delivered", "completed", "delivered & confirmed" -> completedCount++
                        }

                        deliveryList[deliveryId] = data
                    }
                }

                tvTotalRequests.text = totalCount.toString()
                tvPendingDeliveries.text = pendingCount.toString()
                tvInTransitDeliveries.text = inTransitCount.toString()
                tvCompletedDeliveries.text = completedCount.toString()

                // Populate recent delivery activities
                containerRecentDeliveries.removeAllViews()
                if (deliveryList.isEmpty()) {
                    val emptyTv = TextView(requireContext()).apply {
                        text = "No deliveries found. Tap '+ Create New Delivery' to get started."
                        setPadding(16, 16, 16, 16)
                        setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    }
                    containerRecentDeliveries.addView(emptyTv)
                } else {
                    for ((deliveryId, data) in deliveryList.entries.take(5)) {
                        val status = data["status"] as? String ?: "Pending"
                        val receiver = data["receiverName"] as? String ?: data["customerName"] as? String ?: "N/A"
                        val pickup = data["pickupAddress"] as? String ?: data["pickupLocation"] as? String ?: "Nasugbu, Batangas"
                        val dest = data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "Lian, Batangas"
                        val date = data["dateCreated"] as? String ?: "Today"

                        val cardView = layoutInflater.inflate(R.layout.item_delivery_card_snippet, containerRecentDeliveries, false) as? LinearLayout
                        if (cardView != null) {
                            cardView.findViewById<TextView>(R.id.tvDeliveryId)?.text = deliveryId
                            val tvStatus = cardView.findViewById<TextView>(R.id.tvDeliveryStatus)
                            tvStatus?.text = status

                            // Status color badge styling
                            when (status.lowercase()) {
                                "in transit", "dispatched", "active" -> {
                                    tvStatus?.setBackgroundColor(0xFFE0F2FE.toInt())
                                    tvStatus?.setTextColor(0xFF0284C7.toInt())
                                }
                                "pending" -> {
                                    tvStatus?.setBackgroundColor(0xFFFEF3C7.toInt())
                                    tvStatus?.setTextColor(0xFFD97706.toInt())
                                }
                                "delivered", "completed", "delivered & confirmed" -> {
                                    tvStatus?.setBackgroundColor(0xFFDCFCE7.toInt())
                                    tvStatus?.setTextColor(0xFF16A34A.toInt())
                                }
                            }

                            cardView.findViewById<TextView>(R.id.tvReceiverName)?.text = "Receiver: $receiver"
                            cardView.findViewById<TextView>(R.id.tvPickupLocation)?.text = "Pickup: $pickup"
                            cardView.findViewById<TextView>(R.id.tvDeliveryDestination)?.text = "Destination: $dest"
                            cardView.findViewById<TextView>(R.id.tvDeliveryDate)?.text = "Date Created: $date"

                            cardView.setOnClickListener {
                                val intent = Intent(requireContext(), DeliveryTrackingActivity::class.java)
                                intent.putExtra("deliveryId", deliveryId)
                                startActivity(intent)
                            }

                            containerRecentDeliveries.addView(cardView)
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure gracefully
            }
    }

    override fun onResume() {
        super.onResume()
        loadCustomerDeliveries()
    }
}
