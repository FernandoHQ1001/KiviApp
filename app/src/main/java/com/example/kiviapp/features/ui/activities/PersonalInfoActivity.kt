package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream

class PersonalInfoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etFecha: EditText
    private lateinit var etTelefono: EditText
    private lateinit var tvEmail: TextView
    private lateinit var imgPerfil: ImageView
    private lateinit var tvCambiarFoto: TextView

    private val PICK_IMAGE_CODE = 4000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        // Back
        findViewById<ImageButton>(R.id.btnBackPersonal).setOnClickListener { finish() }

        // Campos
        etNombre = findViewById(R.id.etNombrePersonal)
        etApellido = findViewById(R.id.etApellidoPersonal)
        etFecha = findViewById(R.id.etFechaPersonal)
        etTelefono = findViewById(R.id.etTelefonoPersonal)
        tvEmail = findViewById(R.id.tvEmailPersonal)
        imgPerfil = findViewById(R.id.imgPerfil)
        tvCambiarFoto = findViewById(R.id.tvCambiarFoto)

        findViewById<MaterialButton>(R.id.btnGuardarPersonal).setOnClickListener {
            guardarCambios()
        }

        // Cambiar foto → abrir galería
        tvCambiarFoto.setOnClickListener {
            elegirFotoGaleria()
        }

        cargarDatos()
        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    // --- Abrir galería ---
    private fun elegirFotoGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            subirFotoBase64(uri)
        }
    }

    // --- Subir foto como Base64 a Firestore ---
    private fun subirFotoBase64(uri: Uri) {
        val user = auth.currentUser ?: return
        try {
            val bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            imgPerfil.setImageBitmap(bmp)

            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bytes = baos.toByteArray()

            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            db.collection("usuarios").document(user.uid)
                .update("fotoPerfilBase64", base64)
                .addOnSuccessListener {
                    Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error guardando foto", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error convirtiendo la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Cargar datos de Firestore ---
    private fun cargarDatos() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvEmail.text = user.email ?: "Sin email"

        db.collection("usuarios").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etNombre.setText(doc.getString("nombre") ?: "")
                    etApellido.setText(doc.getString("apellido") ?: "")
                    etFecha.setText(doc.getString("fechaNacimiento") ?: "")
                    etTelefono.setText(doc.getString("telefono") ?: "")

                    // Cargar foto si hay Base64
                    val base64Foto = doc.getString("fotoPerfilBase64")
                    if (!base64Foto.isNullOrEmpty()) {
                        try {
                            val bytes = Base64.decode(base64Foto, Base64.DEFAULT)
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imgPerfil.setImageBitmap(bmp)
                        } catch (_: Exception) {
                            imgPerfil.setImageResource(android.R.drawable.ic_menu_camera)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando datos", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Guardar cambios en Firestore ---
    private fun guardarCambios() {
        val user = auth.currentUser ?: return

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val fecha = etFecha.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()

        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(this, "Nombre y apellido son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val datosActualizados = mapOf(
            "nombre" to nombre,
            "apellido" to apellido,
            "fechaNacimiento" to fecha,
            "telefono" to telefono
        )

        db.collection("usuarios").document(user.uid)
            .update(datosActualizados)
            .addOnSuccessListener {
                Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Tema / Colores ---
    private fun aplicarTema() {
        val root = findViewById<ScrollView>(R.id.rootPersonalInfo)
        val card = findViewById<MaterialCardView>(R.id.cardPersonalInfo)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        root.setBackgroundColor(colorFondo)
        card.setCardBackgroundColor(colorCard)

        // Título
        findViewById<TextView>(R.id.tvPersonalHeader).setTextColor(colorTexto)

        // Labels
        findViewById<TextView>(R.id.tvLabelNombre).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvLabelApellido).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvLabelFecha).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvLabelTelefono).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvLabelEmail).setTextColor(colorSecundario)

        // Campos
        etNombre.setTextColor(colorTexto)
        etApellido.setTextColor(colorTexto)
        etFecha.setTextColor(colorTexto)
        etTelefono.setTextColor(colorTexto)
        tvEmail.setTextColor(colorTexto)

        etNombre.setHintTextColor(colorSecundario)
        etApellido.setHintTextColor(colorSecundario)
        etFecha.setHintTextColor(colorSecundario)
        etTelefono.setHintTextColor(colorSecundario)

        // Botón guardar
        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardarPersonal)
        btnGuardar.backgroundTintList = temaState
        btnGuardar.setTextColor(0xFFFFFFFF.toInt())
    }

    // --- Tamaño de texto dinámico ---
    private fun aplicarTamanos() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)

        // Título
        findViewById<TextView>(R.id.tvPersonalHeader).textSize = size(22f)

        // Labels
        findViewById<TextView>(R.id.tvLabelNombre).textSize = size(14f)
        findViewById<TextView>(R.id.tvLabelApellido).textSize = size(14f)
        findViewById<TextView>(R.id.tvLabelFecha).textSize = size(14f)
        findViewById<TextView>(R.id.tvLabelTelefono).textSize = size(14f)
        findViewById<TextView>(R.id.tvLabelEmail).textSize = size(14f)

        // Campos
        etNombre.textSize = size(16f)
        etApellido.textSize = size(16f)
        etFecha.textSize = size(16f)
        etTelefono.textSize = size(16f)
        tvEmail.textSize = size(16f)
    }
}
