package com.example.mykumve.ui.map


import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mykumve.R
import android.graphics.Color
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.mykumve.util.LocalProperties
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    lateinit var assets: Any
    private lateinit var mMap: GoogleMap
    private val points = mutableListOf<LatLng>()
    private var polyline: Polyline? = null
    private val markerMap = mutableMapOf<String, Int>() // Map to store Marker ID and corresponding index

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Places SDK
        //val apiKey=R.string.myApi
        val apiKey=LocalProperties.getApiKey(requireContext())
        Places.initialize(requireContext(), apiKey)

        // Initialize Autocomplete fragment
        autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(
            Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                if (latLng != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(requireContext(), "Error In Search", Toast.LENGTH_SHORT).show()
            }
        })

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up map options button
        val mapOptions = view.findViewById<ImageButton>(R.id.map_menu)
        mapOptions.setOnClickListener { v ->
            // Initialize PopupMenu
            val popupMenu = PopupMenu(requireContext(), v)
            popupMenu.inflate(R.menu.map_options) // Inflate menu resource (use your own menu file)
            // Set a listener for menu item clicks
            popupMenu.setOnMenuItemClickListener { menuItem ->
                changeMap(menuItem.itemId)
                true
            }
            // Show the PopupMenu
            popupMenu.show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val location = LatLng(31.771959, 35.217018) // Jerusalem
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))

        mMap.setOnMarkerClickListener(this)

        mMap.setOnMapClickListener { latLng ->
            addMarker(latLng)
        }

        // Enable the My Location button
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Configure map settings
        val mapSettings = mMap.uiSettings
        mapSettings.isZoomControlsEnabled = true
        mapSettings.isCompassEnabled = true
        mapSettings.isMyLocationButtonEnabled = true

    }


    override fun onMarkerClick(marker: Marker): Boolean {
        val markerId = marker.id
        val index = markerMap[markerId]
        if (index != null) {
            points.removeAt(index)
            marker.remove()
            updateMarkerMapAfterRemoval(markerId)
            redrawPolyline()
        }
        return true
    }

    private fun addMarker(position: LatLng) {
        points.add(position)

        // Determine marker color
        val markerColor = if (points.size == 1) {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE) // First marker color (blue)
        } else {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW) // Default color for other markers
        }

        // Add marker with custom color
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(position)
                .title("Point ${points.size}")
                .icon(markerColor)
        )

        // Store marker ID and corresponding index in points list
        if (marker != null) {
            markerMap[marker.id] = points.size - 1
        }

        // Remove existing polyline
        polyline?.remove()

        // Redraw polyline
        drawPolyline()
    }

    private fun drawPolyline() {
        if (points.size >= 2) {
            val polylineOptions = PolylineOptions()
                .color(Color.BLACK)
                .width(5f)

            for (point in points) {
                polylineOptions.add(point)
            }

            polyline = mMap.addPolyline(polylineOptions)
        }
    }

    private fun redrawPolyline() {
        if (points.size >= 2) {
            val polylineOptions = PolylineOptions()
                .color(Color.BLACK)
                .width(5f)

            for (point in points) {
                polylineOptions.add(point)
            }

            polyline?.remove() // Remove old polyline
            polyline = mMap.addPolyline(polylineOptions)
        } else {
            // Remove the polyline if there are less than 2 points
            polyline?.remove()
        }
    }

    private fun updateMarkerMapAfterRemoval(removedMarkerId: String) {
        val removedIndex = markerMap[removedMarkerId]
        markerMap.remove(removedMarkerId)

        // Update indices for markers after the removed one
        val iterator = markerMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value > removedIndex!!) {
                markerMap[entry.key] = entry.value - 1
            }
        }
    }

    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normal_map -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.satellite_map -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
    }
}




