package coddiers.hackyeah.dziki.ui.dashboard

import android.content.Context
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
import coddiers.hackyeah.dziki.database.Report
import coddiers.hackyeah.dziki.ui.notifications.NotificationsFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_notifications.*


class DashboardFragment : Fragment() {
    data class ReportData(
        var id: String,
        var timeStamp: Timestamp,
        var dead: Boolean,
        var location: GeoPoint,
        var description: String,
        var region: String,
        var subRegion: String,
        var borough: String,
        var creatorId: String,
        var photoId: String,
        var boarsList: ArrayList<Int>
        )

    private lateinit var dataList: ArrayList<ReportData>

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
        DataBase().getUserReports().observe( viewLifecycleOwner, Observer { arrayListOfUserReports ->  logAllUserReports(arrayListOfUserReports)})


        var listView = reportsUserListView as ListView
        var arrReport: ArrayList<NotificationsFragment.Report> = ArrayList()
        listView.adapter = CustomAdaptor(requireContext(), arrReport)

        val userReports = DataBase().getUserReports()
        userReports.observe(viewLifecycleOwner, Observer { reportsList ->
            arrReport.clear()
            if (reportsList!=null){
                for (report in reportsList) {
                    DataBase().getPhoto(report, resources).observe(
                        viewLifecycleOwner,
                        Observer { bitmap ->
                            if (bitmap != null) {
                                var exist:Boolean = false;
                                var elementToRemove: NotificationsFragment.Report? = null
                                for(reportItem in arrReport){
                                    if(reportItem.name==report.ID){
                                        exist=true;
                                        elementToRemove = reportItem
                                    }
                                }
                                if(exist==false){
                                    arrReport.add(NotificationsFragment.Report(report.ID, bitmap))
                                }else{
                                    arrReport.remove(elementToRemove )
                                    arrReport.add(NotificationsFragment.Report(report.ID, bitmap))
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

    class CustomAdaptor(var context: Context, var report: ArrayList<NotificationsFragment.Report>) : BaseAdapter() {

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

            var report: NotificationsFragment.Report = getItem(position) as NotificationsFragment.Report
            viewHolder.txtName.text = report.name
            viewHolder.ivImgae.setImageBitmap(report.image)

            return view as View
        }
    }

    private fun logAllUserReports(list: ArrayList<Report>){
        dataList = reportsListToReportDataList(list)
    }

    private fun reportsListToReportDataList(reports: ArrayList<Report>): ArrayList<ReportData>{
        val dataList: ArrayList<ReportData> = arrayListOf()
        for (report in reports){
            val reportData = ReportData(
                report.ID,
                report.timestamp,
                report.dead,
                report.locationGeoPoint,
                report.description,
                report.region,
                report.subregion,
                report.borough,
                report.creatorID,
                report.photoID,
                report.wildBoar)
            dataList.add(reportData)
        }
        return dataList
    }
}

