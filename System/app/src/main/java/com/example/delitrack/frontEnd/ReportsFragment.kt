package com.example.delitrack.frontEnd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.delitrack.R

class ReportsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvDateFilter).setOnClickListener {
            Toast.makeText(requireContext(), "Filter: This Week selected", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<LinearLayout>(R.id.reportWeekly).setOnClickListener {
            Toast.makeText(requireContext(), "Downloading Weekly Performance Summary...", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<LinearLayout>(R.id.reportRider).setOnClickListener {
            Toast.makeText(requireContext(), "Downloading Rider Efficiency Audit...", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnExportReport).setOnClickListener {
            Toast.makeText(requireContext(), "Complete Analytics Report Exported Successfully!", Toast.LENGTH_LONG).show()
        }
    }
}

