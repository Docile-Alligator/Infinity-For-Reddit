package ml.docilealligator.infinityforreddit.moderation.modmail

import android.os.Parcelable
import androidx.room.Ignore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import ml.docilealligator.infinityforreddit.RedditError
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import ml.docilealligator.infinityforreddit.viewmodels.ModConversationAction
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

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
    /*
        0:  new
        1:  in progress
        2:  archived
        7:  conversation started by reddit??
    */
    @SerializedName("state") var state: Int? = null,
    @SerializedName("conversationType") var conversationType: String? = null,
    @SerializedName("lastUnread") var lastUnread: String? = null,
    // Owner should contain the subreddit name in its displayName field
    @SerializedName("owner") var owner: Owner? = Owner(),
    @SerializedName("subject") var subject: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("isHighlighted") var isHighlighted: Boolean? = null,
    @SerializedName("numMessages") var numMessages: Int? = null,
    @Ignore private var _modMailItems: MutableList<ModMailItem>? = mutableListOf(),
    @Ignore var isUpdated: Boolean,
    var isArchived: Boolean = state == 2,
    var isRead: Boolean = state != 0
): Parcelable {
    var modMailItems: MutableList<ModMailItem>
        get() = _modMailItems ?: mutableListOf<ModMailItem>().also { _modMailItems = it }
        set(newMessages) {
            _modMailItems = newMessages
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

        return other.id == id && other.modMailItems == modMailItems && other.isUpdated == isUpdated
    }

    companion object {
        fun parseConversation(
            gson: Gson,
            conversationJson: String,
            messagesJSONObject: JSONObject,
            modActionsJSONObject: JSONObject?
        ): Conversation {
            return gson.fromJson(conversationJson, Conversation::class.java).apply {
                for (objId in objIds) {
                    objId.key?.let { key ->
                        if (key == "messages") {
                            objId.id?.let { id ->
                                try {
                                    modMailItems.add(
                                        gson.fromJson(
                                            messagesJSONObject.getString(id),
                                            ModMessage::class.java
                                        )
                                    )
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (ignore: Exception) {
                                    ignore.printStackTrace()
                                }
                            }
                        } else if (key == "modActions" && modActionsJSONObject != null) {
                            objId.id?.let { id ->
                                try {
                                    modMailItems.add(
                                        gson.fromJson(
                                            modActionsJSONObject.getString(id),
                                            ModAction::class.java
                                        )
                                    )
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (ignore: Exception) {
                                    ignore.printStackTrace()
                                }
                            }
                        } else {

                        }
                    }
                }
            }
        }

        suspend fun fetchConversation(oauthRetrofit: Retrofit, accessToken: String, conversationId: String): Pair<Conversation, ModMailUser> {
            val response: Response<String> = oauthRetrofit.create(RedditAPIKt::class.java).getModMailConversation(
                conversationId, APIUtils.getOAuthHeader(accessToken))
            if (response.isSuccessful && response.body() != null) {
                val responseJson = JSONObject(response.body()!!)
                val gson = GsonBuilder()
                    .registerTypeAdapter(
                        ModActionType::class.java,
                        ModActionTypeDeserializer()
                    )
                    .create()
                val conversation = parseConversation(
                    gson, responseJson.getString(JSONUtils.CONVERSATION_KEY),
                    responseJson.getJSONObject(JSONUtils.MESSAGES_KEY),
                    if (responseJson.has(JSONUtils.MOD_ACTIONS_KEY)) responseJson.getJSONObject(JSONUtils.MOD_ACTIONS_KEY) else null
                ).apply {
                    isUpdated = true
                }
                val modMailUserResponse = ModMailUser.parseModMailUser(gson, responseJson.getString(JSONUtils.USER_KEY))

                return Pair(conversation, modMailUserResponse)
            } else {
                throw IOException(response.errorBody()?.string())
            }
        }

        suspend fun reply(
            oauthRetrofit: Retrofit,
            accessToken: String,
            conversationId: String,
            content: String,
            isAuthorHidden: Boolean,
            isInternal: Boolean
        ) {
            val response: Response<String> = oauthRetrofit.create(RedditAPIKt::class.java).replyToModMailConversation(
                conversationId,
                APIUtils.getOAuthHeader(accessToken),
                mutableMapOf<String, String>().apply {
                    this[APIUtils.BODY_KEY] = content
                    this[APIUtils.IS_AUTHOR_HIDDEN_KEY] = isAuthorHidden.toString()
                    this[APIUtils.IS_INTERNAL_KEY] = isInternal.toString()
                }
            )

            if (!response.isSuccessful) {
                // The response.body() will be the current conversation object but without the new message. Weird.
                response.errorBody()?.string().let {
                    val redditError: RedditError? = Gson().fromJson(it, RedditError::class.java)
                    redditError?.let { redditError ->
                        throw IOException(redditError.reason ?: "Could not send message")
                    }
                }

                throw IOException("Could not send message")
            }
        }

        suspend fun executeAction(
            oauthRetrofit: Retrofit,
            accessToken: String,
            conversationId: String,
            modConversationAction: ModConversationAction
        ) {
            val response: Response<String> = if (modConversationAction == ModConversationAction.UNHIGHLIGHT) {
                oauthRetrofit.create(RedditAPIKt::class.java).unhighlightModMailConversation(
                    "ModmailConversation_$conversationId",
                    APIUtils.getOAuthHeader(accessToken)
                )
            } else if (modConversationAction == ModConversationAction.READ || modConversationAction == ModConversationAction.UNREAD) {
                // Response will be empty
                oauthRetrofit.create(RedditAPIKt::class.java).toggleModMailConversationsRead(
                    modConversationAction.rawValue,
                    APIUtils.getOAuthHeader(accessToken),
                    mutableMapOf<String, String>().apply {
                        this[APIUtils.CONVERSATION_IDS_KEY] = conversationId
                    }
                )
            } else {
                oauthRetrofit.create(RedditAPIKt::class.java).executeModMailConversationAction(
                    "ModmailConversation_$conversationId",
                    modConversationAction.rawValue,
                    APIUtils.getOAuthHeader(accessToken)
                )
            }

            // The response does not have the updated conversation state.
            if (!response.isSuccessful) {
                response.errorBody()?.string().let {
                    val redditError: RedditError? = Gson().fromJson(it, RedditError::class.java)
                    redditError?.let { redditError ->
                        throw IOException(redditError.reason ?: "Failed")
                    }
                }

                throw IOException("Failed")
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
    // The key can be "messages" or "ModAction"
    @SerializedName("key") var key: String? = null
): Parcelable

@Parcelize
data class Owner(
    // displayName should be the subreddit name
    @SerializedName("displayName") var displayName: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("id") var id: String? = null
): Parcelable