package coddiers.hackyeah.dziki

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import coddiers.hackyeah.dziki.database.Report
import coddiers.hackyeah.dziki.ui.map.MapFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_boar_notification_avtivity.*


class MapToApplicationActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var report: Report;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    companion object{ private const val LOCATION_PERMISSION_REQUEST_CODE = 1}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_to_application)
        report = Report(
            region = intent.getStringExtra("region"),
            subregion = intent.getStringExtra("subregion"),
            locationGeoPoint = GeoPoint(intent.getStringExtra("lat").toDouble(),intent.getStringExtra("lng").toDouble() )
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        topAppBar.setNavigationOnClickListener {
            finish();
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.accept -> {
                    //TUTAJ button handler <------------------------------------------------------------------------------------------TUTAJ PIOTREK TU
                    true
                }

                else -> false
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMyLocationButtonClickListener(this)
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(report.locationGeoPoint.latitude, report.locationGeoPoint.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
                .draggable(true)
        )
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 17f))
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    override fun onMyLocationButtonClick(): Boolean {
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return false
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .draggable(true)
                )
            }
        }
        return false
    }
}