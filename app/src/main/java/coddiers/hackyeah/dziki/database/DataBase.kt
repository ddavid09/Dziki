package coddiers.hackyeah.dziki.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File


class DataBase() {
    val db = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser
    val TAG = "DataBase"
    val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun uploadReport(location: LatLng, description: String, bitmap: Bitmap?, wildBoar: ArrayList<Int>, dead:Boolean, region:String, subregion:String, borough:String): Task<Void> {
        val locationGeoPoint = GeoPoint(location.latitude, location.longitude)
        val firebaseRef = db.collection("uploadReports").document()
        val report = Report(
            firebaseRef.id,
            user!!.uid,
            wildBoar,
            dead,
            locationGeoPoint,
            region,
            subregion,
            borough,
            description,
            Timestamp.now()
        )

        if(bitmap == null){
            return firebaseRef.set(report)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot added with ID: ${firebaseRef.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }
        else{
            val storageRef = storage.reference
            val pictureRef = storageRef.child("photos/" + firebaseRef.id + ".jpg")
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
            val data = baos.toByteArray()
            return pictureRef.putBytes(data)
                .addOnSuccessListener {
                    Log.d(TAG, "Picture send id:"+firebaseRef.id)
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Error sending picture", e)
                }.continueWithTask{
                    firebaseRef.set(report)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot added with ID: ${firebaseRef.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }

                }
        }
    }

    fun getReports(dead:Boolean?, region:String, subregion:String, commune:String): MutableLiveData<ArrayList<Report>> {
        //borough:String
        val MLreports :MutableLiveData<ArrayList<Report>> = MutableLiveData<ArrayList<Report>>()

        if(dead == null) {
            db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING).whereEqualTo("region", region)
                .whereEqualTo("subregion", subregion)
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    val reportList = ArrayList<Report>()
                    if (value != null) {
                        for (doc in value) {
                            doc.toObject(Report::class.java).let {
                                reportList.add(it)
                            }
                        }
                    }
                    MLreports.value = reportList
                }
        }
        else {
            db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING).whereEqualTo("region", region)
                .whereEqualTo("subregion", subregion).whereEqualTo("dead",dead)
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    val reportList = ArrayList<Report>()
                    if (value != null) {
                        for (doc in value) {
                            doc.toObject(Report::class.java).let {
                                reportList.add(it)
                            }
                        }
                    }
                    MLreports.value = reportList
                }
        }
        return MLreports

    }

    fun getPhoto(report: Report): MutableLiveData<Bitmap>{
        val liveBitmap: MutableLiveData<Bitmap> = MutableLiveData()
        val storageRef = storage.reference

        var pictureRef = storageRef.child("photos/"+ report.ID + ".jpg")

        val localFile = File.createTempFile("image_"+report.ID, "jpg")

        pictureRef.getFile(localFile).addOnSuccessListener {
            liveBitmap.value = BitmapFactory.decodeFile(localFile.absolutePath)
            // Local temp file has been created
        }.addOnFailureListener {
            // Handle any errors
        }
        return liveBitmap
    }








}