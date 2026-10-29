package ml.docilealligator.infinityforreddit.repositories

import android.content.SharedPreferences
import androidx.paging.LoadState
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.state.DataLoadState
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.moderation.modmail.Conversation
import ml.docilealligator.infinityforreddit.moderation.modmail.ModAction
import ml.docilealligator.infinityforreddit.moderation.modmail.ModActionTypeDeserializer
import ml.docilealligator.infinityforreddit.moderation.modmail.ModMailUser
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import ml.docilealligator.infinityforreddit.viewmodels.ModConversationAction
import ml.docilealligator.infinityforreddit.viewmodels.ModMailState
import org.json.JSONObject
import retrofit2.Retrofit
import kotlin.coroutines.cancellation.CancellationException

class ModMailConversationRepository(
    private val oauthRetrofit: Retrofit,
    private val accessToken: String,
    private val sharedPreferences: SharedPreferences,
    private val sort: String
) {
    private val _conversations = MutableStateFlow<List<Conversation>?>(null)
    val conversations = _conversations.asStateFlow()

    private val _modMailUser = MutableStateFlow<ModMailUser?>(null)
    val modMailUser = _modMailUser.asStateFlow()

    // Conversations are saved in _conversations
    private val _loadState: MutableStateFlow<DataLoadState<Unit>> = MutableStateFlow(DataLoadState.Idle)
    val loadState = _loadState.asStateFlow()

    private val _isRefreshingConversations: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshingConversations = _isRefreshingConversations.asStateFlow()

    private var conversationsAfter: String? = null

    private var conversationIds = mutableSetOf<String>()

    private var _conversationMessagesDataLoadState = MutableStateFlow<DataLoadState<Unit>>(DataLoadState.Idle)
    val conversationMessagesDataLoadState = _conversationMessagesDataLoadState.asStateFlow()

    suspend fun initialLoadConversations(state: ModMailState) {
        if (_loadState.value == DataLoadState.Idle) {
            loadConversations(state)
        }
    }

    suspend fun refreshConversations(state: ModMailState) {
        _isRefreshingConversations.value = true
        conversationIds.clear()
        conversationsAfter = null
        loadConversations(state)
        _isRefreshingConversations.value = false
    }

    suspend fun loadNextPageConversations(state: ModMailState) {
        if (_conversations.value.isNullOrEmpty() || conversationsAfter.isNullOrEmpty()) {
            return
        }

        if (_loadState.value != DataLoadState.Loading) {
            loadConversations(state)
        }
    }

    private suspend fun loadConversations(state: ModMailState) {
        try {
            _loadState.value = DataLoadState.Loading
            val response = oauthRetrofit.create(RedditAPIKt::class.java)
                .getModMailConversations(
                    APIUtils.getOAuthHeader(accessToken),
                    state.rawValue,
                    sort,
                    conversationsAfter
                )

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val conversations: MutableList<Conversation>? = parseConversations(body)
                    conversations?.let {
                        val deduplicatedValue = it.filter { conversation ->
                            conversation.id?.let { id ->
                                conversationIds.add(id)
                            } ?: false
                        }
                        if (_isRefreshingConversations.value) {
                            _conversations.value = deduplicatedValue
                        } else {
                            val currentValue = _conversations.value
                            if (currentValue == null) {
                                _conversations.value = deduplicatedValue
                            } else {
                                _conversations.value = currentValue + deduplicatedValue
                            }
                        }
                        conversationsAfter = if (deduplicatedValue.isEmpty()) null else "ModmailConversation_" + deduplicatedValue[deduplicatedValue.size - 1].id
                    } ?: run {
                        conversationsAfter = null
                    }

                    _loadState.value = DataLoadState.Success(Unit)
                }
            } else {
                _loadState.value = DataLoadState.Error(response.errorBody()?.string() ?: "Error loading conversations")
            }
        } catch (e: Exception) {
            e.printStackTrace()

            if (e is CancellationException) {
                _loadState.value = DataLoadState.Error("Error loading conversations")
                throw e
            } else {
                _loadState.value = DataLoadState.Error(e.message ?: "Error loading conversations")
            }
        }
    }

    private suspend fun parseConversations(response: String): MutableList<Conversation>? {
        return withContext(Dispatchers.Default) {
            val json = JSONObject(response)
            val conversationIdsArray = json.getJSONArray(JSONUtils.CONVERSATION_IDS_KEY)
            if (conversationIdsArray.length() == 0) {
                null
            } else {
                val gson = Gson()
                val conversations: MutableList<Conversation> = mutableListOf()

                for (i in 0 until conversationIdsArray.length()) {
                    val conversationId = conversationIdsArray.getString(i)
                    try {
                        conversations.add(
                            Conversation.parseConversation(
                            gson,
                            json.getJSONObject(JSONUtils.CONVERSATIONS_KEY).getString(conversationId),
                            json.getJSONObject(JSONUtils.MESSAGES_KEY),
                                if (json.has(JSONUtils.MOD_ACTIONS_KEY)) json.getJSONObject(JSONUtils.MOD_ACTIONS_KEY) else null
                            )
                        )
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (ignore: Exception) {
                        ignore.printStackTrace()
                    }
                }

                conversations
            }
        }
    }

    suspend fun refreshMessagesInConversation(conversation: Conversation) {
        conversation.id?.let { id ->
            if (_conversationMessagesDataLoadState.value !is DataLoadState.Loading) {
                _conversationMessagesDataLoadState.value = DataLoadState.Loading
            }

            try {
                val (updatedConversation, modMailUser) = Conversation.fetchConversation(
                    oauthRetrofit, accessToken, id
                )

                _modMailUser.value = modMailUser

                updateConversation(conversation, updatedConversation)

                _conversationMessagesDataLoadState.value = DataLoadState.Success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                _conversationMessagesDataLoadState.value = DataLoadState.Error(e.message ?: "Error refreshing the conversation")

                if (e is CancellationException) {
                    throw e
                }
            }
        }
    }

    val updateConversation: (Conversation, Conversation) -> Unit = { conversation, updatedConversation ->
        conversation.apply {
            if (updatedConversation.id == id) {
                isAuto = updatedConversation.isAuto
                participant = updatedConversation.participant
                objIds = updatedConversation.objIds
                isRepliable = updatedConversation.isRepliable
                lastUserUpdate = updatedConversation.lastUserUpdate
                isInternal = updatedConversation.isInternal
                lastModUpdate = updatedConversation.lastModUpdate
                authors = updatedConversation.authors
                lastUpdated = updatedConversation.lastUpdated
                legacyFirstMessageId = updatedConversation.legacyFirstMessageId
                this.state = updatedConversation.state
                conversationType = updatedConversation.conversationType
                lastUnread = updatedConversation.lastUnread
                owner = updatedConversation.owner
                subject = updatedConversation.subject
                isHighlighted = updatedConversation.isHighlighted
                numMessages = updatedConversation.numMessages
                modMailItems = updatedConversation.modMailItems
                isUpdated = true
            }
        }
    }

    suspend fun sendMessage(conversation: Conversation, content: String, isAuthorHidden: Boolean, isInternal: Boolean) {
        conversation.id?.let { id ->
            Conversation.reply(
                oauthRetrofit, accessToken, id, content, isAuthorHidden, isInternal
            )
        }
    }

    suspend fun executeModConversationAction(
        conversation: Conversation,
        modConversationAction: ModConversationAction
    ) {
        conversation.id?.let { id ->
            Conversation.executeAction(
                oauthRetrofit, accessToken, id, modConversationAction
            )
        }
    }

    fun resetState() {
        _conversations.value = null
        _modMailUser.value = null
        _loadState.value = DataLoadState.Idle
        _isRefreshingConversations.value = false
        conversationsAfter = null
        conversationIds.clear()
        _conversationMessagesDataLoadState.value = DataLoadState.Idle
    }
}