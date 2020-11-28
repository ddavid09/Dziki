package coddiers.hackyeah.dziki

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.*
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.ByteArrayOutputStream

class BoarNotificationAvtivity : AppCompatActivity() {
    private lateinit var cityEditText: EditText;
    private lateinit var boroughEditText: EditText;
    private lateinit var voivodeshipEditText: EditText;
    private lateinit var lat: EditText;
    private lateinit var lng: EditText;
    private lateinit var goToMapButton: Button
    private lateinit var intentToMap: Intent
    private lateinit var makeAPhotoButton: Button
    private lateinit var mLocationManager: LocationManager
    private lateinit var sweetPhotoOfPiggy: ImageView
    private lateinit var phtotByteArray: ByteArrayOutputStream

    var LOCATION_REFRESH_DISTANCE = 1f
    var LOCATION_REFRESH_TIME: Long = 100
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_CODE = 200

    private val mLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            getAddressFromLocation(location)
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            TODO("Not yet implemented")
        }

        override fun onProviderEnabled(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun onProviderDisabled(p0: String?) {
            Toast.makeText(applicationContext, "Nie udało się pobrać lokalizacji. Uzupełnij formularz ręcznie.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boar_notification_avtivity)
        cityEditText = findViewById(R.id.city_text_view)
        boroughEditText = findViewById(R.id.borough_text_view)
        voivodeshipEditText = findViewById(R.id.voivodeship_text_view)
        sweetPhotoOfPiggy = findViewById(R.id.sweetPhotoOfPiggy)
        lat = findViewById(R.id.lat)
        lng = findViewById(R.id.lng)

        mLocationManager =getSystemService(LOCATION_SERVICE) as LocationManager;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        makeAPhotoButton = findViewById(R.id.make_a_photo_button)
        makeAPhotoButton.setOnClickListener {
            capturePhoto()
        }
        goToMapButton = findViewById(R.id.go_to_map_button)
        intentToMap = Intent(this, MapToApplicationActivity::class.java)
        goToMapButton.setOnClickListener{
            startActivity(intentToMap)
        }

        cityEditText.setOnKeyListener(View.OnKeyListener { _, keyCode, keyevent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyevent.action == KeyEvent.ACTION_UP) {
                getAddress(cityEditText.text.toString())
                hideKeyboard(this)
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE && data != null){
            val bmp: Bitmap  = data?.extras?.get("data") as Bitmap
            sweetPhotoOfPiggy.setImageBitmap(data.extras?.get("data") as Bitmap)
            phtotByteArray = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, phtotByteArray)
            var byteArrayp = phtotByteArray.toByteArray()
            intentToMap.putExtra("img", byteArrayp)
        }
    }

    fun capturePhoto() {

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }



    private fun hideKeyboard(activity: Activity?) {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("SetTextI18n")
    private fun getAddress(name: String) {
        val coder = Geocoder(this.applicationContext)
        val address: List<Address>?

        try {
            address = coder.getFromLocationName(name, 5)
            val location: Address = address[0]
            intentToMap.putExtra("lat", location.latitude.toString())
            lat.setText("" + location.latitude.toString())
            lng.setText("" + location.longitude.toString())
            intentToMap.putExtra("lng", location.longitude.toString())
            intentToMap.putExtra("region", location.adminArea)
            intentToMap.putExtra("subregion", location.subAdminArea)
            boroughEditText.setText("" + location.subAdminArea.toString())
            voivodeshipEditText.setText("" + location.adminArea.toString())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getAddressFromLocation(location: Location?) {
        val coder = Geocoder(this.applicationContext)
        val address: List<Address>?

        try{
            address = coder.getFromLocation(location!!.latitude, location.longitude, 1)
            cityEditText.setText("" + address[0].locality)
            boroughEditText.setText("" + address[0].subAdminArea)
            voivodeshipEditText.setText(address[0].adminArea)
            intentToMap.putExtra("lng", location.longitude.toString())
            intentToMap.putExtra("region", address[0].adminArea.toString())
            intentToMap.putExtra("subregion", address[0].subAdminArea.toString())
            intentToMap.putExtra("lat", location.latitude.toString())
        }
        catch (exp: java.lang.Exception){
            Toast.makeText(this, "Nie udało się pobrac lokalizacji uzupełnij formularz ręcznie", Toast.LENGTH_LONG).show()
        }
    }
}