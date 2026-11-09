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
        val customerNameField = findViewById<EditText>(R.id.customerNameField)
        val contactNumberField = findViewById<EditText>(R.id.contactNumberField)
        val pickupAddressField = findViewById<EditText>(R.id.pickupAddressField)
        val destinationField = findViewById<EditText>(R.id.destinationField)
        val packageDescField = findViewById<EditText>(R.id.packageDescField)
        val packageTypeField = findViewById<EditText>(R.id.packageTypeField)
        val deliveryNotesField = findViewById<EditText>(R.id.deliveryNotesField)
        val btnCreateDelivery = findViewById<Button>(R.id.btnCreateDelivery)

        backBtn.setOnClickListener { finish() }

        btnCreateDelivery.setOnClickListener {
            val customerName = customerNameField.text.toString().trim()
            val contact = contactNumberField.text.toString().trim()
            val pickup = pickupAddressField.text.toString().trim()
            val destination = destinationField.text.toString().trim()
            val description = packageDescField.text.toString().trim()
            val type = packageTypeField.text.toString().trim()
            val notes = deliveryNotesField.text.toString().trim()

            if (customerName.isEmpty() || contact.isEmpty() || pickup.isEmpty() || destination.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deliveryId = "DLV-${System.currentTimeMillis().toString().takeLast(4)}"
            val currentUser = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            val deliveryMap = hashMapOf(
                "deliveryId" to deliveryId,
                "customerName" to customerName,
                "contactNumber" to contact,
                "pickupAddress" to pickup,
                "destination" to destination,
                "packageDescription" to description,
                "packageType" to type,
                "deliveryNotes" to notes,
                "status" to "Pending",
                "dateCreated" to "Today",
                "customerId" to (currentUser?.uid ?: "default_customer"),
                "customerEmail" to (currentUser?.email ?: "")
            )

            btnCreateDelivery.isEnabled = false
            db.collection("deliveries").document(deliveryId)
                .set(deliveryMap)
                .addOnSuccessListener {
                    btnCreateDelivery.isEnabled = true
                    Toast.makeText(this, "Delivery $deliveryId Requested Successfully! Status: Pending", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, SelectRouteActivity::class.java)
                    intent.putExtra("deliveryId", deliveryId)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    btnCreateDelivery.isEnabled = true
                    Toast.makeText(this, "Failed to submit delivery: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
