package coddiers.hackyeah.dziki.ui.dashboard

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coddiers.hackyeah.dziki.MainActivity
import coddiers.hackyeah.dziki.R
import coddiers.hackyeah.dziki.database.DataBase
import kotlinx.android.synthetic.main.activity_choose_marker_details.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import com.google.firebase.Timestamp

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DashboardFragment : Fragment() {
    data class Report(var data: Timestamp,var name: String, var status:String, var ID: String, var image: Bitmap)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MainActivity.btn.visibility = View.GONE
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = reportsUserListView as ListView
        val arrReport: ArrayList<Report> = ArrayList()
        listView.adapter = CustomAdaptor(requireContext(), arrReport)

        val userReports = DataBase().getUserReports()
        userReports.observe(viewLifecycleOwner, Observer { reportsList ->
            arrReport.clear()
            if (reportsList!=null){
                for (report in reportsList) {
                    val status: String = if (!report.dead) "Martwy"
                    else "Żywy"
                    DataBase().getPhoto(report, resources).observe(
                        viewLifecycleOwner,
                        Observer { bitmap ->

                            if (bitmap != null) {
                                var exist = false

                                var elementToRemove: Report? = null
                                for(reportItem in arrReport){
                                    if(reportItem.ID==report.ID){
                                        exist=true
                                        elementToRemove = reportItem
                                    }
                                }
                                if(!exist){
                                    arrReport.add(Report(report.timestamp, report.region, report.subregion, report.ID , bitmap ))
                                }else{
                                    arrReport.remove(elementToRemove )
                                    arrReport.add(Report(report.timestamp, report.region, report.subregion, report.ID , bitmap  ))
                                }
                                Log.d("bitmapa",arrReport.toString())
                                listView.adapter = CustomAdaptor(requireContext(), arrReport)
                                Log.d("Adapter", report.toString())

                            } else {
                                Log.d("Adapter", "Bitmapa Jest nullem")
                            }
                        })
                }
            }else{
                Log.d("Adapter", "ELSE")
            }
        })
        listView.adapter = CustomAdaptor(requireContext(), arrReport)
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
}

