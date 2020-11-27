package coddiers.hackyeah.dziki.ui.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import coddiers.hackyeah.dziki.R


class DashboardFragment : Fragment() {
    private lateinit var cityEditText: EditText;
    private lateinit var boroughEditText: EditText;
    private lateinit var voivodeshipEditText: EditText;
    private lateinit var goToMapButton: Button;


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        cityEditText = root.findViewById(R.id.city_text_view)
        boroughEditText = root.findViewById(R.id.borough_text_view)
        voivodeshipEditText = root.findViewById(R.id.voivodeship_text_view)
        goToMapButton = root.findViewById(R.id.go_to_map_button)
        val intent = Intent(this.activity, MapToApplicationActivity::class.java)
        goToMapButton.setOnClickListener{
            activity?.startActivity(intent)
        }

        cityEditText.setOnKeyListener(View.OnKeyListener { _, keyCode, keyevent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyevent.action == KeyEvent.ACTION_UP) {
                getAddress(cityEditText.text.toString())
                hideKeyboard(this.activity)
                return@OnKeyListener true
            }
            false
        })
        return root
    }

    @SuppressLint("SetTextI18n")
    private fun getAddress(name: String) {
        val coder = Geocoder(context)
        val address: List<Address>?

        try {
            address = coder.getFromLocationName(name, 5)
            val location: Address = address[0]
            boroughEditText.setText("" + location.subAdminArea.toString())
            voivodeshipEditText.setText("" + location.adminArea.toString())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun hideKeyboard(activity: FragmentActivity?) {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

