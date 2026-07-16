package com.digicoffer.lauditor.Email

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.digicoffer.lauditor.R

class ComposeFragment : Fragment() {

    @SuppressLint("MissingInflatedId", "LocalSuppress")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.compose, container, false)
        val closeDocuments = view?.findViewById<ImageView>(R.id.compose)
        closeDocuments?.setOnClickListener {
            try {
                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.beginTransaction()
                    .replace(R.id.child_container, ComposeFragment())
                    .addToBackStack(null)
                    .commit()
            } catch (e: Exception) {
                e.fillInStackTrace()
                Toast.makeText(activity, "Failed to open compose fragment", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}
