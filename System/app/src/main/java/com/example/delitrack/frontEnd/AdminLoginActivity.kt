package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        auth = FirebaseAuth.getInstance()

        val loginUserField = findViewById<EditText>(R.id.loginUserField)
        val loginPassField = findViewById<EditText>(R.id.loginPassField)
        val signInBtn = findViewById<Button>(R.id.signInBtn)
        val errorBarLabel = findViewById<TextView>(R.id.errorBarLabel)
        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)
        val signUpPrompt = findViewById<TextView>(R.id.signUpPrompt)
        val togglePasswordBtn = findViewById<TextView>(R.id.togglePasswordBtn)

        var isPasswordVisible = false
        togglePasswordBtn.setOnClickListener {
            if (isPasswordVisible) {
                loginPassField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordBtn.text = "Show"
            } else {
                loginPassField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordBtn.text = "Hide"
            }
            loginPassField.setSelection(loginPassField.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        signInBtn.setOnClickListener {
            val usernameOrEmail = loginUserField.text.toString().trim()
            val password = loginPassField.text.toString().trim()

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                errorBarLabel.text = "Please enter email address and password"
                errorBarLabel.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val isDefaultAdmin = (usernameOrEmail.equals("admin@delitrack.com", ignoreCase = true) || usernameOrEmail.equals("admin", ignoreCase = true)) && password == "admin"

            if (isDefaultAdmin) {
                errorBarLabel.visibility = View.GONE
                Toast.makeText(this, "Login Successful (Admin Portal)!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return@setOnClickListener
            }

            signInBtn.isEnabled = false
            auth.signInWithEmailAndPassword(usernameOrEmail, password)
                .addOnCompleteListener(this) { task ->
                    signInBtn.isEnabled = true
                    if (task.isSuccessful) {
                        errorBarLabel.visibility = View.GONE
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener { document ->
                                    val role = document.getString("role") ?: "Customer"
                                    Toast.makeText(this, "Login Successful ($role Portal)!", Toast.LENGTH_SHORT).show()

                                    val targetActivity = when (role.lowercase()) {
                                        "admin" -> AdminDashboardActivity::class.java
                                        "rider" -> RiderDashboardActivity::class.java
                                        else -> DashboardActivity::class.java
                                    }

                                    val intent = Intent(this, targetActivity)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, DashboardActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                        } else {
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        errorBarLabel.text = "Login failed: ${task.exception?.message ?: "Invalid credentials"}"
                        errorBarLabel.visibility = View.VISIBLE
                    }
                }
        }

        forgotPasswordText.setOnClickListener {
            val email = loginUserField.text.toString().trim()
            if (email.isEmpty() || !email.contains("@")) {
                errorBarLabel.text = "Please enter your registered email address above first"
                errorBarLabel.visibility = View.VISIBLE
                return@setOnClickListener
            }
            errorBarLabel.visibility = View.GONE
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                    } else {
                        errorBarLabel.text = "Failed to send reset email: ${task.exception?.message}"
                        errorBarLabel.visibility = View.VISIBLE
                    }
                }
        }

        signUpPrompt.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
    }
}
