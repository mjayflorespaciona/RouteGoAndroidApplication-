package com.example.delitrack.frontEnd

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.delitrack.R

class DeliveriesFragment : Fragment() {

    private var currentFilter = "All"
    private var searchQuery = ""

    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterPending: Button
    private lateinit var btnFilterInTransit: Button
    private lateinit var btnFilterDelivered: Button
    private lateinit var searchField: EditText

    private lateinit var item1001: LinearLayout
    private lateinit var item1002: LinearLayout
    private lateinit var item1003: LinearLayout
    private lateinit var item1000: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deliveries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnFilterAll = view.findViewById(R.id.btnFilterAll)
        btnFilterPending = view.findViewById(R.id.btnFilterPending)
        btnFilterInTransit = view.findViewById(R.id.btnFilterInTransit)
        btnFilterDelivered = view.findViewById(R.id.btnFilterDelivered)
        searchField = view.findViewById(R.id.searchField)

        item1001 = view.findViewById(R.id.itemDlv1001)
        item1002 = view.findViewById(R.id.itemDlv1002)
        item1003 = view.findViewById(R.id.itemDlv1003)
        item1000 = view.findViewById(R.id.itemDlv1000)

        item1001.setOnClickListener {
            startActivity(Intent(requireContext(), AssignRiderVehicleActivity::class.java))
        }

        item1002.setOnClickListener {
            startActivity(Intent(requireContext(), AssignRiderVehicleActivity::class.java))
        }

        item1003.setOnClickListener {
            startActivity(Intent(requireContext(), AssignRiderVehicleActivity::class.java))
        }

        item1000.setOnClickListener {
            startActivity(Intent(requireContext(), DeliveryTrackingActivity::class.java))
        }

        btnFilterAll.setOnClickListener { setFilter("All") }
        btnFilterPending.setOnClickListener { setFilter("Pending") }
        btnFilterInTransit.setOnClickListener { setFilter("In Transit") }
        btnFilterDelivered.setOnClickListener { setFilter("Delivered") }

        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setFilter(filter: String) {
        currentFilter = filter
        updateFilterButtonStyles()
        applyFilterAndSearch()
    }

    private fun updateFilterButtonStyles() {
        val activeBg = ColorStateList.valueOf(Color.parseColor("#0B4F2C"))
        val inactiveBg = ColorStateList.valueOf(Color.parseColor("#E5E7E6"))
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#555555")

        btnFilterAll.backgroundTintList = if (currentFilter == "All") activeBg else inactiveBg
        btnFilterAll.setTextColor(if (currentFilter == "All") activeText else inactiveText)

        btnFilterPending.backgroundTintList = if (currentFilter == "Pending") activeBg else inactiveBg
        btnFilterPending.setTextColor(if (currentFilter == "Pending") activeText else inactiveText)

        btnFilterInTransit.backgroundTintList = if (currentFilter == "In Transit") activeBg else inactiveBg
        btnFilterInTransit.setTextColor(if (currentFilter == "In Transit") activeText else inactiveText)

        btnFilterDelivered.backgroundTintList = if (currentFilter == "Delivered") activeBg else inactiveBg
        btnFilterDelivered.setTextColor(if (currentFilter == "Delivered") activeText else inactiveText)
    }

    private fun applyFilterAndSearch() {
        val match1001 = (currentFilter == "All" || currentFilter == "In Transit") &&
                (searchQuery.isEmpty() || "dlv-1001 nasugbu lian in transit".contains(searchQuery.lowercase()))
        item1001.visibility = if (match1001) View.VISIBLE else View.GONE

        val match1002 = (currentFilter == "All" || currentFilter == "Pending") &&
                (searchQuery.isEmpty() || "dlv-1002 nasugbu lian pending".contains(searchQuery.lowercase()))
        item1002.visibility = if (match1002) View.VISIBLE else View.GONE

        val match1003 = (currentFilter == "All" || currentFilter == "Pending") &&
                (searchQuery.isEmpty() || "dlv-1003 nasugbu lian pending".contains(searchQuery.lowercase()))
        item1003.visibility = if (match1003) View.VISIBLE else View.GONE

        val match1000 = (currentFilter == "All" || currentFilter == "Delivered") &&
                (searchQuery.isEmpty() || "dlv-1000 nasugbu lian delivered".contains(searchQuery.lowercase()))
        item1000.visibility = if (match1000) View.VISIBLE else View.GONE
    }
}
