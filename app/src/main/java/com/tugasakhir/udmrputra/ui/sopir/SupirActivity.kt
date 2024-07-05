package com.tugasakhir.udmrputra.ui.sopir

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codebyashish.googledirectionapi.AbstractRouting
import com.codebyashish.googledirectionapi.ErrorHandling
import com.codebyashish.googledirectionapi.RouteDrawing
import com.codebyashish.googledirectionapi.RouteInfoModel
import com.codebyashish.googledirectionapi.RouteListener
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.databinding.ActivitySupirBinding
import com.tugasakhir.udmrputra.ui.service.LocationService
import java.util.concurrent.TimeUnit

class SupirActivity : AppCompatActivity(), OnMapReadyCallback, RouteListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivitySupirBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isTracking = false
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private val auth = FirebaseAuth.getInstance()
    private lateinit var pengirimanId: String
    private var endLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupirBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        pengirimanId = intent.getStringExtra("PENGIRIMAN_ID").toString()

        firestore = FirebaseFirestore.getInstance()

        // Add Firestore Listener
        firestore.collection("pengiriman").document(pengirimanId)
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val latitude = documentSnapshot.getDouble("latitudeTujuan")
                    val longitude = documentSnapshot.getDouble("longitudeTujuan")
                    val status = documentSnapshot.getString("status")
                    binding.alamatPengiriman.text = documentSnapshot.getString("address")
                    binding.namaSopir.text = documentSnapshot.getString("supir")
                    binding.noHpSopir.text = documentSnapshot.getString("status")

                    if (latitude != null && longitude != null) {
                        endLatLng = LatLng(latitude, longitude)
                    }

                    if (status == "pengiriman") {
                        binding.btnKirim.text = "Perjalanan"
                        binding.btnKirim.isEnabled = false
                    } else {
                        binding.btnKirim.text = "Kirim"
                        binding.btnKirim.isEnabled = true
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }

        // Set up click listener for btnKirim
        binding.btnKirim.setOnClickListener {
            updatePengirimanStatus(pengirimanId, "pengiriman")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        getMyLocation()
        createLocationRequest()
        createLocationCallback()
        binding.btnRouteMaps.setOnClickListener {
            if (!isTracking) {
                updateTrackingStatus(true)
                startLocationUpdates()
                startService(Intent(this, LocationService::class.java))
            } else {
                updateTrackingStatus(false)
                stopLocationUpdates()
                stopService(Intent(this, LocationService::class.java))
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    if (endLatLng != null) {
                        findRoute(latLng, endLatLng!!)
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Endpoint location not available",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    Log.i(TAG, "onActivityResult: All location settings are satisfied.")
                RESULT_CANCELED ->
                    Toast.makeText(
                        this,
                        "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.MINUTES.toMillis(2) // Set interval to 2 minutes
            maxWaitTime = TimeUnit.MINUTES.toMillis(2) // Set max wait time to 2 minutes
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getMyLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun findRoute(start: LatLng?, end: LatLng?) {
        if (start == null || end == null) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Unable to get location",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            val routing = RouteDrawing.Builder()
                .key(getString(R.string.api_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .build()

            routing.execute()
        }
    }

    override fun onRouteFailure(e: ErrorHandling?) {
        Log.d("Route", "onRoutingFailure: $e")
    }

    override fun onRouteStart() {
        Log.d("Route", "Started Route")
    }

    override fun onRouteSuccess(list: ArrayList<RouteInfoModel>, indexing: Int) {
        val polylineOptions = PolylineOptions()
        val polylines = ArrayList<Polyline>()
        for (i in 0 until list.size) {
            if (i == indexing) {
                Log.e("TAG", "onRoutingSuccess: routeIndexing $indexing")
                polylineOptions.color(Color.GRAY)
                polylineOptions.width(12f)
                polylineOptions.addAll(list[indexing].points)
                polylineOptions.startCap(RoundCap())
                polylineOptions.endCap(RoundCap())
                val polyline: Polyline = mMap.addPolyline(polylineOptions)
                polylines.add(polyline)
            }
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d(TAG, "onLocationResult: ${location.latitude}, ${location.longitude}")
                    val latLng = LatLng(location.latitude, location.longitude)
                    saveLocationToFirebase(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }
    }

    private fun saveLocationToFirebase(latitude: Double, longitude: Double) {
        firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val db = FirebaseFirestore.getInstance()
        val pengirimanUpdate: Map<String, Any> = hashMapOf(
            "userId" to userId,
            "latitudeSupir" to latitude,
            "longitudeSupir" to longitude,
        )

        firestore.collection("pengiriman").document(pengirimanId).update(pengirimanUpdate)
            .addOnSuccessListener {
                Toast.makeText(this, "Location updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error updating location: $exception", Toast.LENGTH_SHORT).show()
            }

        db.collection("location")
            .add(pengirimanUpdate)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Data gagal disimpan", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e(TAG, "Error : " + exception.message)
        }
    }

    private fun updateTrackingStatus(newStatus: Boolean) {
        isTracking = newStatus
        if (isTracking) {
            binding.btnRouteMaps.text = getString(R.string.stop_running)
        } else {
            binding.btnRouteMaps.text = getString(R.string.start_running)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (isTracking) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onRouteCancelled() {
        Log.d("Route", "Cancel Route")
    }

    private fun updatePengirimanStatus(pengirimanId: String, status: String) {
        firestore.collection("pengiriman").document(pengirimanId)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Status updated to $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update status: $e", Toast.LENGTH_SHORT).show()
            }
        firestore.collection("pengajuan").whereEqualTo("pengirimanId", pengirimanId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val pengajuan = document.toObject(Pengajuan::class.java)
                    firestore.collection("pengajuan").document(pengajuan.id)
                        .update("status", status)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Status updated to $status", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update status: $e", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update status: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
