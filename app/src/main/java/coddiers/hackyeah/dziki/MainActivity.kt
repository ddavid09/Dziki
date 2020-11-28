package coddiers.hackyeah.dziki

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng

class ItemViewModel : ViewModel() {
    private val currentLocation = MutableLiveData<LatLng>()
    val selectedItem: LiveData<LatLng> get() = currentLocation

    fun selectItem(item: LatLng) {
        currentLocation.value = item
    }

    fun getCurrentLocation(): LatLng? {
        Log.w("Passing", "1: ${currentLocation.value}")
        return currentLocation.value
    }

    fun setCurrentLocation(location: LatLng){
        currentLocation.value = location
        Log.w("Passing", "2: ${currentLocation.value}")
    }
}

class MainActivity : AppCompatActivity() {
    private val viewModel: ItemViewModel by viewModels()

    companion object {
        lateinit var btn: Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectedItem.observe(this, Observer { item ->
            Log.w("Passing", "Przekazano: $item")
        })
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val intentToMap = Intent(this, BoarNotificationAvtivity::class.java).apply {

        }
        val navController = findNavController(R.id.nav_host_fragment)
        btn = findViewById(R.id.extended_fab_add_new_boar)
        btn.setOnClickListener{
            startActivity(intentToMap)
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}