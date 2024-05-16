package com.tugasakhir.udmrputra.ui.pengiriman

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.tugasakhir.udmrputra.R
import com.codebyashish.googledirectionapi.AbstractRouting
import com.codebyashish.googledirectionapi.ErrorHandling
import com.codebyashish.googledirectionapi.RouteDrawing
import com.codebyashish.googledirectionapi.RouteInfoModel
import com.codebyashish.googledirectionapi.RouteListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds

import com.tugasakhir.udmrputra.databinding.ActivityMapsBinding

class MapsActivity: AppCompatActivity(), OnMapReadyCallback, RouteListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var userLocation: LatLng = LatLng(0.0, 0.0)
    private var vendorLocation: LatLng = LatLng(0.0, 0.0)
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }


override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    mMap.uiSettings.isZoomControlsEnabled = true
    mMap.uiSettings.isIndoorLevelPickerEnabled = true
    mMap.uiSettings.isCompassEnabled = true
    mMap.uiSettings.isMapToolbarEnabled = true

    getMyLocation()
    val dicodingSpace = LatLng(-6.8957643, 107.6338462)
    userLocation = LatLng(-6.971744, 107.630628)
    vendorLocation = LatLng(-6.1754,  106.8272)

    val truckIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_truck)

    mMap.addMarker(
        MarkerOptions()
            .position(dicodingSpace)
            .title("Dicoding Space")
            .snippet("Batik Kumeli No.50")
            .icon(truckIcon) // Set the custom icon here
    )

    // Create a LatLngBounds that includes the userLocation and vendorLocation
    val builder = LatLngBounds.builder()
    builder.include(userLocation)
    builder.include(vendorLocation)
    val bounds = builder.build()

    // Animate camera to show the bounds
    val padding = 100 // offset from edges of the map in pixels

    val mapView = supportFragmentManager.findFragmentById(R.id.google_map)?.view

    // Use a ViewTreeObserver to wait until the map has been laid out
    mapView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })

    findRoute(userLocation, vendorLocation)
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
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }



    @Suppress("DEPRECATION")
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
                val durationText = list[indexing].durationText
                binding.tvArrivalTimesMaps.text = durationText
                binding.linearLayoutArrivalTime.visibility = View.VISIBLE
                Log.e("DActivity", "onRoutingSuccess: routeIndexing $durationText")
            }
        }
    }

    override fun onRouteCancelled() {
        Log.d("Route", "Cancel Route")
    }


}

