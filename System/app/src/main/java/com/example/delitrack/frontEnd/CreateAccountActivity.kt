package com.example.delitrack.frontEnd

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
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

class CreateAccountActivity : AppCompatActivity() {

    private var selectedRole = "Admin"
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

        val roleAdminBtn = findViewById<Button>(R.id.roleAdminBtn)
        val roleDispatcherBtn = findViewById<Button>(R.id.roleDispatcherBtn)
        val roleRiderBtn = findViewById<Button>(R.id.roleRiderBtn)

        val fullNameField = findViewById<EditText>(R.id.fullNameField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val phoneField = findViewById<EditText>(R.id.phoneField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val togglePasswordBtn = findViewById<TextView>(R.id.togglePasswordBtn)
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

        fun updateRoleSelection(role: String) {
            selectedRole = role
            val activeColor = Color.parseColor("#0B4F2C")
            val activeTextColor = Color.WHITE
            val inactiveTextColor = Color.parseColor("#666666")

            roleAdminBtn.setBackgroundColor(if (role == "Admin") activeColor else Color.TRANSPARENT)
            roleAdminBtn.setTextColor(if (role == "Admin") activeTextColor else inactiveTextColor)
            roleAdminBtn.setTypeface(null, if (role == "Admin") Typeface.BOLD else Typeface.NORMAL)

            roleDispatcherBtn.setBackgroundColor(if (role == "Dispatcher") activeColor else Color.TRANSPARENT)
            roleDispatcherBtn.setTextColor(if (role == "Dispatcher") activeTextColor else inactiveTextColor)
            roleDispatcherBtn.setTypeface(null, if (role == "Dispatcher") Typeface.BOLD else Typeface.NORMAL)

            roleRiderBtn.setBackgroundColor(if (role == "Rider") activeColor else Color.TRANSPARENT)
            roleRiderBtn.setTextColor(if (role == "Rider") activeTextColor else inactiveTextColor)
            roleRiderBtn.setTypeface(null, if (role == "Rider") Typeface.BOLD else Typeface.NORMAL)
        }

        roleAdminBtn.setOnClickListener { updateRoleSelection("Admin") }
        roleDispatcherBtn.setOnClickListener { updateRoleSelection("Dispatcher") }
        roleRiderBtn.setOnClickListener { updateRoleSelection("Rider") }

        createAccountBtn.setOnClickListener {
            val name = fullNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
                                createAccountBtn.isEnabled = true
                                Toast.makeText(this, "$selectedRole Account Created & Saved to Firestore!", Toast.LENGTH_LONG).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                createAccountBtn.isEnabled = true
                                Toast.makeText(this, "Account created, but profile save failed: ${e.message}", Toast.LENGTH_LONG).show()
                                finish()
                            }
                    } else {
                        createAccountBtn.isEnabled = true
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        signInText.setOnClickListener {
            finish()
        }
    }
}
