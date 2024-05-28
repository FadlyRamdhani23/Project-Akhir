package com.tugasakhir.udmrputra.ui.pengiriman

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.Toast
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
import com.tugasakhir.udmrputra.data.Pengiriman

import com.tugasakhir.udmrputra.databinding.ActivityMapsBinding
import java.util.Locale

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
        val pengirimanData: Pengiriman? = intent.getParcelableExtra("PENGIRIMAN_DATA")

        if(pengirimanData != null) {
            binding.namaSopir.text = pengirimanData.name
            binding.alamatPengiriman.text = pengirimanData.address
            binding.noHpSopir.text = pengirimanData.phone
            binding.statusPengiriman.text = pengirimanData.status
        }
        else {
            Toast.makeText(this, "Data pengiriman tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


@SuppressLint("SuspiciousIndentation")
override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    mMap.uiSettings.isZoomControlsEnabled = true
    mMap.uiSettings.isIndoorLevelPickerEnabled = true
    mMap.uiSettings.isCompassEnabled = true
    mMap.uiSettings.isMapToolbarEnabled = true
    val pengirimanData: Pengiriman? = intent.getParcelableExtra("PENGIRIMAN_DATA")
    if(pengirimanData != null) {
        userLocation = LatLng(pengirimanData.supirLatitude, pengirimanData.supirLongitude)
        vendorLocation = LatLng(pengirimanData.mitraLatitude, pengirimanData.mitraLongitude)
        val geocoder = Geocoder(this, Locale.getDefault())
       val addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0].getAddressLine(0)
                binding.tvLocationRightNow.text = address.toString()
            } else {
                binding.tvLocationRightNow.text = "Alamat tidak ditemukan"
            }
    }
    else {
       Toast.makeText(this, "Data pengiriman tidak ditemukan", Toast.LENGTH_SHORT).show()
    }

    val truckIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_truck)

  val distance = calculateDistance(
    userLocation.latitude,
    userLocation.longitude,
    vendorLocation.latitude,
    vendorLocation.longitude
)
val distanceFormatted = String.format("%.2f", distance)
binding.distanceMapsContent.text = "${distanceFormatted} km"

    mMap.addMarker(
        MarkerOptions()
            .position(vendorLocation)
            .title("Dicoding Space")
            .snippet("Batik Kumeli No.50")
            .icon(truckIcon) // Set the custom icon here
    )
    zoomToLocations()

    findRoute(userLocation, vendorLocation)
}



    private fun zoomToLocations() {
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

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000 // Mengubah meter menjadi kilometer
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

