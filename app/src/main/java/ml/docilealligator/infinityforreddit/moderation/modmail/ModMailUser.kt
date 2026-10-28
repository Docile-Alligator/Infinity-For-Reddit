package ml.docilealligator.infinityforreddit.moderation.modmail

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlin.jvm.java

@Parcelize
data class ModMailUser(
    @SerializedName("recentComments")
    val recentComments: Map<String, ModMailRecentComment> = emptyMap(),

    @SerializedName("muteStatus")
    val muteStatus: MuteStatus,

    @SerializedName("name")
    val name: String,

    @SerializedName("created")
    val created: String,

    @SerializedName("banStatus")
    val banStatus: BanStatus,

    @SerializedName("isSuspended")
    val isSuspended: Boolean,

    @SerializedName("approveStatus")
    val approveStatus: ApproveStatus,

    @SerializedName("isShadowBanned")
    val isShadowBanned: Boolean,

    /*@SerializedName("recentPosts")
    val recentPosts: Map<String, Any?> = emptyMap(),*/

    @SerializedName("recentConvos")
    val recentConvos: Map<String, RecentConvo> = emptyMap(),

    @SerializedName("id")
    val id: String
): Parcelable {
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
): Parcelable