package coddiers.hackyeah.dziki.ui;


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import coddiers.hackyeah.dziki.R
import coddiers.hackyeah.dziki.database.DataBase
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_choose_marker_details.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class ChooseMarkerDetailsActivity : AppCompatActivity() {

    lateinit var confirmTrashIntent: Intent
    var btnClickable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_marker_details)

        val intent : Intent = getIntent()
        val locationLong = intent.getDoubleExtra("long", 0.0)
        val locationLat = intent.getDoubleExtra("lat", 0.0)
        val deleteButtonBoolean = intent.getBooleanExtra("button",false)
        val db = DataBase()
        val location = LatLng(locationLat, locationLong)

        val MLdetails = db.getFromLatLong(location)

        button.setOnClickListener {
            finish()
        }

        var deleteButton = delete_button as Button




        MLdetails.observe(this, Observer { reportDoc ->
            if (reportDoc != null) {

                //ChooseTrashActivityTextViewId.setText(it.description)
                var date = reportDoc.timestamp.toDate()
                val pattern = "dd-MM-yyyy hh:mm"
                val simpleDateFormat = SimpleDateFormat(pattern)
                val toPrintDate = simpleDateFormat.format(date)

                report_data.text = toPrintDate
                vojewudztwo.text = reportDoc.region
                poviat.text = reportDoc.subregion
                description.text = reportDoc.description

                val MLPicture = db.getPhoto(reportDoc, resources)
                MLPicture.observe(this, Observer {
                    if (it != null) {
                        ChooseMarkerDetailsImageViewId.setImageBitmap(it)
                        btnClickable = true
                    } else{
                        Toast.makeText(this, "Nie można pobrać zdjęcia", Toast.LENGTH_SHORT).show()
                    }
                })
                if(deleteButtonBoolean){
                    deleteButton.visibility= View.VISIBLE;
                    deleteButton.setOnClickListener {
                        DataBase().deleteReport(raportId = reportDoc.ID )
                        finish()
                    }
                }


            } else{
                //Log.d("ChooseTrashActivity", "out")
                Toast.makeText(this, "Nie można pobrać informacji", Toast.LENGTH_SHORT).show()
            }

        })


        }


    }

