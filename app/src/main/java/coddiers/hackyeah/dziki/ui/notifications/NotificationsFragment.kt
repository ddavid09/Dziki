package coddiers.hackyeah.dziki.ui.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.media.session.PlaybackState
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import coddiers.hackyeah.dziki.R
import coddiers.hackyeah.dziki.database.DataBase
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.report_item.*


class NotificationsFragment : Fragment() {

    lateinit var voivodeship : String
    lateinit var district   : String
    var dead: Boolean? = null

    data class Report(var name: String, var image: Bitmap)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val voivodeships = resources.getStringArray(R.array.voivodeships)
        var districts = resources.getStringArray(R.array.dolnoslaskie)
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, voivodeships)
        voivodeshipSpinner.adapter = arrayAdapter
        voivodeshipSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                var voivodeship = voivodeships[position]
                var resourcesID = resources.getIdentifier(voivodeship,"array",context?.packageName)
                if(resourcesID!=0){
                    Log.d("NotificationsFragment",resourcesID.toString())
                    districts = resources.getStringArray(resourcesID)
                    Log.d("NotificationsFragment",districts[0])
                }
                Log.d("NotificationsFragment","resourcesID: "+resourcesID+" "+context?.packageName)
                val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts)
                districtSpinner.adapter = arrayAdapter

                districtSpinner.onItemSelectedListener = object :

                    AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                    ) {
                        var district = districts[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }
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
                        DataBase().getPhoto(report, resources).observe(viewLifecycleOwner, Observer { bitmap ->
                            if(bitmap !=null){
                                arrReport.add(Report(report.description,bitmap))
                                listView.adapter = CustomAdaptor(requireContext(), arrReport)

                            }else{
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

        listView.onItemClickListener = AdapterView.OnItemClickListener{
            parent, view, position, id ->
            val selectedItemText = parent.getItemAtPosition(position)
            Log.d("Listener", selectedItemText.toString())
        }

    }

//    private fun getAddress(name: String) {
//        val coder = Geocoder(this.applicationContext)
//        val address: List<Address>?
//
//        try {
//            address = coder.getFromLocationName(name, 5)
//            val location: Address = address[0]
//            boroughEditText.setText("" + location.subAdminArea.toString())
//            voivodeshipEditText.setText("" + location.adminArea.toString())
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//        }
//    }

}
