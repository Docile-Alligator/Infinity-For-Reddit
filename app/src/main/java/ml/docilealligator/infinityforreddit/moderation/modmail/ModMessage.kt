package ml.docilealligator.infinityforreddit.moderation.modmail

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class ModMessage(
    @SerializedName("body") var body: String? = null,
    @SerializedName("author") var author: Author? = Author(),
    @SerializedName("isInternal") var isInternal: Boolean? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("bodyMarkdown") var bodyMarkdown: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("participatingAs") var participatingAs: String? = null
): Parcelable {
    @IgnoredOnParcel
    @Transient
    private var _dateUTC: Long? = null

    val dateUTC: Long
        get() {
            if (_dateUTC == null) {
                _dateUTC = date?.let {
                    val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxx")
                    OffsetDateTime.parse(it, formatter1).toInstant().toEpochMilli()
                } ?: 0
            }

            return _dateUTC!!
        }

}