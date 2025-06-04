package com.riskguard.appadmin

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ResumoRiscosActivity : AppCompatActivity() {

    private lateinit var txtTotalRiscos: TextView
    private lateinit var txtDatasCriticas: TextView
    private lateinit var txtUltimoRisco: TextView
    private lateinit var txtGravidade: TextView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val TAG = "ResumoRiscosActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_resumo_riscos)

        txtTotalRiscos = findViewById(R.id.txtTotalRiscos)
        txtDatasCriticas = findViewById(R.id.txtDatasCriticas)
        txtUltimoRisco = findViewById(R.id.txtUltimoRisco)
        txtGravidade = findViewById(R.id.txtGravidade)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        carregarResumo()
    }

    private fun carregarResumo() {
        firestore.collection("reports")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val listaReports = querySnapshot.documents.mapNotNull { doc ->
                    val titulo = doc.getString("titulo") ?: return@mapNotNull null
                    val descricao = doc.getString("descricao") ?: ""
                    val tipoRisco = doc.getString("tipoRisco") ?: "Desconhecido"
                    val timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()

                    ReportData(titulo, descricao, tipoRisco, timestamp)
                }

                if (listaReports.isEmpty()) {
                    txtTotalRiscos.text = "Total de riscos: 0"
                    txtDatasCriticas.text = "Datas críticas:\n-"
                    txtUltimoRisco.text = "Último risco:\n-"
                    txtGravidade.text = "Gravidade:\nBaixo: 0 | Moderado: 0 | Alto: 0"
                    return@addOnSuccessListener
                }

                atualizarUIResumo(listaReports)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao carregar reports", e)
                Toast.makeText(this, "Falha ao carregar resumo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarUIResumo(reports: List<ReportData>) {
        txtTotalRiscos.text = "Total de riscos: ${reports.size}"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val datasCriticas = reports
            .map { it.timestamp.toDate() }
            .sortedByDescending { it.time }
            .take(3)
            .map { "- ${sdf.format(it)}" }
            .joinToString("\n")

        txtDatasCriticas.text = "Datas críticas:\n$datasCriticas"

        val ultimo = reports.maxByOrNull { it.timestamp.seconds }
        if (ultimo != null) {
            txtUltimoRisco.text = "Último risco:\n${ultimo.tipoRisco}: ${ultimo.titulo}"
        } else {
            txtUltimoRisco.text = "Último risco:\n-"
        }

        val contagem = reports.groupingBy { it.tipoRisco }.eachCount()
        val baixo = contagem["Baixo"] ?: 0
        val moderado = contagem["Moderado"] ?: 0
        val alto = contagem["Alto"] ?: 0

        txtGravidade.text = "Gravidade:\nBaixo: $baixo | Moderado: $moderado | Alto: $alto"
    }

    data class ReportData(
        val titulo: String,
        val descricao: String,
        val tipoRisco: String,
        val timestamp: Timestamp
    )
}
