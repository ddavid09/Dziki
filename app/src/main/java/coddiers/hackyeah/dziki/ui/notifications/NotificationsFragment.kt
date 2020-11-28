package coddiers.hackyeah.dziki.ui.notifications

import android.app.Activity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coddiers.hackyeah.dziki.R
import coddiers.hackyeah.dziki.database.DataBase
import coddiers.hackyeah.dziki.database.Report
import kotlinx.android.synthetic.main.fragment_notifications.*


class NotificationsFragment : Fragment() {

    var voivodeship = "dolnoslaskie"
    var district    = "bolesławiecki"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

        return root
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
        
        searchButton.setOnClickListener {
            var id: Int = radiogroup.checkedRadioButtonId
            var dead: Boolean? = null
            when(id){
                0 -> dead = null
                1 -> dead = false
                2 -> dead = true
            }

            val notifications = DataBase().getReports(dead, voivodeship, district, "")
            var idList: ArrayList<String> = ArrayList()
            var descriptionList: ArrayList<String> = ArrayList()
            notifications.observe(viewLifecycleOwner, Observer { reportsList ->
                if(reportsList!=null){
                    idList = ArrayList()
                    descriptionList = ArrayList()
                    for( report in reportsList){
                        idList.add(report.ID);
                        descriptionList.add(report.description)
                    }
                    val idArrayAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, idList)
                    photosList.adapter = idArrayAdapter

                    val descriptionArrayAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, descriptionList)
                    notificationList.adapter = descriptionArrayAdapter

                    Log.d("Adapter", photosList.toString())
                    Log.d("Adapter", notificationList.toString())
                }else{
                    Log.d("Adapter", "ELSE")
                }
            })



        }
    }



}
