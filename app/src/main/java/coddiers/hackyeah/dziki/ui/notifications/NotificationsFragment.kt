package coddiers.hackyeah.dziki.ui.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
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
import coddiers.hackyeah.dziki.ui.dashboard.DashboardFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_notifications.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotificationsFragment : Fragment() {
    private lateinit var voivodeship : String
    private lateinit var district   : String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var positions = arrayOf("test","test")
    private var dead: Boolean? = null

    data class Report(var data: Timestamp, var name: String, var status:String, var ID: String, var image: Bitmap)
        override fun onCreateView(

            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
        MainActivity.btn.visibility = View.GONE
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                positions = getAddressFromLocation(location)
                Log.d("lokacja1", positions[0])
                Log.d("lokacja2", positions[1])
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
            android.R.layout.simple_spinner_dropdown_item,
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
                val voivodeship = voivodeships[position]
                val resourcesID = resources.getIdentifier(
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

                val list : ArrayList<String> = arrayListOf()
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

        class CustomAdaptor(var context: Context, private var report: ArrayList<Report>) : BaseAdapter() {
            private inner class ViewHolder(row: View?){
                var data: TextView = row?.findViewById(R.id.data_txt) as TextView
                var wojewodztwo: TextView = row?.findViewById(R.id.item_wojewodztwo_txt) as TextView
                var powiatName: TextView = row?.findViewById(R.id.item_powiat_txt) as TextView
                var ivImage: ImageView = row?.findViewById(R.id.imageinput) as ImageView

            }

            override fun getCount(): Int {
                return report.count()
            }

            override fun getItem(position: Int): Any {
                return report[position]

            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view: View?
                val viewHolder: ViewHolder
                if (convertView == null){
                    val layout = LayoutInflater.from(context)
                    view = layout.inflate(R.layout.report_item, parent, false)
                    viewHolder = ViewHolder(view)
                    view.tag = viewHolder
                }else{
                    view = convertView
                    viewHolder = view.tag as ViewHolder
                }


                val report: Report = getItem(position) as Report

                var date = report.data.toDate()
                val pattern = "dd-MM-yyyy hh:mm"
                val simpleDateFormat = SimpleDateFormat(pattern)
                val toPrintDate = simpleDateFormat.format(date)


                viewHolder.data.text = toPrintDate
                viewHolder.wojewodztwo.text = report.name
                viewHolder.powiatName.text = report.status
                viewHolder.ivImage.setImageBitmap(report.image)

                return view as View
            }
        }

        val listView = notificationList as ListView
        val arrReport: ArrayList<Report> = ArrayList()
        listView.adapter = CustomAdaptor(requireContext(), arrReport)

        searchButton.setOnClickListener {
            arrReport.clear()
            voivodeship = voivodeshipSpinner.selectedItem.toString()
            district = districtSpinner.selectedItem.toString()

            if(radioButton7.isChecked) dead = null
            if(radioButton8.isChecked) dead = true
            if(radioButton9.isChecked) dead = false
            val notifications = DataBase().getReports(dead, voivodeship, district, "")
            notifications.observe(viewLifecycleOwner, Observer { reportsList ->
                if (reportsList != null) {
                    arrReport.clear()
                    for (report in reportsList) {
                        val status: String = if (!report.dead) "Martwy"
                        else "Żywy"
                        DataBase().getPhoto(report, resources).observe(
                            viewLifecycleOwner,
                            Observer { bitmap ->
                                if (bitmap != null) {
                                    var exist = false
                                    var elementToRemove:Report? = null
                                    for(reportItem in arrReport){
                                        if(reportItem.name==report.ID){
                                            exist=true
                                            elementToRemove = reportItem
                                        }
                                    }
                                    if(!exist){
                                        arrReport.add(Report(report.timestamp, report.region, report.subregion, report.ID , bitmap ))
                                    }else{
                                        arrReport.remove(elementToRemove )
                                        arrReport.add(Report(report.timestamp, report.region, report.subregion, report.ID , bitmap ))
                                    }
                                    Log.d("bitmapa",arrReport.toString())
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
        listView.onItemClickListener = AdapterView.OnItemClickListener{ parent, _, position, _ ->
            val selectedItemText = parent.getItemAtPosition(position)
            Log.d("Listener", "selectedItemText.toString()")

        }
    }

    private fun getAddressFromLocation(location: Location?): Array<String> {
        val coder = Geocoder(this.requireContext())
        val address: List<Address>?
        var districts = resources.getStringArray(R.array.dolnoslaskie)

        address = coder.getFromLocation(location!!.latitude, location.longitude, 1)
        val voivodeshipFromLocation = address[0].adminArea.toLowerCase(Locale.ROOT)
        val districtFromLocation = address[0].subAdminArea

        //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Adapter wojewodztw $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
        val voivodeshipAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.voivodeships,
            android.R.layout.simple_spinner_item
        )
        voivodeshipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voivodeshipSpinner.adapter = voivodeshipAdapter

        val spinnerPosition = voivodeshipAdapter.getPosition(voivodeshipFromLocation)
        voivodeshipSpinner.setSelection(spinnerPosition)

        //$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Adapter powiatow $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
        val resourcesID = resources.getIdentifier(
            voivodeshipFromLocation,
            "array",
            context?.packageName
        )
        if(resourcesID!=0){
            districts = resources.getStringArray(resourcesID)
        }
        Log.d("Listenerres", resourcesID.toString())
        Log.d("Listenerdistr", districts.toString())

        val districtsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            districts
        )
        districtsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        districtSpinner.adapter = districtsAdapter
        val spinnerDistrictPosition = districtsAdapter.getPosition(districtFromLocation)
        Log.d("Listener1",spinnerDistrictPosition.toString())
        districtSpinner.setSelection(spinnerDistrictPosition)
        Log.d("Listener2",spinnerDistrictPosition.toString())

        return arrayOf(voivodeshipFromLocation, districtFromLocation)
    }

}
