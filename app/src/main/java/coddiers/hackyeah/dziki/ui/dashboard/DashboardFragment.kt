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
import coddiers.hackyeah.dziki.ui.notifications.NotificationsFragment
import kotlinx.android.synthetic.main.fragment_dashboard.*


class DashboardFragment : Fragment() {

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

        val listView = reportsUserListView as ListView
        val arrReport: ArrayList<NotificationsFragment.Report> = ArrayList()
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
                                var exist = false
                                var elementToRemove: NotificationsFragment.Report? = null
                                for(reportItem in arrReport){
                                    if(reportItem.name==report.ID){
                                        exist=true
                                        elementToRemove = reportItem
                                    }
                                }
                                if(!exist){
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

    class CustomAdaptor(var context: Context, private var report: ArrayList<NotificationsFragment.Report>) : BaseAdapter() {
        private inner class ViewHolder(row: View?){
            var txtName: TextView = row?.findViewById(R.id.textName) as TextView
            var ivImage: ImageView = row?.findViewById(R.id.iveImgae) as ImageView
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

            val report: NotificationsFragment.Report = getItem(position) as NotificationsFragment.Report
            viewHolder.txtName.text = report.name
            viewHolder.ivImage.setImageBitmap(report.image)

            return view as View
        }
    }
}

