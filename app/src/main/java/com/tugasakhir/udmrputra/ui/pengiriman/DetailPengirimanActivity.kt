package com.tugasakhir.udmrputra.ui.pengiriman

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengiriman
import com.tugasakhir.udmrputra.databinding.ActivityMapsBinding
import com.codebyashish.googledirectionapi.AbstractRouting
import com.codebyashish.googledirectionapi.ErrorHandling
import com.codebyashish.googledirectionapi.RouteDrawing
import com.codebyashish.googledirectionapi.RouteInfoModel
import com.codebyashish.googledirectionapi.RouteListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class  DetailPengirimanActivity : AppCompatActivity(), OnMapReadyCallback, RouteListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel: DetailPengirimanViewModel by viewModels()
    private val polylines = mutableListOf<Polyline>()
    private var truckMarker: Marker? = null
    private var zoomedInitially = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pengirimanId: String? = intent.getStringExtra("PENGIRIMAN_ID")
        if (pengirimanId != null) {
            fetchPengirimanDataRealtime(pengirimanId)
        } else {
            Toast.makeText(this, "tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        observeViewModel()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun fetchPengirimanDataRealtime(pengirimanId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengiriman").document(pengirimanId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Toast.makeText(this, "Error saat mengambil data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val tanggalPengajuanTimestamp = document.get("tanggal") // Read without casting
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val tanggal = if (tanggalPengajuanTimestamp is com.google.firebase.Timestamp) {
                        dateFormat.format(tanggalPengajuanTimestamp.toDate())
                    } else {
                        // Handle the case where tanggalPengajuan is not a Timestamp
                        ""
                    }
                    val pengiriman = Pengiriman(
                        document.id,
                        document.getDouble("latitudeTujuan") ?: 0.0,
                        document.getDouble("longitudeTujuan") ?: 0.0,
                        document.getDouble("latitudeSupir") ?: 0.0,
                        document.getDouble("longitudeSupir") ?: 0.0,
                        document.getString("supir") ?: "",
                        document.getString("supirId") ?: "",
                        document.getString("address") ?: "",
                        document.getString("status") ?: "",
                        tanggal
                    )
                    mapsViewModel.setPengirimanData(pengiriman)
                } else {
                    Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun observeViewModel() {
        mapsViewModel.pengirimanData.observe(this, Observer { data ->
            data?.let {
                binding.namaSopir.text = it.supir
                binding.alamatPengiriman.text = it.address
                binding.statusPengiriman.text = it.status

                if (it.status == "Dikemas") {
                    binding.imageStatus.visibility = View.VISIBLE
                    binding.googleMap.visibility = View.GONE
                } else {
                    binding.imageStatus.visibility = View.GONE
                    binding.googleMap.visibility = View.VISIBLE
                }
            } ?: run {
                Toast.makeText(this, "tidak di temukan", Toast.LENGTH_SHORT).show()
            }
        })

        mapsViewModel.address.observe(this, Observer { address ->
            binding.tvLocationRightNow.text = address
        })

        mapsViewModel.distance.observe(this, Observer { distance ->
            binding.distanceMapsContent.text = distance
        })

        mapsViewModel.durationText.observe(this, Observer { durationText ->
            binding.tvArrivalTimesMaps.text = durationText
            binding.linearLayoutArrivalTime.visibility = View.VISIBLE
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isIndoorLevelPickerEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = true
        }

        // Now observe user and vendor locations
        observeMapUpdates()
    }

    private fun observeMapUpdates() {
        mapsViewModel.userLocation.observe(this, Observer { userLocation ->
            mapsViewModel.vendorLocation.observe(this, Observer { vendorLocation ->
                updateMap(userLocation, vendorLocation)
            })
        })
    }

    private fun updateMap(userLocation: LatLng, vendorLocation: LatLng) {
        moveTruckMarker(userLocation)
        if (!zoomedInitially) {
            zoomToLocations(userLocation, vendorLocation)
            zoomedInitially = true
        }
        findRoute(userLocation, vendorLocation)
    }

    private fun moveTruckMarker(location: LatLng) {
        if (truckMarker == null) {
            val truckIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_truck)
            truckMarker = mMap.addMarker(MarkerOptions().position(location).icon(truckIcon))
        } else {
            truckMarker?.position = location
        }
    }

    private fun zoomToLocations(userLocation: LatLng, vendorLocation: LatLng) {
        val bounds = LatLngBounds.builder()
            .include(userLocation)
            .include(vendorLocation)
            .build()
        val padding = 100
        val mapView = supportFragmentManager.findFragmentById(R.id.google_map)?.view
        mapView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (mapView.height > 0 && mapView.width > 0) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    private fun findRoute(start: LatLng?, end: LatLng?) {
        if (start == null || end == null) {
            Snackbar.make(findViewById(android.R.id.content), "Unable to get location", Snackbar.LENGTH_LONG).show()
        } else {
            val routing = RouteDrawing.Builder()
                .key(getString(R.string.api_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
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
        if (list.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .color(Color.GRAY)
                .width(12f)
                .addAll(list[0].points)
                .startCap(RoundCap())
                .endCap(RoundCap())

            if (polylines.isEmpty()) {
                polylines.add(mMap.addPolyline(polylineOptions))
            } else {
                polylines[0].points = list[0].points
            }

            mapsViewModel.setDurationText(list[0].durationText)
        }
    }

    override fun onRouteCancelled() {
        Log.d("Route", "Cancel Route")
    }
}
