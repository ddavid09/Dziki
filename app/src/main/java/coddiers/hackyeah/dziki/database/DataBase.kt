package coddiers.hackyeah.dziki.database

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import coddiers.hackyeah.dziki.R
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
    val TAG = "DataBase"
    val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun uploadToken(token: String): Task<Void>? {
        val userToken = UserToken(token)
        val user = FirebaseAuth.getInstance().currentUser
        if (user!=null){
            return db.collection("userTokens").document(user.uid).set(userToken).addOnSuccessListener {
                Log.d(TAG, "Token added with ID: ${it}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
        }
        else{
            Log.w(TAG, "User null")
            return null
        }
    }

    fun getUserReports(): MutableLiveData<ArrayList<Report>> {
        val user = FirebaseAuth.getInstance().currentUser
        val MLreports: MutableLiveData<ArrayList<Report>> = MutableLiveData<ArrayList<Report>>()
        if (user!=null){
            db.collection("reports")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .whereEqualTo("creatorID", user.uid)
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
    
    fun getFromLatLong(location: LatLng): MutableLiveData<Report>{
        val locationGeoPoint = GeoPoint(location.latitude, location.longitude)
        val MLreport :MutableLiveData<Report> = MutableLiveData()
        db.collection("reports")
                .whereEqualTo("locationGeoPoint", locationGeoPoint).limit(1).addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    for (doc in value!!) {
                        MLreport.value=doc.toObject(Report::class.java)
                        break
                    }
                }
        return MLreport
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun uploadReport(location: LatLng, description: String, bitmap: Bitmap?, wildBoar: ArrayList<Int>, dead: Boolean, region: String, subregion: String, borough: String): Task<Void>? {
        val locationGeoPoint = GeoPoint(location.latitude, location.longitude)
        val firebaseRef = db.collection("pendingReports").document()
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("User", user?.uid.toString())
        if(user != null){
            if (bitmap == null) {

                val report = Report(
                        firebaseRef.id,
                        user.uid,
                        wildBoar,
                        dead,
                        locationGeoPoint,
                        region,
                        subregion,
                        borough,
                        description,
                        Timestamp.now(),
                        "brak"
                )
                return firebaseRef.set(report)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot added with ID: ${firebaseRef.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
            } else {
                val report = Report(
                        firebaseRef.id,
                        user.uid,
                        wildBoar,
                        dead,
                        locationGeoPoint,
                        region,
                        subregion,
                        borough,
                        description,
                        Timestamp.now(),
                        firebaseRef.id
                )
                val storageRef = storage.reference
                val pictureRef = storageRef.child("photos/" + firebaseRef.id + ".jpg")
                val baos = ByteArrayOutputStream()

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                return pictureRef.putBytes(data)
                        .addOnSuccessListener {
                            Log.d(TAG, "Picture send id:" + firebaseRef.id)
                        }.addOnFailureListener { e ->
                            Log.w(TAG, "Error sending picture", e)
                        }.continueWithTask {
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
        else return null;


    }

    fun deleteReport(raportId: String): Task<Void> {
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("User", user?.uid.toString())
        val firebaseRef = db.collection("pendingDelete").document(user?.uid.toString())
        return firebaseRef.set(hashMapOf(
                "raportID" to raportId,
                "userID" to user?.uid.toString()
        )).addOnSuccessListener {
            Log.d(TAG, "DocumentSnapshot added with ID: ${firebaseRef.id}")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
    }

    fun getReports(dead: Boolean?, region: String, subregion: String, commune: String): MutableLiveData<ArrayList<Report>> {
        //borough:String
        val MLreports: MutableLiveData<ArrayList<Report>> = MutableLiveData<ArrayList<Report>>()

        if (dead == null) {
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
        } else {
            db.collection("reports")
                    .orderBy("timestamp", Query.Direction.DESCENDING).whereEqualTo("region", region)
                    .whereEqualTo("subregion", subregion).whereEqualTo("dead", dead)
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

    fun getPhoto(report: Report, resources: Resources): MutableLiveData<Bitmap> {
        val liveBitmap: MutableLiveData<Bitmap> = MutableLiveData()
        liveBitmap.value = BitmapFactory.decodeResource(resources, R.drawable.dzik);
        Log.w(TAG, "bitmap: " + liveBitmap.value.toString())
        if (report.photoID != "null") {
            val storageRef = storage.reference

            var pictureRef = storageRef.child("photos/" + report.ID + ".jpg")

            val localFile = File.createTempFile("image_" + report.ID, "jpg")

            pictureRef.getFile(localFile).addOnSuccessListener {
                liveBitmap.value = getResizedBitmap(BitmapFactory.decodeFile(localFile.absolutePath),500)

                Log.w(TAG, "Photo getting success.")
                // Local temp file has been created
            }.addOnFailureListener {
                Log.w(TAG, "Photo getting failed.")
                // Handle any errors
            }
        }

        return liveBitmap
    }
}







