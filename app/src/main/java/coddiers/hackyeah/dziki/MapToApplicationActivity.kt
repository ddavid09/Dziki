package coddiers.hackyeah.dziki

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import coddiers.hackyeah.dziki.database.DataBase
import coddiers.hackyeah.dziki.database.Report
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
import java.io.File
import java.lang.Exception


class MapToApplicationActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraMoveListener {

    private lateinit var mMap: GoogleMap
    private lateinit var report: Report
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng
    private val database: DataBase = DataBase()
    companion object{ private const val LOCATION_PERMISSION_REQUEST_CODE = 1}
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_to_application)
        report = Report(
            region = intent.getStringExtra("region"),
            subregion = intent.getStringExtra("subregion"),
            locationGeoPoint = GeoPoint(intent.getStringExtra("lat").toDouble(),intent.getStringExtra("lng").toDouble() )
        )
        currentLocation = LatLng(report.locationGeoPoint.latitude, report.locationGeoPoint.longitude)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        topAppBar.setNavigationOnClickListener {
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.accept -> {
                    createReport()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("tab","map")
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun createReport()  {
        var bitmap : Bitmap? = null
        try {
            bitmap = getImageFromPath(intent.getStringExtra("img"))
        }catch (exp: IllegalStateException){
            bitmap = null
        }
        Log.d("DESC", intent.getStringExtra("description"))
        database.uploadReport(currentLocation,
                intent.getStringExtra("description"),
                bitmap,
                arrayListOf(1,0,0), intent.getBooleanExtra("deathStatus", false),
                intent.getStringExtra("region").toString().decapitalize(),
                intent.getStringExtra("subregion").toString(),
                "waszkowiakowskieborough")
    }

    private fun getImageFromPath(path: String) : Bitmap? {
        val imgFile = File(path)
        var bitmap: Bitmap? = null
        if(imgFile.exists()){
            bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        Log.d("IMAGE", bitmap.toString())
        return bitmap
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnCameraMoveListener(this)
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        marker = mMap.addMarker(MarkerOptions().position(currentLocation).title("Tutaj oznacz dzika!"))

        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true
        currentLocation = LatLng(report.locationGeoPoint.latitude, report.locationGeoPoint.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17f))
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onCameraMove() {
        currentLocation = mMap.cameraPosition.target
        changeMarkerPosition(marker, currentLocation)
    }

    private fun changeMarkerPosition(marker: Marker, location: LatLng) {
        marker.position = location
    }
}
