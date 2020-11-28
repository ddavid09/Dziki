package coddiers.hackyeah.dziki.ui.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coddiers.hackyeah.dziki.MainActivity
import coddiers.hackyeah.dziki.R
import coddiers.hackyeah.dziki.database.DataBase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_notifications.*
import java.io.ByteArrayOutputStream


class NotificationsFragment : Fragment() {

    lateinit var voivodeship : String
    lateinit var district   : String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var positions = arrayOf("test","test")
    var dead: Boolean? = null
    private lateinit var cityEditText: EditText;
    private lateinit var boroughEditText: EditText;
    private lateinit var voivodeshipEditText: EditText;
    private lateinit var intentToMap: Intent
    private lateinit var makeAPhotoButton: Button
    private lateinit var mLocationManager: LocationManager
    private lateinit var sweetPhotoOfPiggy: ImageView
    private lateinit var phtotByteArray: ByteArrayOutputStream

    data class Report(var name: String, var image: Bitmap)
        override fun onCreateView(

            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
        MainActivity.btn.visibility = View.GONE;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                positions = getAddressFromLocation(location)
                Log.d("lokacja1", positions[0].toString())
                Log.d("lokacja2", positions[1].toString())
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }




        val voivodeships = resources.getStringArray(R.array.voivodeships)
        var districts = resources.getStringArray(R.array.dolnoslaskie)
        val voivodeshipAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            voivodeships
        )
        voivodeshipSpinner.adapter = voivodeshipAdapter
        voivodeshipSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var voivodeship = voivodeships[position]
                var resourcesID = resources.getIdentifier(
                    voivodeship,
                    "array",
                    context?.packageName
                )
                if(resourcesID!=0){
                    Log.d("NotificationsFragment", resourcesID.toString())
                    districts = resources.getStringArray(resourcesID)
                    Log.d("NotificationsFragment", districts[0])
                }
                Log.d(
                    "NotificationsFragment",
                    "resourcesID: " + resourcesID + " " + context?.packageName
                )
                val districtsAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    districts
                )

                var list : ArrayList<String> = arrayListOf()
                list.clear()
                for (x in districts){
                    list.add(x)
                }
                if(list.contains(positions[1])){
                    districtSpinner.adapter = districtsAdapter
                    Log.d("position",list.toString())
                    Log.d("position",positions[1])
                    Log.d("position", districtsAdapter.getPosition(positions[1]).toString())
                    districtSpinner.setSelection(districtsAdapter.getPosition(positions[1]))
                }else{
                    districtSpinner.adapter = districtsAdapter
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        class CustomAdaptor(var context: Context, var report: ArrayList<Report>) : BaseAdapter() {

            private inner class ViewHolder(row: View?){
                var txtName: TextView
                var ivImgae: ImageView

                init {
                    this.txtName = row?.findViewById(R.id.textName) as TextView
                    this.ivImgae = row?.findViewById(R.id.iveImgae) as ImageView
                }
            }

            override fun getCount(): Int {
                return report.count()
            }

            override fun getItem(position: Int): Any {
                return report.get(position)

            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                var view: View?
                var viewHolder: ViewHolder
                if (convertView == null){
                    var layout = LayoutInflater.from(context)
                    view = layout.inflate(R.layout.report_item, parent, false)
                    viewHolder = ViewHolder(view)
                    view.tag = viewHolder
                }else{
                    view = convertView
                    viewHolder = view.tag as ViewHolder
                }

                var report: Report = getItem(position) as Report
                viewHolder.txtName.text = report.name
                viewHolder.ivImgae.setImageBitmap(report.image)

                return view as View
            }
        }



        var listView = notificationList as ListView
        var arrReport: ArrayList<Report> = ArrayList()
        listView.adapter = CustomAdaptor(requireContext(), arrReport)

        searchButton.setOnClickListener {
            arrReport.clear()
            voivodeship = voivodeshipSpinner.selectedItem.toString()
            district = districtSpinner.selectedItem.toString()

            if(radioButton7.isChecked) dead = null
            if(radioButton8.isChecked) dead = false
            if(radioButton9.isChecked) dead = true
            val notifications = DataBase().getReports(dead, voivodeship, district, "")
            notifications.observe(viewLifecycleOwner, Observer { reportsList ->
                if (reportsList != null) {
                    for (report in reportsList) {
                        DataBase().getPhoto(report, resources).observe(
                            viewLifecycleOwner,
                            Observer { bitmap ->
                                if (bitmap != null) {
                                    arrReport.add(Report(report.description, bitmap))
                                    listView.adapter = CustomAdaptor(requireContext(), arrReport)
                                    Log.d("Adapter", report.toString())

                                } else {
                                    Log.d("Adapter", "Bitmapa Jest nullem")
                                }
                            })
                    }
                } else {
                    Log.d("Adapter", "ELSE")
                }
            })
            listView.adapter = CustomAdaptor(requireContext(), arrReport)
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
            val selectedItemText = parent.getItemAtPosition(position)
            Log.d("Listener", selectedItemText.toString())
        }




    }

    private fun getAddressFromLocation(location: Location?): Array<String> {
        val coder = Geocoder(this.requireContext())
        val address: List<Address>?
        var districts = resources.getStringArray(R.array.dolnoslaskie)

//        try{
            address = coder.getFromLocation(location!!.latitude, location.longitude, 1)
            val voivodeshipFromLocation = address[0].adminArea.toLowerCase()
            val districtFromLocation = address[0].subAdminArea





            //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Adapter wojewodztw $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
            val voivodeshipAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.voivodeships,
                android.R.layout.simple_spinner_item
            )
            voivodeshipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            voivodeshipSpinner.setAdapter(voivodeshipAdapter)

            val spinnerPosition = voivodeshipAdapter.getPosition(voivodeshipFromLocation)
            voivodeshipSpinner.setSelection(spinnerPosition)



            //$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Adapter powiatow $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

            var voivodeship = voivodeshipFromLocation

            var resourcesID = resources.getIdentifier(
                voivodeship,
                "array",
                context?.packageName
            )

            districts = resources.getStringArray(resourcesID)
            Log.d("Listenerres", resourcesID.toString())
            Log.d("Listenerdistr", districts.toString())

            val districtsAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                districts
            )
            districtsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            districtSpinner.setAdapter(districtsAdapter)
            val spinnerDistrictPosition = districtsAdapter.getPosition(districtFromLocation)
            Log.d("Listener1",spinnerDistrictPosition.toString())
            districtSpinner.setSelection(spinnerDistrictPosition)
            Log.d("Listener2",spinnerDistrictPosition.toString())

        val positions = arrayOf(voivodeshipFromLocation, districtFromLocation)
        return positions
    }

}
