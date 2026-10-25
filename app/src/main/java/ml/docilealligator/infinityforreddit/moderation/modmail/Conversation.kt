package ml.docilealligator.infinityforreddit.moderation.modmail

import android.os.Parcelable
import androidx.room.Ignore
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.jvm.java

@Parcelize
data class Conversation(
    @SerializedName("isAuto") var isAuto: Boolean? = null,
    @SerializedName("participant") var participant: Participant? = Participant(),
    @SerializedName("objIds") var objIds: ArrayList<ObjId> = arrayListOf(),
    @SerializedName("isRepliable") var isRepliable: Boolean? = null,
    @SerializedName("lastUserUpdate") var lastUserUpdate: String? = null,
    // isInternal will be true for Mods Only conversation
    @SerializedName("isInternal") var isInternal: Boolean? = null,
    @SerializedName("lastModUpdate") var lastModUpdate: String? = null,
    @SerializedName("authors") var authors: ArrayList<Author> = arrayListOf(),
    @SerializedName("lastUpdated") var lastUpdated: String? = null,
    @SerializedName("legacyFirstMessageId") var legacyFirstMessageId: String? = null,
    @SerializedName("state") var state: Int? = null,
    @SerializedName("conversationType") var conversationType: String? = null,
    @SerializedName("lastUnread") var lastUnread: String? = null,
    // Owner should contain the subreddit name in its displayName field
    @SerializedName("owner") var owner: Owner? = Owner(),
    @SerializedName("subject") var subject: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("isHighlighted") var isHighlighted: Boolean? = null,
    @SerializedName("numMessages") var numMessages: Int? = null,
    @Ignore private var _messages: MutableList<ModMessage>? = mutableListOf(),
    @Ignore var isUpdated: Boolean
): Parcelable {
    var messages: MutableList<ModMessage>
        get() = _messages ?: mutableListOf<ModMessage>().also { _messages = it }
        set(newMessages) {
            _messages = newMessages
        }

    @IgnoredOnParcel
    @Transient
    private var _lastUpdatedUTC: Long? = null

    val lastUpdatedUTC: Long
        get() {
            if (_lastUpdatedUTC == null) {
                _lastUpdatedUTC = lastUpdated?.let {
                    val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxx")
                    OffsetDateTime.parse(it, formatter1).toInstant().toEpochMilli()
                } ?: 0
            }

            return _lastUpdatedUTC!!
        }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is Conversation) {
            return false
        }

        return other.id == id && other.messages == messages && other.isUpdated == isUpdated
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
                                    print(messagesJSONObject)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (ignore: Exception) {
                                    ignore.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }

        suspend fun fetchConversation(oauthRetrofit: Retrofit, accessToken: String, conversationId: String, gson: Gson): Conversation? {
            try {
                val response: Response<String> = oauthRetrofit.create(RedditAPIKt::class.java).getModMailConversation(
                    conversationId, APIUtils.getOAuthHeader(accessToken))
                if (response.isSuccessful && response.body() != null) {
                    val responseJson = JSONObject(response.body()!!)
                    return parseConversation(
                        gson, responseJson.getString(JSONUtils.CONVERSATION_KEY),
                        responseJson.getJSONObject(JSONUtils.MESSAGES_KEY)
                    ).apply {
                        isUpdated = true
                    }
                } else {
                    return null
                }
            } catch (e: IOException) {
                return null
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
    // displayName should be the subreddit name
    @SerializedName("displayName") var displayName: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("id") var id: String? = null
): Parcelable