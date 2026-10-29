package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bottomNavigation = findViewById(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_deliveries -> DeliveriesFragment()
                R.id.nav_map -> MapFragment()
                R.id.nav_reports -> ReportsFragment()
                R.id.nav_more -> MoreFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }

        if (savedInstanceState == null) {
            val navigateTo = intent.getStringExtra("navigate_to")
            bottomNavigation.selectedItemId = when (navigateTo) {
                "deliveries" -> R.id.nav_deliveries
                "map" -> R.id.nav_map
                "reports" -> R.id.nav_reports
                "more" -> R.id.nav_more
                else -> R.id.nav_home
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo != null) {
            bottomNavigation.selectedItemId = when (navigateTo) {
                "deliveries" -> R.id.nav_deliveries
                "map" -> R.id.nav_map
                "reports" -> R.id.nav_reports
                "more" -> R.id.nav_more
                else -> R.id.nav_home
            }
        }
    }

    fun selectTab(itemId: Int) {
        bottomNavigation.selectedItemId = itemId
    }
}
