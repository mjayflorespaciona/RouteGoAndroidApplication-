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

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var adminBottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Security Guard: Verify Admin Role
        verifyAdminSecurity()

        adminBottomNavigation = findViewById(R.id.adminBottomNavigation)

        adminBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> {
                    replaceFragment(AdminHomeFragment())
                    true
                }
                R.id.nav_admin_deliveries -> {
                    replaceFragment(DeliveriesFragment())
                    true
                }
                R.id.nav_admin_assignments -> {
                    val intent = Intent(this, AssignRiderVehicleActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_admin_map -> {
                    replaceFragment(MapFragment())
                    true
                }
                R.id.nav_admin_reports -> {
                    replaceFragment(ReportsFragment())
                    true
                }
                R.id.nav_admin_profile -> {
                    replaceFragment(MoreFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            replaceFragment(AdminHomeFragment())
            adminBottomNavigation.selectedItemId = R.id.nav_admin_dashboard
        }
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

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "Customer"
                    if (!role.equals("Admin", ignoreCase = true)) {
                        Toast.makeText(this, "Access Denied: Admin privileges required", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }
}
