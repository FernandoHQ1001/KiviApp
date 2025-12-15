package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


/*
 * Pantalla de inicio de sesión de Kivi.
 * Permite autenticación con:
 * - Correo y contraseña
 * - Google Sign-In

 * Tras iniciar sesión:
 * - Descarga configuraciones del usuario desde Firestore
 * - Decide si mostrar tutorial o pantalla principal
 */
class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth // Firebase Authentication
    private lateinit var db: FirebaseFirestore // Firestore (para datos del usuario)

    /*
     * Launcher para manejar el resultado del login con Google
     * usando Activity Result API (reemplaza onActivityResult)
     */
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Obtiene la cuenta de Google
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                // Si hay token válido, se autentica en Firebase
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Toast.makeText(this, "No se obtuvo el token de Google", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar con Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        // Referencias UI
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLoginAction = findViewById<Button>(R.id.btnLoginAction)
        val btnGoogle = findViewById<Button>(R.id.btnGoogleSignIn)

        // Volver atrás
        btnBack.setOnClickListener { finish() }

        /*
         * Login con email y contraseña
         */
        btnLoginAction.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()

                            // ✅ bajar settings (incluye app_language -> KiviAppPrefs)
                            KiviSettings.loadFromCloud(this) {
                                irAInicio()
                            }

                        } else {
                            Toast.makeText(this, "Error: Correo o contraseña incorrectos", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Login con Google
        btnGoogle.setOnClickListener { iniciarSesionConGoogle() }
    }

    /*
     * Inicia el flujo de autenticación con Google
     */
    private fun iniciarSesionConGoogle() {
        // Configuración de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)   // Lanza el intent de Google
    }

    /*
     * Autenticación en Firebase usando credenciales de Google
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val result = task.result
                    val isNewUser = result?.additionalUserInfo?.isNewUser ?: false
                    val currentUser = auth.currentUser

                    /*
                     * Si es un usuario nuevo:
                     * - Crear documento en Firestore
                     * - Guardar datos básicos
                     * - Inicializar settings por defecto
                     */
                    if (isNewUser && currentUser != null) {
                        val uid = currentUser.uid
                        val displayName = currentUser.displayName ?: ""
                        val email = currentUser.email ?: ""
                        val photoUrl = currentUser.photoUrl?.toString()

                        // Separar nombre y apellido
                        val partes = displayName.split(" ")
                        val nombre = if (partes.isNotEmpty()) partes.first() else ""
                        val apellido = if (partes.size > 1) partes.drop(1).joinToString(" ") else ""

                        // Datos iniciales del usuario
                        val datosUsuario = hashMapOf(
                            "id" to uid,
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "email" to email,
                            "fechaNacimiento" to "",
                            "telefono" to "",
                            "fotoPerfil" to photoUrl
                        )

                        db.collection("usuarios").document(uid)
                            .set(datosUsuario)
                            .addOnSuccessListener {
                                //  defaults primera vez (incluye app_language global)
                                KiviSettings.setTutorialVisto(this, false)
                                KiviSettings.setAppLanguage(this, "es")
                                KiviSettings.syncToCloud(this)
                            }
                            .addOnFailureListener {
                                // igual intentamos defaults
                                KiviSettings.setTutorialVisto(this, false)
                                KiviSettings.setAppLanguage(this, "es")
                                KiviSettings.syncToCloud(this)
                            }
                    }

                    Toast.makeText(this, "Inicio de sesión con Google correcto", Toast.LENGTH_SHORT).show()

                    // Descargar settings y decidir siguiente pantalla
                    KiviSettings.loadFromCloud(this) {
                        irAInicio()
                    }

                } else {
                    Toast.makeText(this, "Error autenticando con Firebase", Toast.LENGTH_LONG).show()
                }
            }
    }

    /*
     * Decide si mostrar:
     * - Tutorial (primera vez)
     * - Pantalla principal
     */
    private fun irAInicio() {
        val next = if (!KiviSettings.isTutorialVisto(this)) {
            Intent(this, TutorialActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        next.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(next)
    }
}
