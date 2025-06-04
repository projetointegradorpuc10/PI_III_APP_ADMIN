package com.riskguard.appadmin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class ReportDetailActivity : AppCompatActivity() {

    private lateinit var btnLogout: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_report_detail)

        btnLogout = findViewById(R.id.btnLogout)
        auth = FirebaseAuth.getInstance()

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val titulo = intent.getStringExtra("titulo")
        val descricao = intent.getStringExtra("descricao")
        val tipoRisco = intent.getStringExtra("tipoRisco")
        val anexoUrl = intent.getStringExtra("anexoUrl")

        val tvTitulo = findViewById<TextView>(R.id.tvTitulo)
        val tvDescricao = findViewById<TextView>(R.id.tvDescricao)
        val tvTipoRisco = findViewById<TextView>(R.id.tvTipoRisco)
        val ivAnexo = findViewById<ImageView>(R.id.ivAnexo)

        tvTitulo.text = titulo
        tvDescricao.text = descricao
        tvTipoRisco.text = tipoRisco

        if (!anexoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(anexoUrl)
                .into(ivAnexo)
        } else {
            ivAnexo.setImageResource(R.drawable.ic_no_image) // placeholder sem imagem
        }
    }
}
