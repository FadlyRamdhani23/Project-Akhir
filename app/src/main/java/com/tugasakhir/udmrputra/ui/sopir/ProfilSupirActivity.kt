package com.tugasakhir.udmrputra.ui.sopir

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.databinding.ProfileSupirActivityBinding
import com.tugasakhir.udmrputra.ui.logreg.LoginActivity
import com.tugasakhir.udmrputra.ui.mitra.LocationAdapter
import java.util.Locale

class ProfilSupirActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ProfileSupirActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var geocoder: Geocoder
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationAdapter: LocationAdapter
    private var selectedAddress: Address? = null
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileSupirActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        geocoder = Geocoder(this, Locale.getDefault())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        storage = Firebase.storage

        // Initialize RecyclerView
        binding.rvLokasi.layoutManager = LinearLayoutManager(this)
        locationAdapter = LocationAdapter { address ->
            updateMapWithLocation(address)
        }
        binding.rvLokasi.adapter = locationAdapter

        binding.btnGantiAlamat.setOnClickListener {
            toggleVisibility(binding.materialCardView2)
        }

        // Show ProgressBar
        binding.profileProgressBar.visibility = View.VISIBLE

        // Retrieve current user ID
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            // Fetch data from Firestore
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    // Hide ProgressBar
                    binding.profileProgressBar.visibility = View.GONE

                    if (document != null) {
                        binding.edtNama.setText(document.getString("nama"))
                        binding.edtEmail.setText(document.getString("email"))
                        binding.edtNoHp.setText(document.getString("noHp"))

                        // Check if address is available and update UI accordingly
                        val address = document.getString("address")
                        if (address.isNullOrEmpty()) {
                            binding.textViewAlamat.text = "Lengkapi Alamat"
                        } else {
                            binding.textViewAlamat.text = address
                        }

                        // Load profile image using Glide
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(binding.imgProfile)
                        }
                    } else {
                        Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Hide ProgressBar
                    binding.profileProgressBar.visibility = View.GONE

                    Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            // Hide ProgressBar if no user is logged in
            binding.profileProgressBar.visibility = View.GONE
        }

        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSearch.setOnClickListener {
            val locationName = binding.editTextSearchAlamat.text.toString()
            if (locationName.isNotEmpty()) {
                searchLocation(locationName)
            } else {
                Toast.makeText(this, "Masukkan nama lokasi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editTextSearchAlamat.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val locationName = v.text.toString()
                if (locationName.isNotEmpty()) {
                    searchLocation(locationName)
                }
                true
            } else {
                false
            }
        }

        binding.btnSetLokasi.setOnClickListener {
            saveLocation()
            binding.materialCardView2.visibility = View.GONE
        }

        binding.btnEdit.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                googleMap.cameraPosition?.target?.let { target ->
                    val latitude = target.latitude
                    val longitude = target.longitude
                    val nama = binding.edtNama.text.toString()
                    val noHp = binding.edtNoHp.text.toString()

                    val userUpdates: Map<String, Any> = hashMapOf(
                        "nama" to nama,
                        "noHp" to noHp,
                        "latitude" to latitude,
                        "longitude" to longitude
                    )

                    firestore.collection("users").document(userId).update(userUpdates)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Error updating profile: $exception",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }

        binding.imgProfile.setOnClickListener {
            selectImageFromGallery()
        }
    }

    private fun toggleVisibility(view: View) {
        if (view.visibility == View.VISIBLE) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun updateMapWithLocation(address: Address) {
        val latLng = LatLng(address.latitude, address.longitude)
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng).title(address.getAddressLine(0)))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        binding.textViewAlamat.text = address.getAddressLine(0)
        binding.editTextSearchAlamat.setText(address.getAddressLine(0))
        binding.rvLokasi.visibility = View.GONE

        // Set selected address but do not save to Firestore
        selectedAddress = address
    }

    private fun searchLocation(locationName: String) {
        try {
            val addressList: List<Address> = geocoder.getFromLocationName(locationName, 5) ?: return

            if (addressList.isNotEmpty()) {
                locationAdapter.updateLocations(addressList)
                binding.rvLokasi.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLocation() {
        val currentUser = auth.currentUser
        if (currentUser != null && selectedAddress != null) {
            val userId = currentUser.uid
            val address = selectedAddress!!
            val latitude = address.latitude
            val longitude = address.longitude
            val addressLine = address.getAddressLine(0)

            val userUpdates: Map<String, Any> = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "address" to addressLine
            )

            firestore.collection("users").document(userId).update(userUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Location updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error updating location: $exception", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "No address selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Update EditTextSearchAlamat
            val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addressList!!.isNotEmpty()) {
                val address = addressList[0]
                binding.editTextSearchAlamat.setText(address.getAddressLine(0))

                // Update selected address
                selectedAddress = address
            }
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val latitude = document.getDouble("latitude") ?: 0.0
                        val longitude = document.getDouble("longitude") ?: 0.0
                        val address = document.getString("address")
                        if (latitude != 0.0 && longitude != 0.0) {
                            val location = LatLng(latitude, longitude)
                            googleMap.addMarker(
                                MarkerOptions().position(location)
                                    .title(address ?: "Lengkapi Alamat")
                            )
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            binding.textViewAlamat.text = address ?: "Lengkapi Alamat"
                            binding.editTextSearchAlamat.setText(address ?: "Lengkapi Alamat")
                        } else {
                            binding.textViewAlamat.text = "Lengkapi Alamat"
                        }
                    } else {
                        Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT)
                        .show()
                }
        }
        binding.btnLokasiSaatIni.setOnClickListener {
            getMyLocation()
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                    // Update EditTextSearchAlamat
                    val addressList = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (addressList!!.isNotEmpty()) {
                        val address = addressList[0]
                        binding.editTextSearchAlamat.setText(address.getAddressLine(0))

                        // Update selected address
                        selectedAddress = address
                    }
                }
            }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
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

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            if (selectedImageUri != null) {
                binding.imgProfile.setImageURI(selectedImageUri)
                uploadImageToFirebase()
            }
        }
    }

    private fun uploadImageToFirebase() {
        val currentUser = auth.currentUser
        if (currentUser != null && selectedImageUri != null) {
            val userId = currentUser.uid
            val storageRef = storage.reference.child("profileImages/$userId.jpg")
            val uploadTask = storageRef.putFile(selectedImageUri!!)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val profileImageUrl = uri.toString()
                    firestore.collection("users").document(userId)
                        .update("profileImageUrl", profileImageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Profile image updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Error updating profile image: $exception",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error uploading image: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 1
    }
}