package ml.docilealligator.infinityforreddit.moderation.modmail

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import ml.docilealligator.infinityforreddit.extensions.toEpochMilliForModMail
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class ModMessage(
    @SerializedName("body") var body: String? = null,
    @SerializedName("author") var author: Author? = Author(),
    // isInternal will be true for Mods Only message
    @SerializedName("isInternal") var isInternal: Boolean? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("bodyMarkdown") var bodyMarkdown: String? = null,
    @SerializedName("id") override var id: String? = null,
    @SerializedName("participatingAs") var participatingAs: String? = null
): ModMailItem {
    @IgnoredOnParcel
    @Transient
    private var _dateUTC: Long? = null

    val dateUTC: Long
        get() {
            if (_dateUTC == null) {
                _dateUTC = date?.toEpochMilliForModMail() ?: 0
            }

            return _dateUTC!!
        }

}