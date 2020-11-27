package coddiers.hackyeah.dziki.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Report(
    var ID: String = "brak",
    var creatorID: String = "null",
    var wildBoar: ArrayList<Int> = ArrayList(),
    var dead: Boolean = false,
    var locationGeoPoint: GeoPoint = GeoPoint(0.0,0.0),
    var region: String = "null",
    var subregion: String = "null",
    var borough: String = "null",
    var description: String = "null",
    var timestamp: Timestamp = Timestamp.now()
)

