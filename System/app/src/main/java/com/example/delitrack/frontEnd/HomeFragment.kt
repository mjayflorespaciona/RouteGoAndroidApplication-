package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.delitrack.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.cardDeliveries).setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
        }

        view.findViewById<LinearLayout>(R.id.cardRouteMap).setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_map)
        }

        view.findViewById<Button>(R.id.btnNewDelivery).setOnClickListener {
            startActivity(Intent(requireContext(), NewDeliveryActivity::class.java))
        }

        view.findViewById<TextView>(R.id.viewAllDeliveries).setOnClickListener {
            (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
        }

        view.findViewById<LinearLayout>(R.id.itemDlv1001).setOnClickListener {
            startActivity(Intent(requireContext(), DeliveryTrackingActivity::class.java))
        }
    }
}

