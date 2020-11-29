package coddiers.hackyeah.dziki.ui.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import coddiers.hackyeah.dziki.MainActivity
import coddiers.hackyeah.dziki.ItemViewModel
import coddiers.hackyeah.dziki.R
import coddiers.hackyeah.dziki.database.DataBase
import coddiers.hackyeah.dziki.database.Report
import coddiers.hackyeah.dziki.ui.ChooseMarkerDetailsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapFragment : Fragment(), LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveListener {
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var creating: Boolean = true
    private var counterCliks: Int = 0;
    private var locationClicked: LatLng? = null;
    private var lastMarker: Marker? = null;

    companion object{private const val LOCATION_PERMISSION_REQUEST_CODE = 1}

    private val viewModel: ItemViewModel by activityViewModels()
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        map.setOnMarkerClickListener(this)
        map.setOnCameraMoveListener(this)
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isCompassEnabled = true
        DataBase()
                .getReports(null, "mazowieckie", "Warszawa", "Bemowo")
                .observe(this, Observer { arrayListOfReports -> setMarkers(arrayListOfReports) })
        setUpMap()
        creating = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MainActivity.btn.visibility = View.VISIBLE
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return inflater.inflate(R.layout.fragment_map, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if(viewModel.getCurrentLocation() != null)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.getCurrentLocation(), 17f))
            else {
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                }
            }
        }
    }

    private fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): BitmapDescriptor? {
        val drawable = context?.let { ContextCompat.getDrawable(it, drawableId) }
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun setMarkers(listOfReports: ArrayList<Report>) {
        for (report in listOfReports) {
            val markerOptions = MarkerOptions().position(
                    LatLng(
                            report.locationGeoPoint.latitude,
                            report.locationGeoPoint.longitude
                    )
            )
            markerOptions.title("Wybierz")
            if (report.dead) {
                markerOptions.icon(getBitmapFromVectorDrawable(requireContext(), R.drawable.ic_dead_boar_marker))
                markerOptions.snippet("Martwy dzik")
                map.addMarker(markerOptions)
            } else {
                markerOptions.icon(getBitmapFromVectorDrawable(requireContext(), R.drawable.ic_lives_boar_marker))
                markerOptions.snippet("Żywy dzik")
                map.addMarker(markerOptions)
            }
        }
    }

        fun returnMarkerIcon(snippet: String?) : Int {
            return when (snippet) {
                "Martwy dzik" -> R.drawable.ic_dead_boar_marker
                "Żywy dzik" -> R.drawable.ic_lives_boar_marker
                else -> 0
            }
        }

    fun returnBiggerMarkerIcon(snippet: String?) : Int {
        return when (snippet) {
            "Martwy dzik" -> R.drawable.ic_dead_boar_marker_big
            "Żywy dzik" -> R.drawable.ic_lives_boar_marker_big
            else -> 0
        }
    }


    override fun onMarkerClick(p0: Marker?): Boolean {
        counterCliks++
        if((p0?.position!! == locationClicked)){
            var intent = Intent(context, ChooseMarkerDetailsActivity::class.java)
            intent.putExtra("long", p0?.position!!.longitude)
            intent.putExtra("lat", p0?.position!!.latitude)
            counterCliks = 0;
            locationClicked = null
            lastMarker?.hideInfoWindow()
            lastMarker?.setIcon(getBitmapFromVectorDrawable(requireContext(), returnMarkerIcon(lastMarker?.snippet)))
            startActivity(intent)
        }else{
            lastMarker?.setIcon(getBitmapFromVectorDrawable(requireContext(), returnMarkerIcon(lastMarker?.snippet)))
            lastMarker?.hideInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(p0?.position, 17f))
            p0?.showInfoWindow()
            p0?.setIcon((getBitmapFromVectorDrawable(requireContext(), returnBiggerMarkerIcon(p0?.snippet))))
            lastMarker = p0
            locationClicked = p0?.position
        }
        return true

    }

    override fun onCameraMove() {
        if(!creating) viewModel.setCurrentLocation(map.cameraPosition.target)

    }

    override fun onLocationChanged(location: Location?) {
        TODO("Not yet implemented")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("Not yet implemented")
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("Not yet implemented")
    }

    override fun onMapReady(p0: GoogleMap?) {
        TODO("Not yet implemented")
    }
}
