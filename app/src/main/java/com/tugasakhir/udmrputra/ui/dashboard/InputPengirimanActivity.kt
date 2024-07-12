package com.tugasakhir.udmrputra.ui.dashboard

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.databinding.ActivityInputPengirimanBinding
import com.tugasakhir.udmrputra.databinding.BottomSheetSelectItemBinding
import com.tugasakhir.udmrputra.ui.mitra.LocationAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class InputPengirimanActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityInputPengirimanBinding
    private lateinit var auth: FirebaseAuth
    private val barangList = mutableListOf<Pengajuan>()
    private lateinit var adapter: InpuPengajuanAdapter
    private var isBarangDataLoaded = false
    private var selectedSupirId: String? = null  // Variable to store the selected driver ID
    private var selectedSupirName: String? = null  // Variable to store the selected driver name
    private lateinit var geocoder: Geocoder
    private lateinit var googleMap: GoogleMap
    private lateinit var locationAdapter: LocationAdapter
    private var selectedAddress: Address? = null
    private val alamatOptions = mutableListOf<String>() // List to store address options
    private lateinit var alamatAdapter: ArrayAdapter<String> // Adapter for the address spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputPengirimanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupToolbar()
        initializeTextView()
        setupSpinner()
        geocoder = Geocoder(this, Locale.getDefault())
        setupAlamatSpinner() // Initialize the alamat spinner here

        binding.btnCheckout.setOnClickListener {
            submitForm()
        }

        binding.rvLokasi.layoutManager = LinearLayoutManager(this)
        locationAdapter = LocationAdapter { address ->
            updateMapWithLocation(address)
        }
        binding.rvLokasi.adapter = locationAdapter
        binding.btnSearch.setOnClickListener {
            val locationName = binding.editTextSearchAlamat.text.toString()
            if (locationName.isNotEmpty()) {
                searchLocation(locationName)
            } else {
                Toast.makeText(this, "Masukkan nama lokasi", Toast.LENGTH_SHORT).show()
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
    }
    // Example: Adding log in setupAlamatSpinner method
    private fun setupAlamatSpinner(selectedItems: List<Pengajuan>? = null) {
        Log.d("SetupAlamatSpinner", "Selected items: $selectedItems")

        val defaultOptions = mutableListOf("Pilih Lokasi Pengiriman", "Custom")
        alamatOptions.clear()
        alamatOptions.addAll(defaultOptions)
        selectedItems?.forEach { item ->
            alamatOptions.add(item.address)
        }
        alamatAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            alamatOptions
        )

        binding.inpuAlamatPengiriman.adapter = alamatAdapter

        val spinner: Spinner = findViewById(R.id.inpu_alamat_pengiriman)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, alamatOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedAlamat = alamatOptions[position]
                Log.d("SpinnerItemSelected", "Selected address: $selectedAlamat")
                if (selectedAlamat == "Custom") {
                    binding.lokasiLayout.visibility = View.VISIBLE
                } else {
                    binding.lokasiLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }



    private fun updateMapWithLocation(address: Address) {
        val latLng = LatLng(address.latitude, address.longitude)
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng).title(address.getAddressLine(0)))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        binding.editTextSearchAlamat.setText(address.getAddressLine(0))
        binding.rvLokasi.visibility = View.GONE

        // Log selected address
        Log.d("UpdateMapWithLocation", "Selected address: ${address.getAddressLine(0)}")

        // Add the selected address to the spinner
        selectedAddress = address
        addAddressToSpinner(address.getAddressLine(0))
    }

    private fun addAddressToSpinner(address: String) {
        if (!alamatOptions.contains(address)) {
            alamatOptions.add(address)
            alamatAdapter.notifyDataSetChanged()
            binding.inpuAlamatPengiriman.setSelection(alamatOptions.size - 1) // Select the newly added address
        }
    }

    private fun searchLocation(locationName: String) {
        try {
            val addressList: List<Address> = geocoder.getFromLocationName(locationName, 5) ?: emptyList()

            if (addressList.isNotEmpty()) {
                Log.d("SearchLocation", "Locations found: ${addressList.size}")
                locationAdapter.updateLocations(addressList)
                binding.rvLokasi.visibility = View.VISIBLE
            } else {
                Log.d("SearchLocation", "No locations found for: $locationName")
                Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SearchLocation", "Error searching location", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun initializeTextView() {
        binding.textViewInputBarang.setOnClickListener {
            showBottomSheetDialog()
        }
    }
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetSelectItemBinding.inflate(LayoutInflater.from(this))
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        adapter = InpuPengajuanAdapter(this, barangList) { selectedPengajuan ->
            // Update daftar item yang dipilih
            val selectedItems = adapter.getSelectedItems()
            binding.textViewInputBarang.text = selectedItems.map { it.userId }.joinToString(", ")
            setupAlamatSpinner(selectedItems)
        }

        bottomSheetBinding.recyclerViewItems.layoutManager = LinearLayoutManager(this)
        bottomSheetBinding.recyclerViewItems.adapter = adapter

        if (!isBarangDataLoaded) {
            loadBarangData(bottomSheetBinding)
        } else {
            bottomSheetBinding.progressBar.visibility = View.GONE
        }

        bottomSheetBinding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBarangList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        bottomSheetDialog.show()
    }

    private fun loadBarangData(bottomSheetBinding: BottomSheetSelectItemBinding) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan")
            .whereEqualTo("status", "Disetujui")
            .get()
            .addOnSuccessListener { result ->
                barangList.clear()
                for (document in result) {
                    val pengajuanId = document.id
                    val userId = document.getString("namaPetani") ?: ""
                    val tanggalPengajuanTimestamp = document.get("tanggalPengajuan") // Read without casting
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val tanggalPengajuan = if (tanggalPengajuanTimestamp is com.google.firebase.Timestamp) {
                        dateFormat.format(tanggalPengajuanTimestamp.toDate())
                    } else {
                        // Handle the case where tanggalPengajuan is not a Timestamp
                        ""
                    }
                    val barangAjuan = document.getString("barangAjuan") ?: ""
                    val jenisPembayaran = document.getString("jenisPembayaran") ?: ""
                    val statusPengajuan = document.getString("status") ?: ""
                    val address = document.getString("address") ?: ""
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0

                    db.collection("pengajuan").document(pengajuanId).collection("barang")
                        .get()
                        .addOnSuccessListener { barangResult ->
                            val listBarang = mutableListOf<String>()
                            for (barangDocument in barangResult) {
                                val namaBarang = barangDocument.getString("namaBarang") ?: ""
                                listBarang.add(namaBarang)
                            }
                            val pengajuan = Pengajuan(
                                pengajuanId,
                                userId,
                                tanggalPengajuan,
                                barangAjuan,
                                listBarang,
                                jenisPembayaran,
                                statusPengajuan,
                                address,
                                latitude,
                                longitude
                            )
                            barangList.add(pengajuan)
                            adapter.notifyDataSetChanged()
                            bottomSheetBinding.progressBar.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            Log.w("BarangActivity", "Error mendapatkan nama kategori", e)
                            bottomSheetBinding.progressBar.visibility = View.GONE
                        }
                }
                Log.d("BarangActivity", "Data: $barangList")
                isBarangDataLoaded = true
                bottomSheetBinding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w("BarangActivity", "Data gagal ditampilkan", exception)
                bottomSheetBinding.progressBar.visibility = View.GONE
            }
    }

    private fun filterBarangList(query: String) {
        val filteredList = barangList.filter { it.userId.contains(query, ignoreCase = true) }
        adapter.updateList(filteredList)
    }

    private fun submitForm() {
        val db = FirebaseFirestore.getInstance()

        // Get selected address
        val selectedAlamat = binding.inpuAlamatPengiriman.selectedItem.toString()

        // Get selected driver (supir)
        val selectedSupir = selectedSupirName
        val selectedSupirId = selectedSupirId

        if (selectedSupir == null || selectedSupirId == null) {
            Toast.makeText(this, "Silakan pilih supir terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val geocoder = Geocoder(this, Locale.getDefault())
        val address = selectedAlamat // Ambil alamat yang sudah dipilih atau dimasukkan
        val results = geocoder.getFromLocationName(address, 1)
        // Prepare data for submission
        if (results!!.isNotEmpty()) {
            val latitude = results[0].latitude
            val longitude = results[0].longitude
           val latitudeSupir = -6.904033999999999
            val longitudeSupir = 107.6207242

            // Persiapkan data untuk dikirim
            val data = hashMapOf(
                "address" to selectedAlamat,
                "status" to "Dikemas",
                "latitudeTujuan" to latitude,
                "longitudeTujuan" to longitude,
                "supir" to selectedSupir,
                "latitudeSupir" to latitudeSupir,
                "longitudeSupir" to longitudeSupir,
                "tanggal" to Timestamp(Date()),
                "supirId" to selectedSupirId  // Use selected driver ID
            )

            // Lanjutkan dengan menyimpan data ke Firestore
            db.collection("pengiriman")
                .add(data)
                .addOnSuccessListener { documentReference ->
                    Log.d("pengiriman", "DocumentSnapshot added with ID: ${documentReference.id}")

                    addSelectedItemsToPengajuanCollection(documentReference.id)
                    Toast.makeText(this, "Pengiriman berhasil diajukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w("pengiriman", "Error adding document", e)
                    Toast.makeText(this, "Gagal mengajukan pengiriman", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Alamat tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSelectedItemsToPengajuanCollection(pengirimanId: String) {
        val db = FirebaseFirestore.getInstance()

        adapter.getSelectedItems().forEach { pengajuan ->
            val pengajuanData: HashMap<String, Any> = hashMapOf(
                "idPengiriman" to pengirimanId, // Tambahkan idPengiriman sebagai referensi
                "userId" to pengajuan.userId,
                "tanggalPengajuan" to pengajuan.tanggalPengajuan,
                "listBarang" to pengajuan.listBarang,
                "jenisPembayaran" to pengajuan.jenisPembayaran,
                "statusPengajuan" to "Dikemas",
                "address" to pengajuan.address,
                "latitude" to pengajuan.latitude,
                "longitude" to pengajuan.longitude
            )

            // Update status dan idPengiriman di koleksi "pengajuan"
            val pengajuanRef = db.collection("pengajuan").document(pengajuan.id)
            val pengajuanUpdateData: HashMap<String, Any> = hashMapOf(
                "status" to "Dikemas",
                "idPengiriman" to pengirimanId
            )

            pengajuanRef.update(pengajuanUpdateData)
                .addOnSuccessListener {
                    Log.d("pengiriman", "Pengajuan updated with idPengiriman: $pengirimanId")
                }
                .addOnFailureListener { e ->
                    Log.w("pengiriman", "Error updating pengajuan", e)
                }

            // Tambahkan pengajuan ke koleksi nested "pengajuan" di bawah "pengirimanId"
            db.collection("pengiriman").document(pengirimanId)
                .collection("pengajuan")
                .add(pengajuanData)
                .addOnSuccessListener { documentReference ->
                    Log.d("pengiriman", "Pengajuan added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("pengiriman", "Error adding pengajuan", e)
                }
        }
    }

    private fun setupSpinner() {
        val spinner: Spinner = findViewById(R.id.inputNamaSupir)
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("status", "supir")
            .get()
            .addOnSuccessListener { documents ->
                val usersList = mutableListOf<String>()
                val usersMap = mutableMapOf<String, String>()  // Map to store uid with corresponding name

                for (document in documents) {
                    val namaSupir = document.getString("nama")
                    val uidSupir = document.id
                    if (namaSupir != null && uidSupir != null) {
                        usersList.add(namaSupir)
                        usersMap[namaSupir] = uidSupir
                    }
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, usersList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        val selectedName = parent.getItemAtPosition(position) as String
                        selectedSupirId = usersMap[selectedName]
                        selectedSupirName = selectedName
                        // Now you can use the selectedSupirId and selectedSupirName for your logic
                        Log.d("Selected Supir", "Name: $selectedSupirName, UID: $selectedSupirId")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Handle the case where no item is selected if needed
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("InputPengirimanActivity", "Error getting documents: ", exception)
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
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}





