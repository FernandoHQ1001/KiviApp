package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/*
 * Pantalla encargada de acciones críticas de la cuenta:
 * - Cerrar sesión
 * - Eliminar cuenta
 * - Reautenticación segura (Firebase)
 */

class AccountActionsActivity : BaseActivity() {

    // Flag que indica si, tras reautenticar, se debe borrar la cuenta
    private var pendingDeleteAfterReauth: Boolean = false

    /*
     * Launcher para manejar el resultado del login con Google
     * usado durante la reautenticación.
     */
    private val googleReauthLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                // Obtener cuenta de Google
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                // Validación del token
                if (idToken.isNullOrBlank()) {
                    Toast.makeText(this, getString(R.string.reauth_failed), Toast.LENGTH_LONG).show()
                    pendingDeleteAfterReauth = false
                    return@registerForActivityResult
                }

                // Crear credencial Firebase con Google
                val credential = GoogleAuthProvider.getCredential(idToken, null)

                // Reautenticación en Firebase
                Firebase.auth.currentUser?.reauthenticate(credential)
                    ?.addOnSuccessListener {
                        // Si estaba pendiente eliminar cuenta, se continúa
                        if (pendingDeleteAfterReauth) {
                            pendingDeleteAfterReauth = false
                            performDeleteAccount()
                        }
                    }
                    ?.addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            getString(R.string.reauth_failed_with_reason, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                        pendingDeleteAfterReauth = false
                    }

            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.reauth_failed), Toast.LENGTH_LONG).show()
                pendingDeleteAfterReauth = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_actions)

        // Referencias UI
        val tvClose = findViewById<TextView>(R.id.tvCloseAccountActions)
        val btnLogout = findViewById<MaterialButton>(R.id.btnDoLogout)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDoDeleteAccount)

        // Acciones
        tvClose.setOnClickListener { finish() }
        btnLogout.setOnClickListener { confirmarCerrarSesion() }
        btnDelete.setOnClickListener { confirmarEliminarCuenta() }

        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    /*
     * Diálogo de confirmación para cerrar sesión
     */
    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(getString(R.string.btn_yes)) { _, _ -> cerrarSesion() }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    /*
     * Diálogo de confirmación para eliminar la cuenta
     */
    private fun confirmarEliminarCuenta() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_account_title))
            .setMessage(getString(R.string.delete_account_message))
            .setPositiveButton(getString(R.string.delete_account_confirm)) { _, _ ->
                eliminarCuentaConReauthSiHaceFalta()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    /*
     * Cierra sesión del usuario:
     * - Google
     * - Firebase
     * - Limpia preferencias locales
     */
    private fun cerrarSesion() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso).signOut()
        Firebase.auth.signOut()

        // Limpia configuración local
        KiviSettings.resetCurrentUserSettings(this)

        // Volver a pantalla inicial
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /*
     * Intenta eliminar la cuenta directamente.
     * Si Firebase exige reautenticación, se solicita.
     */
    private fun eliminarCuentaConReauthSiHaceFalta() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            Toast.makeText(this, getString(R.string.error_no_user), Toast.LENGTH_SHORT).show()
            return
        }

        // Intento directo (rápido): si falla por "recent login", hacemos reauth.
        user.delete()
            .addOnSuccessListener {
                // Limpieza local en caso de éxito directo
                KiviSettings.resetCurrentUserSettings(this)
                Firebase.auth.signOut()

                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    pedirReautenticacionYLuegoBorrar()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.delete_account_auth_failed, e.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /*
     * Reauth según provider y luego hace el borrado completo (Firestore + Auth).
     */
    private fun pedirReautenticacionYLuegoBorrar() {
        val user = Firebase.auth.currentUser ?: return

        val providers = user.providerData.mapNotNull { it.providerId }.toSet()
        val isGoogle = providers.contains("google.com")
        val isPassword = providers.contains("password")

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reauth_required_title))
            .setMessage(getString(R.string.reauth_required_message))
            .setPositiveButton(getString(R.string.btn_continue)) { _, _ ->
                when {
                    isGoogle -> reauthWithGoogle()
                    isPassword -> reauthWithPasswordThenDelete()
                    else -> Toast.makeText(this, getString(R.string.reauth_not_supported), Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    /*
     * Reautenticación usando Google
     */
    private fun reauthWithGoogle() {
        pendingDeleteAfterReauth = true

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)

        // Mejor UX: intenta silent first; si no, abre el chooser.
        client.silentSignIn()
            .addOnSuccessListener { account ->
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    googleReauthLauncher.launch(client.signInIntent)
                    return@addOnSuccessListener
                }

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                Firebase.auth.currentUser?.reauthenticate(credential)
                    ?.addOnSuccessListener { performDeleteAccount() }
                    ?.addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            getString(R.string.reauth_failed_with_reason, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                        pendingDeleteAfterReauth = false
                    }
            }
            .addOnFailureListener {
                googleReauthLauncher.launch(client.signInIntent)
            }
    }

    /**
     * Reautenticación usando contraseña (Email/Password)
     */
    private fun reauthWithPasswordThenDelete() {
        val user = Firebase.auth.currentUser ?: return
        val email = user.email

        if (email.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.reauth_not_supported), Toast.LENGTH_LONG).show()
            return
        }

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.password_hintA)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reauth_password_title))
            .setMessage(getString(R.string.reauth_password_message, email))
            .setView(input)
            .setPositiveButton(getString(R.string.btn_continue)) { _, _ ->
                val pass = input.text.toString()
                if (pass.isBlank()) {
                    Toast.makeText(this, getString(R.string.password_empty), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(email, pass)
                user.reauthenticate(credential)
                    .addOnSuccessListener { performDeleteAccount() }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            getString(R.string.reauth_failed_with_reason, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    /*
     * Borrado completo:
     * 1) Firestore
     * 2) Firebase Auth
     * 3) Preferencias locales
     */
    private fun performDeleteAccount() {
        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid
        val db = FirebaseFirestore.getInstance()

        // UI feedback
        Toast.makeText(this, getString(R.string.deleting_account), Toast.LENGTH_SHORT).show()

        db.collection("usuarios").document(uid)
            .delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener {
                        KiviSettings.resetCurrentUserSettings(this)
                        Firebase.auth.signOut()

                        val intent = Intent(this, WelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // Si aún falla aquí, casi siempre es reauth otra vez
                        Toast.makeText(
                            this,
                            getString(R.string.delete_account_auth_failed, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    getString(R.string.delete_account_firestore_failed, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /*
     * Aplica colores y tema visual según configuración del usuario
     */
    private fun aplicarTema() {
        val root = findViewById<ConstraintLayout>(R.id.rootAccountActions)
        val card = findViewById<MaterialCardView>(R.id.cardAccountActions)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSec = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        root.setBackgroundColor(colorFondo)
        card.setCardBackgroundColor(colorCard)
        card.strokeColor = colorTema

        findViewById<TextView>(R.id.tvTitleAccountActions).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvDescAccountActions).setTextColor(colorSec)

        // ✅ Cerrar (es TextView)
        val tvClose = findViewById<TextView>(R.id.tvCloseAccountActions)
        tvClose.setTextColor(colorTexto)

        // Logout (filled)
        val btnLogout = findViewById<MaterialButton>(R.id.btnDoLogout)
        btnLogout.backgroundTintList = temaState
        btnLogout.setTextColor(0xFFFFFFFF.toInt())

        // Delete (outlined)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDoDeleteAccount)
        btnDelete.strokeColor = temaState
        btnDelete.setTextColor(colorTema)
        btnDelete.iconTint = temaState
    }

    /*
     * Aplica escalado de texto para accesibilidad
     */
    private fun aplicarTamanos() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)
        findViewById<TextView>(R.id.tvTitleAccountActions).textSize = size(22f)
        findViewById<TextView>(R.id.tvDescAccountActions).textSize = size(14f)
        findViewById<MaterialButton>(R.id.btnDoLogout).textSize = size(16f)
        findViewById<MaterialButton>(R.id.btnDoDeleteAccount).textSize = size(16f)
    }
}
