package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Launcher para el flujo de Google
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
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

        // Firebase
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLoginAction = findViewById<Button>(R.id.btnLoginAction)
        val btnGoogle = findViewById<Button>(R.id.btnGoogleSignIn)

        btnBack.setOnClickListener { finish() }

        // ---------- Login con Email/Contraseña ----------
        btnLoginAction.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()

                            // ⬇️ Cargar settings desde Firebase antes de ir a Main
                            KiviSettings.loadFromCloud(this) {
                                irAInicio()
                            }

                        } else {
                            Toast.makeText(
                                this,
                                "Error: Correo o contraseña incorrectos",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // ---------- Login con Google ----------
        btnGoogle.setOnClickListener {
            iniciarSesionConGoogle()
        }
    }

    // Configura y lanza el intent de Google
    private fun iniciarSesionConGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    // Intercambia el token de Google por un usuario de Firebase
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val result = task.result
                    val isNewUser = result?.additionalUserInfo?.isNewUser ?: false
                    val currentUser = auth.currentUser

                    if (isNewUser && currentUser != null) {
                        // Crear documento en Firestore igual que en registro normal
                        val uid = currentUser.uid
                        val displayName = currentUser.displayName ?: ""
                        val email = currentUser.email ?: ""
                        val photoUrl = currentUser.photoUrl?.toString()

                        // Separar nombre y apellido de forma simple
                        val partes = displayName.split(" ")
                        val nombre = if (partes.isNotEmpty()) partes.first() else ""
                        val apellido = if (partes.size > 1) partes.drop(1).joinToString(" ") else ""

                        val datosUsuario = hashMapOf(
                            "id" to uid,
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "email" to email,
                            "fechaNacimiento" to "",   // vacío por ahora
                            "telefono" to "",
                            "fotoPerfil" to photoUrl
                        )

                        db.collection("usuarios").document(uid)
                            .set(datosUsuario)
                            .addOnSuccessListener {
                                // opcional: log o mensaje
                            }
                            .addOnFailureListener {
                                // opcional: log o mensaje
                            }
                    }

                    Toast.makeText(this, "Inicio de sesión con Google correcto", Toast.LENGTH_SHORT).show()

                    // ⬇️ Igual que con email: traer settings de la nube
                    KiviSettings.loadFromCloud(this) {
                        irAInicio()
                    }

                } else {
                    Toast.makeText(this, "Error autenticando con Firebase", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun irAInicio() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
