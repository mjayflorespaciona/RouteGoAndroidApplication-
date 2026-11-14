package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoreFragment : Fragment() {

    private lateinit var tvAvatarInitials: TextView
    private lateinit var tvHeaderName: TextView
    private lateinit var tvHeaderEmail: TextView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContactNumber: EditText
    private lateinit var etAddress: EditText
    private lateinit var etAccountRole: EditText
    private lateinit var etAssignedVehicle: EditText
    private lateinit var btnEditSaveProfile: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button

    private var isEditing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials)
        tvHeaderName = view.findViewById(R.id.tvHeaderName)
        tvHeaderEmail = view.findViewById(R.id.tvHeaderEmail)
        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        etContactNumber = view.findViewById(R.id.etContactNumber)
        etAddress = view.findViewById(R.id.etAddress)
        etAccountRole = view.findViewById(R.id.etAccountRole)
        etAssignedVehicle = view.findViewById(R.id.etAssignedVehicle)
        btnEditSaveProfile = view.findViewById(R.id.btnEditSaveProfile)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnLogout = view.findViewById(R.id.btnLogout)

        view.findViewById<Button>(R.id.btnViewDeliveryHistory)?.setOnClickListener {
            if (activity is RiderDashboardActivity) {
                (activity as RiderDashboardActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.riderFragmentContainer, DeliveriesFragment())
                    .commit()
            } else {
                (activity as? DashboardActivity)?.selectTab(R.id.nav_deliveries)
            }
        }

        loadProfileData()

        btnEditSaveProfile.setOnClickListener {
            if (!isEditing) {
                isEditing = true
                setFieldsEditable(true)
                btnEditSaveProfile.text = "Save Changes"
                Toast.makeText(requireContext(), "You can now edit your profile information", Toast.LENGTH_SHORT).show()
            } else {
                saveProfileChanges()
            }
        }

        btnChangePassword.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val email = currentUser?.email ?: etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (!isAdded) return@addOnCompleteListener
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "No email address found for password reset", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun loadProfileData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (!isAdded) return@addOnSuccessListener
                    if (document != null && document.exists()) {
                        val name = document.getString("fullName") ?: "User"
                        val email = document.getString("email") ?: (currentUser.email ?: "user@example.com")
                        val phone = document.getString("phone") ?: document.getString("contactNumber") ?: ""
                        val address = document.getString("address") ?: document.getString("deliveryAddress") ?: "Nasugbu / Lian"
                        val role = document.getString("role") ?: "Customer"

                        tvHeaderName.text = name
                        tvHeaderEmail.text = email
                        etFullName.setText(name)
                        etEmail.setText(email)
                        etContactNumber.setText(phone)
                        etAddress.setText(address)
                        etAccountRole.setText(role)

                        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                        tvAvatarInitials.text = if (initials.isNotEmpty()) initials else "US"

                        // If user is a Rider, fetch assigned vehicle & rider ID
                        if (role.equals("Rider", ignoreCase = true)) {
                            db.collection("riders").whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { query ->
                                    if (!isAdded) return@addOnSuccessListener
                                    if (!query.isEmpty) {
                                        val riderDoc = query.documents[0]
                                        val rId = riderDoc.getString("riderId") ?: "RDR-RIDER"
                                        val vehicleId = riderDoc.getString("assignedVehicleId") ?: "None"
                                        val riderStatus = riderDoc.getString("status") ?: "Available"

                                        etAccountRole.setText("Rider ID: $rId ($riderStatus)")
                                        etAssignedVehicle.setText("Vehicle: $vehicleId")
                                    } else {
                                        etAssignedVehicle.setText("Assigned Vehicle: None")
                                    }
                                }
                        } else {
                            etAssignedVehicle.setText("N/A (Customer)")
                        }
                    }
                }
        }
    }

    private fun setFieldsEditable(editable: Boolean) {
        etFullName.isEnabled = editable
        etContactNumber.isEnabled = editable
        etAddress.isEnabled = editable
        // Locked fields: Email, Account Role, Assigned Vehicle cannot be edited by Rider/Customer
        etEmail.isEnabled = false
        etAccountRole.isEnabled = false
        etAssignedVehicle.isEnabled = false
    }

    private fun saveProfileChanges() {
        val name = etFullName.text.toString().trim()
        val phone = etContactNumber.text.toString().trim()
        val address = etAddress.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Full name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            btnEditSaveProfile.isEnabled = false
            val db = FirebaseFirestore.getInstance()
            val updates = mapOf(
                "fullName" to name,
                "phone" to phone,
                "address" to address
            )

            db.collection("users").document(currentUser.uid)
                .update(updates)
                .addOnSuccessListener {
                    if (!isAdded) return@addOnSuccessListener
                    btnEditSaveProfile.isEnabled = true
                    isEditing = false
                    setFieldsEditable(false)
                    btnEditSaveProfile.text = "Edit Profile"
                    tvHeaderName.text = name
                    Toast.makeText(requireContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    if (!isAdded) return@addOnFailureListener
                    btnEditSaveProfile.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout Confirmation")
            .setMessage("Are you sure you want to log out of your RoutGo account?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), AdminLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
