package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NewDeliveryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_delivery)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backBtn = findViewById<TextView>(R.id.backBtn)
        val senderNameField = findViewById<EditText>(R.id.senderNameField)
        val senderContactField = findViewById<EditText>(R.id.senderContactField)
        val pickupAddressField = findViewById<EditText>(R.id.pickupAddressField)
        val receiverNameField = findViewById<EditText>(R.id.receiverNameField)
        val receiverContactField = findViewById<EditText>(R.id.receiverContactField)
        val deliveryAddressField = findViewById<EditText>(R.id.deliveryAddressField)
        val packageDescField = findViewById<EditText>(R.id.packageDescField)
        val packageTypeField = findViewById<EditText>(R.id.packageTypeField)
        val deliveryNotesField = findViewById<EditText>(R.id.deliveryNotesField)
        val pickupLocationField = findViewById<EditText>(R.id.pickupLocationField)
        val destinationField = findViewById<EditText>(R.id.destinationField)
        val deliveryDateField = findViewById<EditText>(R.id.deliveryDateField)
        val btnCreateDelivery = findViewById<Button>(R.id.btnCreateDelivery)

        backBtn.setOnClickListener { finish() }

        btnCreateDelivery.setOnClickListener {
            val senderName = senderNameField.text.toString().trim()
            val senderContact = senderContactField.text.toString().trim()
            val pickupAddress = pickupAddressField.text.toString().trim()
            val receiverName = receiverNameField.text.toString().trim()
            val receiverContact = receiverContactField.text.toString().trim()
            val deliveryAddress = deliveryAddressField.text.toString().trim()
            val packageDesc = packageDescField.text.toString().trim()
            val packageType = packageTypeField.text.toString().trim()
            val deliveryNotes = deliveryNotesField.text.toString().trim()
            val pickupLocation = pickupLocationField.text.toString().trim()
            val destination = destinationField.text.toString().trim()
            val deliveryDate = deliveryDateField.text.toString().trim()

            if (senderName.isEmpty() || senderContact.isEmpty() || pickupAddress.isEmpty() ||
                receiverName.isEmpty() || receiverContact.isEmpty() || deliveryAddress.isEmpty() ||
                packageDesc.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deliveryId = "DLV-${System.currentTimeMillis().toString().takeLast(4)}"
            val currentUser = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()

            val deliveryMap = hashMapOf(
                "deliveryId" to deliveryId,
                // Sender Information
                "senderName" to senderName,
                "senderContact" to senderContact,
                "pickupAddress" to pickupAddress,
                // Receiver Information
                "receiverName" to receiverName,
                "receiverContact" to receiverContact,
                "deliveryAddress" to deliveryAddress,
                // Backward compatibility fields for legacy views
                "customerName" to senderName,
                "contactNumber" to senderContact,
                "destination" to deliveryAddress,
                // Package Information
                "packageDescription" to packageDesc,
                "packageType" to packageType,
                "deliveryNotes" to deliveryNotes,
                // Delivery Information & Route
                "pickupLocation" to pickupLocation,
                "deliveryDate" to deliveryDate,
                "routeName" to "Nasugbu - Lian Route",
                // Functional requirements: status set to PENDING
                "status" to "Pending",
                "dateCreated" to (if (deliveryDate.isNotEmpty()) deliveryDate else "Today"),
                "customerId" to (currentUser?.uid ?: "default_customer"),
                "customerEmail" to (currentUser?.email ?: "")
            )

            btnCreateDelivery.isEnabled = false
            db.collection("deliveries").document(deliveryId)
                .set(deliveryMap)
                .addOnSuccessListener {
                    btnCreateDelivery.isEnabled = true
                    Toast.makeText(this, "Delivery Request $deliveryId Submitted Successfully! Status: PENDING", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, SelectRouteActivity::class.java)
                    intent.putExtra("deliveryId", deliveryId)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    btnCreateDelivery.isEnabled = true
                    Toast.makeText(this, "Failed to submit delivery request: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
