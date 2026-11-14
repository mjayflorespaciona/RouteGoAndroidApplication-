package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_account)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val roleRadioGroup = findViewById<RadioGroup>(R.id.roleRadioGroup)
        val radioCustomer = findViewById<RadioButton>(R.id.radioCustomer)
        val radioRider = findViewById<RadioButton>(R.id.radioRider)

        val fullNameField = findViewById<EditText>(R.id.fullNameField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val phoneField = findViewById<EditText>(R.id.phoneField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val confirmPasswordField = findViewById<EditText>(R.id.confirmPasswordField)
        val togglePasswordBtn = findViewById<TextView>(R.id.togglePasswordBtn)
        val toggleConfirmPasswordBtn = findViewById<TextView>(R.id.toggleConfirmPasswordBtn)
        val createAccountBtn = findViewById<Button>(R.id.createAccountBtn)
        val signInText = findViewById<TextView>(R.id.signInText)

        var isPasswordVisible = false
        togglePasswordBtn.setOnClickListener {
            if (isPasswordVisible) {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordBtn.text = "Show"
            } else {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordBtn.text = "Hide"
            }
            passwordField.setSelection(passwordField.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        var isConfirmPasswordVisible = false
        toggleConfirmPasswordBtn.setOnClickListener {
            if (isConfirmPasswordVisible) {
                confirmPasswordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleConfirmPasswordBtn.text = "Show"
            } else {
                confirmPasswordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleConfirmPasswordBtn.text = "Hide"
            }
            confirmPasswordField.setSelection(confirmPasswordField.text.length)
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }

        createAccountBtn.setOnClickListener {
            val selectedRole = if (radioRider.isChecked) "Rider" else "Customer"
            val name = fullNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createAccountBtn.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: email
                        val db = FirebaseFirestore.getInstance()

                        val userMap = hashMapOf(
                            "fullName" to name,
                            "email" to email,
                            "phone" to phone,
                            "role" to selectedRole
                        )

                        db.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                if (selectedRole == "Rider") {
                                    // Also seed to riders fleet collection
                                    val riderId = "RDR-${System.currentTimeMillis().toString().takeLast(4)}"
                                    val riderMap = hashMapOf(
                                        "riderId" to riderId,
                                        "fullName" to name,
                                        "phone" to phone,
                                        "assignedVehicleId" to null,
                                        "status" to "Available",
                                        "email" to email,
                                        "userId" to userId
                                    )
                                    db.collection("riders").document(riderId).set(riderMap)
                                }

                                createAccountBtn.isEnabled = true
                                Toast.makeText(this, "$selectedRole Account Registered Successfully!", Toast.LENGTH_LONG).show()

                                val intent = Intent(this, DashboardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                createAccountBtn.isEnabled = true
                                Toast.makeText(this, "Account created, but profile save failed: ${e.message}", Toast.LENGTH_LONG).show()
                                val intent = Intent(this, DashboardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                    } else {
                        createAccountBtn.isEnabled = true
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        signInText.setOnClickListener {
            finish()
        }
    }
}
