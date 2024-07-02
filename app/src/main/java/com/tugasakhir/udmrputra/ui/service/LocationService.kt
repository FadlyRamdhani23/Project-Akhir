package com.tugasakhir.udmrputra.ui.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.ui.sopir.SupirActivity

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val CHANNEL_ID = "LocationServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, SupirActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Tracking your location in the background")
            .setSmallIcon(R.drawable.icon_putra)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 120000 // Set interval to 2 minutes
            maxWaitTime = 120000 // Set max wait time to 2 minutes
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("LocationService", "onLocationResult: ${location.latitude}, ${location.longitude}")
                    saveLocationToFirebase(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun saveLocationToFirebase(latitude: Double, longitude: Double) {
        val userId: String = auth.currentUser?.uid ?: ""
        val pengirimanUpdate: Map<String, Any> = hashMapOf(
            "latitudeSupir" to latitude,
            "longitudeSupir" to longitude,
        )

        firestore.collection("pengiriman")
            .whereEqualTo("supirId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    document.reference.update(pengirimanUpdate)
                        .addOnSuccessListener {
                            Log.d("LocationService", "Location updated successfully for document: ${document.id}")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("LocationService", "Error updating location for document: ${document.id}, $exception")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LocationService", "Error getting documents: $exception")
            }


        firestore.collection("location")
            .add(pengirimanUpdate)
            .addOnSuccessListener { documentReference ->
                Log.d("LocationService", "Location saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("LocationService", "Error saving location: $e")
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
            Log.e("LocationService", "Error : " + exception.message)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }
}