package com.example.delitrack.frontEnd

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminCustomerManagementActivity : AppCompatActivity() {

    private lateinit var searchCustomerField: EditText
    private lateinit var customerListContainer: LinearLayout

    private val customerList = mutableListOf<Map<String, Any>>()
    private var searchQuery = ""
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_customer_management)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        verifyAdminSecurity()

        findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() }

        searchCustomerField = findViewById(R.id.searchCustomerField)
        customerListContainer = findViewById(R.id.customerListContainer)

        searchCustomerField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                renderCustomerList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadRegisteredCustomers()
    }

    private fun verifyAdminSecurity() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            finish()
            return
        }

        if (currentUser.email.equals("admin@delitrack.com", ignoreCase = true)) {
            return
        }

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "Customer"
                if (!role.equals("Admin", ignoreCase = true)) {
                    Toast.makeText(this, "Access Denied: Admin privileges required", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
    }

    private fun loadRegisteredCustomers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                customerList.clear()
                for (doc in snapshot) {
                    val data = doc.data
                    val role = data["role"] as? String ?: "Customer"
                    if (role.equals("Customer", ignoreCase = true)) {
                        val mutableData = HashMap(data)
                        mutableData["uid"] = doc.id
                        customerList.add(mutableData)
                    }
                }
                renderCustomerList()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load customer accounts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun renderCustomerList() {
        customerListContainer.removeAllViews()

        val filtered = customerList.filter { data ->
            val name = (data["fullName"] as? String ?: "").lowercase()
            val email = (data["email"] as? String ?: "").lowercase()
            val phone = (data["phone"] as? String ?: data["contactNumber"] as? String ?: "").lowercase()
            val uid = (data["uid"] as? String ?: "").lowercase()

            val query = searchQuery.lowercase()
            query.isEmpty() || name.contains(query) || email.contains(query) || phone.contains(query) || uid.contains(query)
        }

        if (filtered.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No registered customer accounts found."
                setTextColor(Color.parseColor("#777777"))
                textSize = 14f
                setPadding(16, 24, 16, 24)
            }
            customerListContainer.addView(emptyTv)
            return
        }

        for (cust in filtered) {
            val uid = cust["uid"] as? String ?: ""
            val name = cust["fullName"] as? String ?: "Customer"
            val email = cust["email"] as? String ?: "No email"
            val phone = cust["phone"] as? String ?: cust["contactNumber"] as? String ?: "N/A"
            val status = cust["status"] as? String ?: "Active"
            val displayId = "CUST-${uid.takeLast(6).uppercase()}"

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

            val headerRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

            val titleView = TextView(this).apply {
                text = "$displayId • $name"
                setTextColor(Color.parseColor("#111111"))
                setTypeface(null, Typeface.BOLD)
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val statusBadge = TextView(this).apply {
                text = status
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
                setPadding(16, 4, 16, 4)
                if (status.equals("Active", ignoreCase = true)) {
                    setBackgroundColor(Color.parseColor("#DCFCE7"))
                    setTextColor(Color.parseColor("#16A34A"))
                } else {
                    setBackgroundColor(Color.parseColor("#FEE2E2"))
                    setTextColor(Color.parseColor("#DC2626"))
                }
            }

            headerRow.addView(titleView)
            headerRow.addView(statusBadge)
            card.addView(headerRow)

            val emailView = TextView(this).apply {
                text = "Email: $email"
                setTextColor(Color.parseColor("#555555"))
                textSize = 13f
                setPadding(0, 6, 0, 0)
            }
            card.addView(emailView)

            val phoneView = TextView(this).apply {
                text = "Contact: $phone"
                setTextColor(Color.parseColor("#555555"))
                textSize = 13f
                setPadding(0, 2, 0, 0)
            }
            card.addView(phoneView)

            val actionsBar = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 0)
            }

            val btnEdit = Button(this).apply {
                text = "Edit Profile"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#0B4F2C"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                lp.setMargins(0, 0, 6, 0)
                layoutParams = lp
                setOnClickListener { showEditCustomerDialog(uid, name, phone) }
            }

            val btnHistory = Button(this).apply {
                text = "History"
                textSize = 11f
                setBackgroundColor(Color.parseColor("#0284C7"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                lp.setMargins(0, 0, 6, 0)
                layoutParams = lp
                setOnClickListener { showCustomerHistoryDialog(uid, name, email) }
            }

            val btnToggleStatus = Button(this).apply {
                text = if (status.equals("Active", ignoreCase = true)) "Deactivate" else "Activate"
                textSize = 11f
                setBackgroundColor(if (status.equals("Active", ignoreCase = true)) Color.parseColor("#DC2626") else Color.parseColor("#16A34A"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(0, 90, 1f)
                layoutParams = lp
                setOnClickListener { toggleCustomerStatus(uid, status) }
            }

            actionsBar.addView(btnEdit)
            actionsBar.addView(btnHistory)
            actionsBar.addView(btnToggleStatus)
            card.addView(actionsBar)

            customerListContainer.addView(card)
        }
    }

    private fun showEditCustomerDialog(uid: String, currentName: String, currentPhone: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val nameInput = EditText(this).apply { setText(currentName) }
        val phoneInput = EditText(this).apply { setText(currentPhone) }

        layout.addView(TextView(this).apply { text = "Full Name" })
        layout.addView(nameInput)
        layout.addView(TextView(this).apply { text = "Contact Number"; setPadding(0, 10, 0, 0) })
        layout.addView(phoneInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Customer Information")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newPhone = phoneInput.text.toString().trim()

                if (newName.isNotEmpty()) {
                    db.collection("users").document(uid)
                        .update(mapOf("fullName" to newName, "phone" to newPhone))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Customer profile updated!", Toast.LENGTH_SHORT).show()
                            loadRegisteredCustomers()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleCustomerStatus(uid: String, currentStatus: String) {
        val newStatus = if (currentStatus.equals("Active", ignoreCase = true)) "Deactivated" else "Active"
        db.collection("users").document(uid)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Customer status updated to $newStatus!", Toast.LENGTH_SHORT).show()
                loadRegisteredCustomers()
            }
    }

    private fun showCustomerHistoryDialog(uid: String, name: String, email: String) {
        db.collection("deliveries")
            .get()
            .addOnSuccessListener { snapshot ->
                val userDeliveries = snapshot.documents.filter { doc ->
                    val cId = doc.getString("customerId") ?: ""
                    val cEmail = doc.getString("customerEmail") ?: ""
                    cId == uid || (cEmail.isNotEmpty() && cEmail.equals(email, ignoreCase = true))
                }

                val historyText = if (userDeliveries.isEmpty()) {
                    "No delivery requests submitted by $name yet."
                } else {
                    userDeliveries.joinToString("\n\n") { doc ->
                        "Tracking ID: ${doc.getString("deliveryId") ?: doc.id}\n" +
                        "Status: ${doc.getString("status") ?: "Pending"}\n" +
                        "Destination: ${doc.getString("destination") ?: doc.getString("deliveryAddress") ?: "Lian, Batangas"}\n" +
                        "Date: ${doc.getString("dateCreated") ?: "Today"}"
                    }
                }

                AlertDialog.Builder(this)
                    .setTitle("Delivery History: $name")
                    .setMessage(historyText)
                    .setPositiveButton("OK", null)
                    .show()
            }
    }
}
