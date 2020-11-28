package coddiers.hackyeah.dziki.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserToken(
    var Token: String = "brak"
)

