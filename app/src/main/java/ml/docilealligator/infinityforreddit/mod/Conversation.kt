package ml.docilealligator.infinityforreddit.mod

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Ignore
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import org.json.JSONObject
import java.io.IOException

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
    @SerializedName("numMessages") var numMessages: Int? = null,
    @Ignore val messages: MutableList<ModMessage> = mutableListOf()
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is Conversation) {
            return false
        }

        return other.id == id
    }

    companion object {
        fun parseConversation(gson: Gson, conversationJson: String, messagesJSONObject: JSONObject): Conversation {
            return gson.fromJson(conversationJson, Conversation::class.java).apply {
                for (objId in objIds) {
                    objId.key?.let { key ->
                        if (key == "messages") {
                            objId.id?.let { id ->
                                try {
                                    messages.add(gson.fromJson(messagesJSONObject.getString(id), ModMessage::class.java))
                                } catch (ignore: IOException) {
                                    ignore.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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
