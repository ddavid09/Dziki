package coddiers.hackyeah.dziki

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.*
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_boar_notification_avtivity.*
import java.io.ByteArrayOutputStream
import java.io.File

class BoarNotificationAvtivity : AppCompatActivity() {
    private lateinit var boroughEditText: EditText
    private lateinit var voivodeshipEditText: EditText
    private lateinit var descritpionEditText: EditText
    private lateinit var intentToMap: Intent
    private lateinit var makeAPhotoButton: Button
    private lateinit var mLocationManager: LocationManager
    private lateinit var sweetPhotoOfPiggy: ImageView
    private lateinit var phtotByteArray: ByteArrayOutputStream
    private var deathStatus: Boolean = false;
    private val FILE_NAME = "photo.jpg"
    private val REQUEST_CODE = 42
    private var photoFile: File? = null

    var LOCATION_REFRESH_DISTANCE = 1f
    var LOCATION_REFRESH_TIME: Long = 100
    val REQUEST_IMAGE_CAPTURE = 1

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
        boroughEditText = findViewById(R.id.borough_text_view)
        voivodeshipEditText = findViewById(R.id.voivodeship_text_view)
        sweetPhotoOfPiggy = findViewById(R.id.sweetPhotoOfPiggy)
        descritpionEditText = findViewById(R.id.description_text_view)

        mLocationManager =getSystemService(LOCATION_SERVICE) as LocationManager;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        makeAPhotoButton = findViewById(R.id.make_a_photo_button)
        makeAPhotoButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            // This DOESN'T work for API >= 24 (starting 2016)
            // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)

            val fileProvider = FileProvider.getUriForFile(this, "package coddiers.hackyeah.dziki.fileprovider", photoFile!!)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }
        intentToMap = Intent(this, MapToApplicationActivity::class.java)



        topAppBar.setNavigationOnClickListener {
            finish();
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.accept -> {
                    Log.d("BoarNotificationAvtivity",voivodeshipEditText.text.toString())
                    Log.d("BoarNotificationAvtivity",boroughEditText.text.toString())
                    intentToMap.putExtra("region", voivodeshipEditText.text.toString())
                    intentToMap.putExtra("subregion", boroughEditText.text.toString())
                    intentToMap.putExtra("description", " " + descritpionEditText.text.toString())
                    startActivity(intentToMap)
                    true
                }

                else -> false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val takenImage = data?.extras?.get("data") as Bitmap
            val takenImage = BitmapFactory.decodeFile(photoFile?.absolutePath)
            sweetPhotoOfPiggy.setImageBitmap(takenImage)
            intentToMap.putExtra("img", photoFile?.absolutePath)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    private fun getPhotoFile(fileName: String): File {
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.alive ->
                    if (checked) {
                        deathStatus = false
                        intentToMap.putExtra("deathStatus", deathStatus)
                    }
                R.id.dead ->
                    if (checked) {
                        deathStatus = true
                        intentToMap.putExtra("deathStatus", deathStatus)
                    }
            }
        }
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
            intentToMap.putExtra("lng", location.longitude.toString())
            intentToMap.putExtra("region", location.adminArea.toLowerCase())
            intentToMap.putExtra("subregion", location.subAdminArea.toLowerCase())
            boroughEditText.setText("" + location.subAdminArea.toString().toLowerCase())
            voivodeshipEditText.setText("" + location.adminArea.toString().toLowerCase())
            Log.d("letter", location.adminArea.toLowerCase())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getAddressFromLocation(location: Location?) {
        val coder = Geocoder(this.applicationContext)
        val address: List<Address>?

        try{
            address = coder.getFromLocation(location!!.latitude, location.longitude, 1)
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