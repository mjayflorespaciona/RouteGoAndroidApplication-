package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RiderDashboardActivity : AppCompatActivity() {

    private lateinit var riderBottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rider_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        verifyRiderSecurity()

        riderBottomNavigation = findViewById(R.id.riderBottomNavigation)

        riderBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_rider_home -> {
                    replaceFragment(RiderHomeFragment())
                    true
                }
                R.id.nav_rider_assigned -> {
                    replaceFragment(DeliveriesFragment())
                    true
                }
                R.id.nav_rider_route -> {
                    replaceFragment(MapFragment())
                    true
                }
                R.id.nav_rider_history -> {
                    replaceFragment(DeliveriesFragment())
                    true
                }
                R.id.nav_rider_profile -> {
                    replaceFragment(MoreFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            replaceFragment(RiderHomeFragment())
            riderBottomNavigation.selectedItemId = R.id.nav_rider_home
        }
    }

    private fun verifyRiderSecurity() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            finish()
            return
        }

        if (currentUser.email.equals("admin@delitrack.com", ignoreCase = true)) {
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "Customer"
                    if (role.equals("Customer", ignoreCase = true)) {
                        Toast.makeText(this, "Access Denied: Rider or Admin portal only", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.riderFragmentContainer, fragment)
            .commit()
    }
}
