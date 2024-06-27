package com.tugasakhir.udmrputra.ui.mitra

import android.location.Address
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R

class LocationAdapter(
    private val onLocationSelected: (Address) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    private var locations: List<Address> = emptyList()

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val locationTextView: TextView = view.findViewById(R.id.textViewAddress)
        val latitudeTextView: TextView = view.findViewById(R.id.textViewLatitude)
        val longitudeTextView: TextView = view.findViewById(R.id.textViewLongitude)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.locationTextView.text = location.getAddressLine(0)
        holder.latitudeTextView.text = location.latitude.toString()
        holder.longitudeTextView.text = location.longitude.toString()
        holder.itemView.setOnClickListener {
            onLocationSelected(location)
        }
    }

    override fun getItemCount(): Int = locations.size

    fun updateLocations(newLocations: List<Address>) {
        locations = newLocations
        notifyDataSetChanged()
    }
}
