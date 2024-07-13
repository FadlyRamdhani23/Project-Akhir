package com.tugasakhir.udmrputra.ui.pengiriman

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tugasakhir.udmrputra.data.Pengiriman
import com.google.android.gms.maps.model.LatLng
import com.tugasakhir.udmrputra.R
import java.util.Locale

class DetailPengirimanViewModel(application: Application) : AndroidViewModel(application) {

    private val _pengirimanData = MutableLiveData<Pengiriman?>()
    val pengirimanData: LiveData<Pengiriman?> get() = _pengirimanData

    private val _userLocation = MutableLiveData<LatLng>()
    val userLocation: LiveData<LatLng> get() = _userLocation

    private val _vendorLocation = MutableLiveData<LatLng>()
    val vendorLocation: LiveData<LatLng> get() = _vendorLocation

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _distance = MutableLiveData<String>()
    val distance: LiveData<String> get() = _distance

    private val _durationText = MutableLiveData<String>()
    val durationText: LiveData<String> get() = _durationText

    fun setPengirimanData(data: Pengiriman?) {
        _pengirimanData.value = data
        data?.let {
            _userLocation.value = LatLng(it.supirLatitude, it.supirLongitude)
            _vendorLocation.value = LatLng(it.latitudeTujuan, it.LongitudeTujuan)
            updateAddress()
            updateDistance()
        }
    }

    private fun updateAddress() {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        val userLoc = _userLocation.value
        if (userLoc != null) {
            try {
                val addresses = geocoder.getFromLocation(userLoc.latitude, userLoc.longitude, 1)
                _address.value = addresses?.firstOrNull()?.getAddressLine(0) ?: getApplication<Application>().getString(
                    R.string.address_not_found)
            } catch (e: Exception) {
                _address.value = getApplication<Application>().getString(R.string.address_not_found)
            }
        }
    }

    private fun updateDistance() {
        val userLoc = _userLocation.value
        val vendorLoc = _vendorLocation.value
        if (userLoc != null && vendorLoc != null) {
            val distance = calculateDistance(userLoc.latitude, userLoc.longitude, vendorLoc.latitude, vendorLoc.longitude)
            _distance.value = String.format(Locale.getDefault(), "%.2f km", distance)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000 // Convert meters to kilometers
    }

    fun setDurationText(text: String) {
        _durationText.value = text
    }
}
