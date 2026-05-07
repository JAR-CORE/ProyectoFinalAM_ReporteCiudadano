package com.armando.am_proyectofinal.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.armando.am_proyectofinal.R

class ContactoFragment : Fragment(R.layout.fragment_contacto) {

    private val requestCallPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            realizarLlamada()
        } else {
            Toast.makeText(context, "Se necesita permiso de llamadas", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val direccion = getString(R.string.contacto_direccion)
        val email = getString(R.string.contacto_email)
        val telefono = getString(R.string.contacto_telefono)

        view.findViewById<View>(R.id.card_direccion).setOnClickListener {
            abrirMapa(direccion)
        }

        view.findViewById<View>(R.id.card_email).setOnClickListener {
            abrirCorreo(email)
        }

        view.findViewById<View>(R.id.card_telefono).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
                realizarLlamada()
            } else {
                requestCallPermission.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    private fun abrirMapa(direccion: String) {
        val encodedDireccion = Uri.encode(direccion)
        val context = requireContext()

        // 1. Intentar Google Maps
        val googleMapsUri = Uri.parse("geo:0,0?q=$encodedDireccion")
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, googleMapsUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (googleMapsIntent.resolveActivity(context.packageManager) != null) {
            startActivity(googleMapsIntent)
            return
        }

        // 2. Intentar Huawei Petal Maps
        val petalMapsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://petalmaps.com/search?q=$encodedDireccion")
            setPackage("com.huawei.maps.app")
        }

        if (petalMapsIntent.resolveActivity(context.packageManager) != null) {
            startActivity(petalMapsIntent)
            return
        }

        // 3. Intentar cualquier app de mapas genérica
        val genericoUri = Uri.parse("geo:0,0?q=$encodedDireccion")
        val genericoIntent = Intent(Intent.ACTION_VIEW, genericoUri)

        if (genericoIntent.resolveActivity(context.packageManager) != null) {
            startActivity(genericoIntent)
            return
        }

        // 4. Última opción: navegador web
        val webIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedDireccion")
        }
        startActivity(webIntent)
    }

    private fun abrirCorreo(email: String) {
        val context = requireContext()

        // 1. Intentar Gmail (app nativa)
        val gmailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            setPackage("com.google.android.gm")
        }

        if (gmailIntent.resolveActivity(context.packageManager) != null) {
            startActivity(gmailIntent)
            return
        }

        // 2. Intentar cualquier cliente de correo genérico
        val genericoIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }

        if (genericoIntent.resolveActivity(context.packageManager) != null) {
            startActivity(genericoIntent)
            return
        }

        // 3. Última opción: abrir Gmail en el navegador web
        val webIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://mail.google.com/mail/?view=cm&fs=1&to=$email")
        }
        startActivity(webIntent)
    }

    private fun realizarLlamada() {
        val telefono = getString(R.string.contacto_telefono)
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$telefono")
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
}