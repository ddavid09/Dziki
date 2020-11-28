package coddiers.hackyeah.dziki

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.FragmentActivity

class BoarNotificationAvtivity : AppCompatActivity() {
    private lateinit var cityEditText: EditText;
    private lateinit var boroughEditText: EditText;
    private lateinit var voivodeshipEditText: EditText;
    private lateinit var lat: EditText;
    private lateinit var lng: EditText;
    private lateinit var goToMapButton: Button;
    private lateinit var intentToMap: Intent;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boar_notification_avtivity)
        cityEditText = findViewById(R.id.city_text_view)
        boroughEditText = findViewById(R.id.borough_text_view)
        voivodeshipEditText = findViewById(R.id.voivodeship_text_view)
        lat = findViewById(R.id.lat)
        lng = findViewById(R.id.lng)

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
}