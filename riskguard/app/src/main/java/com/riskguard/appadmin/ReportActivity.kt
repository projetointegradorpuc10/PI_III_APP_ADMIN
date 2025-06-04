package com.riskguard.appadmin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private lateinit var btnLogout: Button
    private lateinit var btnRelatorio: Button
    private val db = FirebaseFirestore.getInstance()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_report)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        auth = FirebaseAuth.getInstance()
        btnLogout = findViewById(R.id.btnLogout)
        btnRelatorio = findViewById(R.id.btnRelatorio)
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnRelatorio.setOnClickListener {
            val intent = Intent(this, ResumoRiscosActivity::class.java)
            startActivity(intent)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLocation = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f))
            }
        }

        carregarReportsNoMapa()

        googleMap.setOnMarkerClickListener { marker ->
            val data = marker.tag as? Map<String, String>
            if (data != null) {
                val intent = Intent(this, ReportDetailActivity::class.java).apply {
                    putExtra("titulo", marker.title)
                    putExtra("descricao", data["descricao"])
                    putExtra("tipoRisco", data["tipoRisco"])
                    putExtra("anexoUrl", data["anexoUrl"])
                }
                startActivity(intent)
                true
            } else {
                false
            }
        }
    }

    private fun carregarReportsNoMapa() {
        db.collection("reports")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val lat = document.getDouble("latitude") ?: continue
                    val lng = document.getDouble("longitude") ?: continue
                    val titulo = document.getString("titulo") ?: "Sem título"
                    val descricao = document.getString("descricao") ?: ""
                    val tipoRisco = document.getString("tipoRisco") ?: ""
                    val anexoUrl = document.getString("anexoUrl") ?: ""

                    val position = LatLng(lat, lng)
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(position).title(titulo)
                    )
                    marker?.tag = mapOf(
                        "descricao" to descricao,
                        "tipoRisco" to tipoRisco,
                        "anexoUrl" to anexoUrl
                    )
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar reports: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }
}
