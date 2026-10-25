package ml.docilealligator.infinityforreddit.repositories

import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.state.DataLoadState
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.moderation.modmail.Conversation
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ModMailConversationRepository(
    private val oauthRetrofit: Retrofit,
    private val accessToken: String,
    private val sharedPreferences: SharedPreferences,
    private val sort: String
) {
    private val _conversations = MutableStateFlow<List<Conversation>?>(null)
    val conversations = _conversations.asStateFlow()

    // Conversations are saved in _conversations
    private val _loadState: MutableStateFlow<DataLoadState<Unit>> = MutableStateFlow(DataLoadState.Idle)
    val loadState = _loadState.asStateFlow()

    private val _isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var after: String? = null

    private var conversationIds = mutableSetOf<String>()

    suspend fun initialLoad() {
        if (_loadState.value == DataLoadState.Idle) {
            load()
        }
    }

    suspend fun refresh() {
        _isRefreshing.value = true
        conversationIds.clear()
        after = null
        load()
        _isRefreshing.value = false
    }

    suspend fun loadNextPage() {
        if (_conversations.value.isNullOrEmpty() || after.isNullOrEmpty()) {
            return
        }

        if (_loadState.value != DataLoadState.Loading) {
            load()
        }
    }

    private suspend fun load() {
        try {
            _loadState.value = DataLoadState.Loading
            val response = oauthRetrofit.create(RedditAPIKt::class.java)
                .getModMailConversations(APIUtils.getOAuthHeader(accessToken), sort, after)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val conversations: MutableList<Conversation>? = parseConversations(body)
                    conversations?.let {
                        val deduplicatedValue = it.filter { conversation ->
                            conversation.id?.let { id ->
                                conversationIds.add(id)
                            } ?: false
                        }
                        if (_isRefreshing.value) {
                            _conversations.value = deduplicatedValue
                        } else {
                            val currentValue = _conversations.value
                            if (currentValue == null) {
                                _conversations.value = deduplicatedValue
                            } else {
                                _conversations.value = currentValue + deduplicatedValue
                            }
                        }
                        after = if (deduplicatedValue.isEmpty()) null else "ModmailConversation_" + deduplicatedValue[deduplicatedValue.size - 1].id
                    } ?: run {
                        after = null
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

                val messagesJSONObject = json.getJSONObject(JSONUtils.MESSAGES_KEY)
                for (i in 0 until conversationIdsArray.length()) {
                    val conversationId = conversationIdsArray.getString(i)
                    try {
                        conversations.add(Conversation.parseConversation(gson, json.getJSONObject(JSONUtils.CONVERSATIONS_KEY).getString(conversationId), messagesJSONObject))
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
}