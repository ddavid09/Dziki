package coddiers.hackyeah.dziki.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coddiers.hackyeah.dziki.MainActivity
import coddiers.hackyeah.dziki.R
import coddiers.hackyeah.dziki.database.DataBase
import coddiers.hackyeah.dziki.database.Report
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint


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
        MainActivity.btn.visibility = View.VISIBLE
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DataBase().getUserReports().observe( viewLifecycleOwner, Observer { arrayListOfUserReports ->  logAllUserReports(arrayListOfUserReports)})
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

