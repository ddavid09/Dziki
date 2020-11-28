package coddiers.hackyeah.dziki.ui.notifications

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import coddiers.hackyeah.dziki.R
import kotlinx.android.synthetic.main.fragment_notifications.*


class NotificationsFragment : Fragment() {

    val types = arrayOf("simple User", "Admin")

    lateinit var oprion : Spinner
    lateinit var result : TextView

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
                button2.text = voivodeships[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }


        }
    }



}
