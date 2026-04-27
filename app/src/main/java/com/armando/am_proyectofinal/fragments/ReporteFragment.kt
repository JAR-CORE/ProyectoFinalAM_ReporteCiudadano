package com.armando.am_proyectofinal.fragments

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.armando.am_proyectofinal.R
import com.armando.am_proyectofinal.api.RetrofitClient
import com.armando.am_proyectofinal.model.ReporteRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ReporteFragment : Fragment(R.layout.fragment_reporte) {

    private var imagenBase64: String? = null
    private lateinit var takePictureLauncher: androidx.activity.result.ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: androidx.activity.result.ActivityResultLauncher<String>
    private lateinit var currentPhotoUri: Uri
    private lateinit var imgView: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgView = view.findViewById(R.id.image_preview)

        // Configurar adaptadores para AutoCompleteTextView
        val acColonia = view.findViewById<AutoCompleteTextView>(R.id.ac_colonia)
        acColonia.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.colonias)))

        val acTipo = view.findViewById<AutoCompleteTextView>(R.id.ac_tipo)
        acTipo.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.tipos_reporte)))

        // Lanzadores de imagen
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imgView.setImageURI(it)
                imagenBase64 = encodeImageToBase64(it)
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imgView.setImageURI(null) // refresh
                imgView.setImageURI(currentPhotoUri)
                imagenBase64 = encodeImageToBase64(currentPhotoUri)
            }
        }

        view.findViewById<Button>(R.id.btn_foto).setOnClickListener {
            mostrarOpcionesFoto()
        }

        view.findViewById<Button>(R.id.btn_enviar).setOnClickListener {
            enviarReporte(view)
        }
    }

    private fun mostrarOpcionesFoto() {
        val opciones = arrayOf("Cámara", "Galería")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar imagen")
            .setItems(opciones) { _, which ->
                if (which == 0) tomarFoto() else elegirDeGaleria()
            }
            .show()
    }

    private fun tomarFoto() {
        val photoFile = File(requireContext().cacheDir, "reporte_${System.currentTimeMillis()}.jpg")
        currentPhotoUri = FileProvider.getUriForFile(requireContext(),
            "${requireContext().packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(currentPhotoUri)
    }

    private fun elegirDeGaleria() {
        pickImageLauncher.launch("image/*")
    }

    private fun encodeImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resized = Bitmap.createScaledBitmap(bitmap!!, 800, 800, true)
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun enviarReporte(view: View) {
        val nombre = view.findViewById<EditText>(R.id.et_nombre).text.toString().trim()
        val colonia = view.findViewById<AutoCompleteTextView>(R.id.ac_colonia).text.toString().trim()
        val direccion = view.findViewById<EditText>(R.id.et_direccion).text.toString().trim()
        val celular = view.findViewById<EditText>(R.id.et_celular).text.toString().trim()
        val correo = view.findViewById<EditText>(R.id.et_correo).text.toString().trim()
        val tipo = view.findViewById<AutoCompleteTextView>(R.id.ac_tipo).text.toString().trim()
        val descripcion = view.findViewById<EditText>(R.id.et_descripcion).text.toString().trim()

        if (nombre.isEmpty() || colonia.isEmpty() || direccion.isEmpty() || celular.isEmpty()
            || correo.isEmpty() || tipo.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val reporte = ReporteRequest(nombre, direccion, colonia, celular, correo, tipo, descripcion, imagenBase64)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Se envía el reporte usando los parámetros definidos en ApiService (incluyendo el Header por defecto)
                val response = RetrofitClient.apiService.enviarReporte(reporte = reporte)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Reporte enviado con éxito", Toast.LENGTH_SHORT).show()
                        limpiarFormulario(view)
                    } else {
                        Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun limpiarFormulario(view: View) {
        view.findViewById<EditText>(R.id.et_nombre).text.clear()
        view.findViewById<EditText>(R.id.et_direccion).text.clear()
        view.findViewById<EditText>(R.id.et_celular).text.clear()
        view.findViewById<EditText>(R.id.et_correo).text.clear()
        view.findViewById<EditText>(R.id.et_descripcion).text.clear()
        view.findViewById<AutoCompleteTextView>(R.id.ac_colonia).text.clear()
        view.findViewById<AutoCompleteTextView>(R.id.ac_tipo).text.clear()
        imgView.setImageResource(android.R.color.transparent)
        imagenBase64 = null
    }
}