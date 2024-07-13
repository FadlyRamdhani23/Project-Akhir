package com.tugasakhir.udmrputra.ui.pengaturan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.tugasakhir.udmrputra.databinding.FragmentPengaturanBinding
import com.tugasakhir.udmrputra.ui.logreg.LoginActivity
import com.tugasakhir.udmrputra.ui.mitra.LocationAdapter
import java.util.Locale
import java.util.UUID

class PengaturanFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentPengaturanBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var geocoder: Geocoder
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var storage: FirebaseStorage
    private var selectedAddress: Address? = null
    private var currentImageUri: Uri? = null
    private val imageList = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        storage = Firebase.storage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPengaturanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        binding.rvLokasi.layoutManager = LinearLayoutManager(requireContext())
        locationAdapter = LocationAdapter { address ->
            updateMapWithLocation(address)
        }
        binding.rvLokasi.adapter = locationAdapter

        binding.btnGantiAlamat.setOnClickListener {
            toggleVisibility(binding.materialCardView2)
        }
        setupImagePicker()
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

                        val imageUrl = document.getString("profileImage")
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this).load(imageUrl).into(binding.imgProfile)
                        }
                    } else {
                        Toast.makeText(requireContext(), "No such document", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Hide ProgressBar
                    binding.profileProgressBar.visibility = View.GONE

                    Toast.makeText(requireContext(), "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Hide ProgressBar if no user is logged in
            binding.profileProgressBar.visibility = View.GONE
        }

        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(requireContext(), "Berhasil Keluar", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSearch.setOnClickListener {
            val locationName = binding.editTextSearchAlamat.text.toString()
            if (locationName.isNotEmpty()) {
                searchLocation(locationName)
            } else {
                Toast.makeText(requireContext(), "Masukkan nama lokasi", Toast.LENGTH_SHORT).show()
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
                val nama = binding.edtNama.text.toString()
                val noHp = binding.edtNoHp.text.toString()

                val userUpdates: MutableMap<String, Any> = hashMapOf(
                    "nama" to nama,
                    "noHp" to noHp,
                )

                googleMap.cameraPosition?.target?.let { target ->
                    val latitude = target.latitude
                    val longitude = target.longitude
                    userUpdates["latitude"] = latitude
                    userUpdates["longitude"] = longitude
                }

                if (currentImageUri != null) {
                    uploadImageToStorage(userId) { imageUrl ->
                        userUpdates["profileImage"] = imageUrl
                        updateFirestore(userId, userUpdates)
                    }
                } else {
                    updateFirestore(userId, userUpdates)
                }
            }
        }

        binding.btnLokasiSaatIni.setOnClickListener {
            getMyLocation()
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
                Toast.makeText(requireContext(), "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLocation() {
        if (selectedAddress != null) {
            val currentUser = auth.currentUser ?: return
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
                    Toast.makeText(requireContext(), "Location updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error updating location: $exception", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No address selected", Toast.LENGTH_SHORT).show()
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
                        val imageUrl = document.getString("profileImage")
                        if (latitude != 0.0 && longitude != 0.0) {
                            val location = LatLng(latitude, longitude)
                            googleMap.addMarker(MarkerOptions().position(location).title(address ?: "Lengkapi Alamat"))
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            binding.textViewAlamat.text = address ?: "Lengkapi Alamat"
                            binding.editTextSearchAlamat.setText(address ?: "Lengkapi Alamat")
                        } else {
                            binding.textViewAlamat.text = "Lengkapi Alamat"
                        }

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this).load(imageUrl).into(binding.imgProfile)
                        }
                    } else {
                        Toast.makeText(requireContext(), "No such document", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
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

    private fun setupImagePicker() {
        binding.imgProfile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    123
                )
            }
        }
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncher.launch(gallery)
    }

    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    currentImageUri = it
                    imageList.add(it)
                    binding.imgProfile.setImageURI(currentImageUri)
                }
            }
        }

    private fun uploadImageToStorage(userId: String, callback: (String) -> Unit) {
        val imageRef = storage.reference.child("profileImages/$userId/${UUID.randomUUID()}")
        currentImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        callback(downloadUri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to upload image: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateFirestore(userId: String, updates: Map<String, Any>) {
        firestore.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                updates["profileImage"]?.let { imageUrl ->
                    Glide.with(this).load(imageUrl as String).into(binding.imgProfile)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error updating profile: $exception", Toast.LENGTH_SHORT).show()
            }
    }
}
