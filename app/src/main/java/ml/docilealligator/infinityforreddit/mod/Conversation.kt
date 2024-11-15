package ml.docilealligator.infinityforreddit.mod

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Conversation(
    @SerializedName("isAuto") var isAuto: Boolean? = null,
    @SerializedName("participant") var participant: Participant? = Participant(),
    @SerializedName("objIds") var objIds: ArrayList<ObjId> = arrayListOf(),
    @SerializedName("isRepliable") var isRepliable: Boolean? = null,
    @SerializedName("lastUserUpdate") var lastUserUpdate: String? = null,
    @SerializedName("isInternal") var isInternal: Boolean? = null,
    @SerializedName("lastModUpdate") var lastModUpdate: String? = null,
    @SerializedName("authors") var authors: ArrayList<Author> = arrayListOf(),
    @SerializedName("lastUpdated") var lastUpdated: String? = null,
    @SerializedName("legacyFirstMessageId") var legacyFirstMessageId: String? = null,
    @SerializedName("state") var state: Int? = null,
    @SerializedName("conversationType") var conversationType: String? = null,
    @SerializedName("lastUnread") var lastUnread: String? = null,
    @SerializedName("owner") var owner: Owner? = Owner(),
    @SerializedName("subject") var subject: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("isHighlighted") var isHighlighted: Boolean? = null,
    @SerializedName("numMessages") var numMessages: Int? = null
): Parcelable {
    val messages: MutableList<ModMessage> = mutableListOf()
}

@Parcelize
data class Participant(
    @SerializedName("isMod") var isMod: Boolean? = null,
    @SerializedName("isAdmin") var isAdmin: Boolean? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("isOp") var isOp: Boolean? = null,
    @SerializedName("isParticipant") var isParticipant: Boolean? = null,
    @SerializedName("isApproved") var isApproved: Boolean? = null,
    @SerializedName("isHidden") var isHidden: Boolean? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("isDeleted") var isDeleted: Boolean? = null
): Parcelable

@Parcelize
data class ObjId(
    @SerializedName("id") var id: String? = null,
    @SerializedName("key") var key: String? = null
): Parcelable

@Parcelize
data class Owner(
    @SerializedName("displayName") var displayName: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("id") var id: String? = null
): Parcelable
