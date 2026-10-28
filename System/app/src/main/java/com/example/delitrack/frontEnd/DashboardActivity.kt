package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.delitrack.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<LinearLayout>(R.id.cardDeliveries).setOnClickListener {
            startActivity(Intent(this, DeliveriesListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardRouteMap).setOnClickListener {
            startActivity(Intent(this, RouteMapActivity::class.java))
        }

        findViewById<Button>(R.id.btnNewDelivery).setOnClickListener {
            startActivity(Intent(this, NewDeliveryActivity::class.java))
        }

        findViewById<TextView>(R.id.viewAllDeliveries).setOnClickListener {
            startActivity(Intent(this, DeliveriesListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemDlv1001).setOnClickListener {
            startActivity(Intent(this, DeliveryTrackingActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navDeliveries).setOnClickListener {
            startActivity(Intent(this, DeliveriesListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMap).setOnClickListener {
            startActivity(Intent(this, RouteMapActivity::class.java))
        }
    }
}
