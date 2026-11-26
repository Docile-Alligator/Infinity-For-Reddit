package ml.docilealligator.infinityforreddit.moderation.modmail

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.jvm.java

@Parcelize
data class ModMailUser(
    @SerializedName("recentComments")
    val recentComments: Map<String, ModMailRecentComment> = emptyMap(),

    @SerializedName("muteStatus")
    var muteStatus: MuteStatus,

    @SerializedName("name")
    val name: String,

    @SerializedName("created")
    val created: String,

    @SerializedName("banStatus")
    var banStatus: BanStatus,

    @SerializedName("isSuspended")
    val isSuspended: Boolean,

    @SerializedName("approveStatus")
    var approveStatus: ApproveStatus,

    @SerializedName("isShadowBanned")
    val isShadowBanned: Boolean,

    /*@SerializedName("recentPosts")
    val recentPosts: Map<String, Any?> = emptyMap(),*/

    @SerializedName("recentConvos")
    val recentConvos: Map<String, RecentConvo> = emptyMap(),

    @SerializedName("id")
    val id: String
): Parcelable {
    fun isBanned(): Boolean {
        return banStatus.isBanned
    }

    fun ban(duration: Int, reason: String, isPermanent: Boolean) {
        if (isPermanent) {
            banStatus = BanStatus(null, reason, true, true)
        } else {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxx")
                .withZone(ZoneOffset.UTC)

            val endDate = formatter.format(
                Instant.now().plus(duration.toLong(), java.time.temporal.ChronoUnit.DAYS)
            )
            banStatus = BanStatus(endDate, reason, true, false)
        }
    }

    fun unban() {
        banStatus = BanStatus(null, "", false, false)
    }

    fun isMuted(): Boolean {
        return muteStatus.isMuted
    }

    fun mute(duration: Int) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxx")
            .withZone(ZoneOffset.UTC)

        val endDate = formatter.format(
            Instant.now().plus(duration.toLong(), java.time.temporal.ChronoUnit.HOURS)
        )
        muteStatus = MuteStatus(muteStatus.muteCount + 1, true, endDate, "")
    }

    fun unmute() {
        muteStatus = MuteStatus(muteStatus.muteCount, false, null, "")
    }

    fun isApproved(): Boolean {
        return approveStatus.isApproved
    }

    fun setApproved(isApproved: Boolean) {
        approveStatus = ApproveStatus(isApproved)
    }

    companion object {
        fun parseModMailUser(gson: Gson, json: String): ModMailUser {
            return gson.fromJson(json, ModMailUser::class.java)
        }
    }
}

@Parcelize
data class ModMailRecentComment(
    @SerializedName("comment")
    val comment: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("permalink")
    val permalink: String,

    @SerializedName("title")
    val title: String
): Parcelable

@Parcelize
data class MuteStatus(
    @SerializedName("muteCount")
    val muteCount: Int,

    @SerializedName("isMuted")
    val isMuted: Boolean,

    @SerializedName("endDate")
    val endDate: String?,

    @SerializedName("reason")
    val reason: String
): Parcelable

@Parcelize
data class BanStatus(
    @SerializedName("endDate")
    val endDate: String?,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("isBanned")
    val isBanned: Boolean,

    @SerializedName("isPermanent")
    val isPermanent: Boolean
): Parcelable

@Parcelize
data class ApproveStatus(
    @SerializedName("isApproved")
    val isApproved: Boolean
): Parcelable

@Parcelize
data class RecentConvo(
    @SerializedName("date")
    val date: String,

    @SerializedName("permalink")
    val permalink: String,

    @SerializedName("id")
    val id: String,

    @SerializedName("subject")
    val subject: String
): Parcelable {
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