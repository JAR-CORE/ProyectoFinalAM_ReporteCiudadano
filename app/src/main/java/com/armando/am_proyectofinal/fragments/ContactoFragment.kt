package com.armando.am_proyectofinal.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.armando.am_proyectofinal.R

class ContactoFragment : Fragment(R.layout.fragment_contacto) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val direccion = getString(R.string.contacto_direccion)
        val email = getString(R.string.contacto_email)
        val telefono = getString(R.string.contacto_telefono)

        view.findViewById<View>(R.id.card_direccion).setOnClickListener {
            val geoUri = Uri.parse("geo:0,0?q=$direccion")
            startActivity(Intent(Intent.ACTION_VIEW, geoUri))
        }

        view.findViewById<View>(R.id.card_email).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        }

        view.findViewById<View>(R.id.card_telefono).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$telefono")
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        }
    }
}