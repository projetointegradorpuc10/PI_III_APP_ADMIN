package com.riskguard.appadmin

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRecuperarSenha: TextView

    private val allowedEmail = "projetointegradorpuc10@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        emailField = findViewById(R.id.etEmail)
        passwordField = findViewById(R.id.etSenha)
        btnLogin = findViewById(R.id.btnEntrar)
        btnRecuperarSenha = findViewById(R.id.btnRecuperarSenha)


        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.email == allowedEmail) {
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                auth.signOut()
                Toast.makeText(this, "Usuário não autorizado. Logar com conta admin", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogin.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            if (user?.email == allowedEmail) {
                                val intent = Intent(this, ReportActivity::class.java)
                                startActivity(intent)
                                finish()
                                Toast.makeText(this, "Bem-vindo: ${user.email}", Toast.LENGTH_SHORT).show()
                            } else {
                                auth.signOut()
                                Toast.makeText(this, "Acesso negado para este usuário.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Falha no login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        btnRecuperarSenha.setOnClickListener {
            val email = emailField.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu e-mail", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "E-mail de recuperação enviado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }



    }
}
