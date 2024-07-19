package com.tugasakhir.udmrputra.ui.logreg

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.tugasakhir.udmrputra.R

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var emailTextField: EditText
    private lateinit var submitButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailTextField = findViewById(R.id.emailTextField)
        submitButton = findViewById(R.id.submitBtn)

        auth = FirebaseAuth.getInstance()

        submitButton.setOnClickListener {
            val email = emailTextField.text.toString().trim()

            if (email.isEmpty()) {
                emailTextField.error = "Enter your email"
                return@setOnClickListener
            }

            resetPassword(email)
        }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showSuccessMessage()
            } else {
                showFailureMessage(task.exception?.message)
            }
        }
    }

    private fun showSuccessMessage() {
        Toast.makeText(this, "Password reset email sent successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showFailureMessage(errorMessage: String?) {
        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
    }
}