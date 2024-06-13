package com.tugasakhir.udmrputra.ui.pengiriman

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, RouteListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel: MapsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pengirimanData: Pengiriman? = intent.getParcelableExtra("PENGIRIMAN_DATA")
        mapsViewModel.setPengirimanData(pengirimanData)

        mapsViewModel.pengirimanData.observe(this, Observer { data ->
            if (data != null) {
                binding.namaSopir.text = data.name
                binding.alamatPengiriman.text = data.address
                binding.noHpSopir.text = data.phone
                binding.statusPengiriman.text = data.status.toString()

                if (data.status == 1) {
                    findViewById<ImageView>(R.id.image_status).visibility = View.VISIBLE
                    findViewById<FragmentContainerView>(R.id.google_map).visibility = View.GONE
                } else {
                    findViewById<ImageView>(R.id.image_status).visibility = View.GONE
                    findViewById<FragmentContainerView>(R.id.google_map).visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(this, "Data pengiriman tidak ditemukan", Toast.LENGTH_SHORT).show()
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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mapsViewModel.userLocation.observe(this, Observer { userLocation ->
            mapsViewModel.vendorLocation.observe(this, Observer { vendorLocation ->
                val truckIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_truck)
                mMap.addMarker(
                    MarkerOptions()
                        .position(vendorLocation)
                        .icon(truckIcon) // Set the custom icon here
                )
                zoomToLocations(userLocation, vendorLocation)
                findRoute(userLocation, vendorLocation)
            })
        })
    }

    private fun zoomToLocations(userLocation: LatLng, vendorLocation: LatLng) {
        val builder = LatLngBounds.builder()
        builder.include(userLocation)
        builder.include(vendorLocation)
        val bounds = builder.build()

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
                mapsViewModel.setDurationText(list[indexing].durationText)
            }
        }
    }

    override fun onRouteCancelled() {
        Log.d("Route", "Cancel Route")
    }
}
