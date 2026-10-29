package com.example.delitrack.frontEnd

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.delitrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData(view)

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), AdminLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserData(view: View) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (!isAdded) return@addOnSuccessListener
                    if (document != null && document.exists()) {
                        val name = document.getString("fullName") ?: "Admin User"
                        val email = document.getString("email") ?: (currentUser.email ?: "admin@delitrack.com")
                        val role = document.getString("role") ?: "System Administrator"

                        view.findViewById<TextView>(R.id.tvFullName).text = name
                        view.findViewById<TextView>(R.id.tvEmail).text = email
                        view.findViewById<TextView>(R.id.tvAccountType).text = role
                    } else {
                        view.findViewById<TextView>(R.id.tvFullName).text = "Admin User"
                        view.findViewById<TextView>(R.id.tvEmail).text = currentUser.email ?: "admin@delitrack.com"
                        view.findViewById<TextView>(R.id.tvAccountType).text = "System Administrator"
                    }
                }
                .addOnFailureListener {
                    if (!isAdded) return@addOnFailureListener
                    view.findViewById<TextView>(R.id.tvFullName).text = "Admin User"
                    view.findViewById<TextView>(R.id.tvEmail).text = currentUser.email ?: "admin@delitrack.com"
                    view.findViewById<TextView>(R.id.tvAccountType).text = "System Administrator"
                }
        }
    }
}

