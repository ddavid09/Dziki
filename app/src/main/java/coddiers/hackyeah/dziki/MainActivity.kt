package coddiers.hackyeah.dziki

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import coddiers.hackyeah.dziki.ui.InfoSliderActivity
import coddiers.hackyeah.dziki.ui.dashboard.DashboardFragment
import coddiers.hackyeah.dziki.ui.map.MapFragment
import coddiers.hackyeah.dziki.ui.notifications.NotificationsFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView


class ItemViewModel : ViewModel() {
    private val currentLocation = MutableLiveData<LatLng>()
    val selectedItem: LiveData<LatLng> get() = currentLocation

    fun getCurrentLocation(): LatLng? {
        return currentLocation.value
    }

    fun setCurrentLocation(location: LatLng){
        currentLocation.value = location
    }

}

class MainActivity : AppCompatActivity() {
    private val viewModel: ItemViewModel by viewModels()

    companion object {
        lateinit var btn: Button
    }

    fun replaceFragment(someFragment: Fragment?) {
        val transaction: FragmentTransaction =
            supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, someFragment!!)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectedItem.observe(this, Observer { item ->
            Log.w("Passing", "Przekazano: $item")
        })
        setContentView(R.layout.activity_main)
        val intentToMap = Intent(this, BoarNotificationAvtivity::class.java).apply {

        }
        btn = findViewById(R.id.extended_fab_add_new_boar)
        btn.setOnClickListener{
            startActivity(intentToMap)
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_info
            )
        )

        var nv = findViewById<BottomNavigationView>(R.id.nav_view)


        nv.setOnNavigationItemSelectedListener { item ->
            var fragment: Fragment? = null
                when(item.itemId) {

                R.id.navigation_info -> {
                    var intent = Intent(this, InfoSliderActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_map -> {
                    fragment = MapFragment();
                    replaceFragment(fragment)
                    true
                }R.id.navigation_dashboard -> {
                    fragment = DashboardFragment();
                    replaceFragment(fragment)
                    true
                }R.id.navigation_notifications -> {
                    fragment = NotificationsFragment();
                    replaceFragment(fragment)
                    true
                }

                else -> false
            }
        }

        var Intent = intent
        var fragment: Fragment? = null
        when(intent.getStringExtra("tab")) {
            "lista" -> {
                fragment = NotificationsFragment();
                replaceFragment(fragment)
                nv.selectedItemId = R.id.navigation_notifications
                true
            }
            else -> {
                fragment = MapFragment();
                replaceFragment(fragment)
            }
        }
    }
}