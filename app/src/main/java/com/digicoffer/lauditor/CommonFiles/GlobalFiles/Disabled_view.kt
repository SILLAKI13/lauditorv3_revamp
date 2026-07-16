package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.digicoffer.lauditor.R

class Disabled_view(private val frozenText: String, var isred: Boolean) : Fragment() {
    @JvmField var frozen_PageText: TextView? = null
    private var view: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.disabled_view, container, false)
        frozen_PageText = view?.findViewById(R.id.frozen_PageText)
        frozen_PageText?.text = frozenText
        if (isred) {
            frozen_PageText?.setTextColor(resources.getColor(R.color.Red))
        }
        return view
    }
}
