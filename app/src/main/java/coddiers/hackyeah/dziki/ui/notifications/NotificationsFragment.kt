package coddiers.hackyeah.dziki.ui.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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

                when (voivodeship){
                    "dolnoslaskie" -> districts = resources.getStringArray(R.array.dolnoslaskie)
                    "kujawskopomorskie" -> districts = resources.getStringArray(R.array.kujawskopomorskie)
                    "lubelskie" -> districts = resources.getStringArray(R.array.lubelskie)
                    "lubuskie" -> districts = resources.getStringArray(R.array.lubuskie)
                    "lodzkie" -> districts = resources.getStringArray(R.array.lodzkie)
                    "malopolskie" -> districts = resources.getStringArray(R.array.malopolskie)
                    "mazowieckie" -> districts = resources.getStringArray(R.array.mazowieckie)
                    "opolskie" -> districts = resources.getStringArray(R.array.opolskie)
                    "podkarpackie" -> districts = resources.getStringArray(R.array.podkarpackie)
                    "podlaskie" -> districts = resources.getStringArray(R.array.podlaskie)
                    "pomorskie" -> districts = resources.getStringArray(R.array.pomorskie)
                    "slaskie" -> districts = resources.getStringArray(R.array.slaskie)
                    "swietokszyskie" -> districts = resources.getStringArray(R.array.swietokszyskie)
                    "warminskomazurskie" -> districts = resources.getStringArray(R.array.warminskomazurskie)
                    "wielkopolskie" -> districts = resources.getStringArray(R.array.wielkopolskie)
                    "zachodniopomorskie" -> districts = resources.getStringArray(R.array.zachodniopomorskie)

                }
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
//        arrReport.add(Report("pierwszy",R.drawable.live_boar))
//        arrReport.add(Report("pierwszy",R.drawable.ic_logo))
        listView.adapter = CustomAdaptor(requireContext(), arrReport)



        searchButton.setOnClickListener {

            voivodeship = voivodeshipSpinner.selectedItem.toString()
            district = districtSpinner.selectedItem.toString()

            var id: Int = radiogroup.checkedRadioButtonId
            var dead: Boolean? = null
            when(id){
                0 -> dead = null
                1 -> dead = false
                2 -> dead = true
            }
            val notifications = DataBase().getReports(dead, voivodeship, district, "")
            Log.d("Adapter", dead.toString())
            Log.d("Adapter", voivodeship.toString())
            Log.d("Adapter", district.toString())


            var descriptionList: ArrayList<String> = ArrayList()

            var photoList: ArrayList<Bitmap> = ArrayList()
            notifications.observe(viewLifecycleOwner, Observer { reportsList ->
                if (reportsList != null) {
                    Log.d("Adapter", reportsList.toString())
                    for (report in reportsList) {
                        //arrReport.add(Report("test",bitmap))
                        DataBase().getPhoto(report, resources).observe(viewLifecycleOwner, Observer { bitmap ->
                            if(bitmap !=null){
                                Log.d("Adapter", bitmap.toString())
                                arrReport.add(Report(report.creatorID,bitmap))
                            }else{
                                Log.d("Adapter", "Bitmapa Jest nullem")
                            }
                        })
                        Log.d("Adapter", report.toString())
                    }

                    listView.adapter = CustomAdaptor(requireContext(), arrReport)
                } else {
                    Log.d("Adapter", "ELSE")
                }
            })





        }
    }



}
