package com.example.delitrack.frontEnd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.delitrack.R

class AdminLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        val loginUserField = findViewById<EditText>(R.id.loginUserField)
        val loginPassField = findViewById<EditText>(R.id.loginPassField)
        val signInBtn = findViewById<Button>(R.id.signInBtn)
        val errorBarLabel = findViewById<TextView>(R.id.errorBarLabel)
        val signUpPrompt = findViewById<TextView>(R.id.signUpPrompt)

        signInBtn.setOnClickListener {
            val usernameOrEmail = loginUserField.text.toString().trim()
            val password = loginPassField.text.toString().trim()

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                errorBarLabel.text = "Please enter email/username and password"
                errorBarLabel.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val savedPassword = prefs.getString("pass_$usernameOrEmail", null)

            val isDefaultAdmin = (usernameOrEmail.equals("admin@delitrack.com", ignoreCase = true) || usernameOrEmail.equals("admin", ignoreCase = true)) && password == "admin"

            if (isDefaultAdmin || (savedPassword != null && savedPassword == password)) {
                errorBarLabel.visibility = View.GONE
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                errorBarLabel.text = "Invalid email/username or password"
                errorBarLabel.visibility = View.VISIBLE
            }
        }

        signUpPrompt.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }
}
