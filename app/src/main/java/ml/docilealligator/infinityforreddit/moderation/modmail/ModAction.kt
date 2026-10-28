package ml.docilealligator.infinityforreddit.moderation.modmail

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.lang.reflect.Type
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class ModAction(
    @SerializedName("date") val date: String,
    @SerializedName("actionTypeId") val actionType: ModActionType,
    @SerializedName("id") override val id: String,
    @SerializedName("author") val author: Author?
): ModMailItem {
    @IgnoredOnParcel
    @Transient
    private var _dateUTC: Long? = null

    val dateUTC: Long
        get() {
            if (_dateUTC == null) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxx")
                _dateUTC = OffsetDateTime.parse(date, formatter).toInstant().toEpochMilli()
            }

            return _dateUTC!!
        }
}

enum class ModActionType(val code: Int) {
    HIGHLIGHT(0),
    UN_HIGHLIGHT(1),
    ARCHIVE(2),
    UN_ARCHIVE(3),
    REPORTED_TO_ADMINS(4),
    MUTE(5),
    UN_MUTE(6),
    UNKNOWN(-1);

    companion object {
        fun fromCode(code: Int): ModActionType =
            entries.find { it.code == code } ?: UNKNOWN
    }
}

class ModActionTypeDeserializer : JsonDeserializer<ModActionType> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ModActionType {
        val code = json?.asInt ?: -1

        return ModActionType.fromCode(code)
    }
}