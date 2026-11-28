package ml.docilealligator.infinityforreddit.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.database.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.moderation.modmail.Conversation
import ml.docilealligator.infinityforreddit.repositories.ModMailConversationRepository
import ml.docilealligator.infinityforreddit.result.AppResult
import ml.docilealligator.infinityforreddit.subreddit.SubredditData
import ml.docilealligator.infinityforreddit.subreddit.fetchAllSubredditListingData
import ml.docilealligator.infinityforreddit.user.UserData
import ml.docilealligator.infinityforreddit.user.fetchUserData
import retrofit2.Retrofit

class ModMailConversationViewModel(
    private val redditRoomDatabase: RedditDataRoomDatabase,
    private val oauthRetrofit: Retrofit,
    private val accessToken: String,
    private val accountName: String,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _currentState = MutableStateFlow(ModMailState.ALL)
    val currentState: StateFlow<ModMailState> = _currentState.asStateFlow()

    private val _commaDelimitedSubreddits = MutableStateFlow("")
    val commaDelimitedSubreddits = _commaDelimitedSubreddits.asStateFlow()

    private val _userDataMap = MutableStateFlow<Map<String, UserData>>(emptyMap())
    val userDataMap = _userDataMap.asStateFlow()

    private val _moderatedSubreddits = MutableStateFlow<List<SubredditData>?>(null)
    val moderatedSubreddits = _moderatedSubreddits.asStateFlow()

    private val _unselectedSubreddits = MutableStateFlow<Set<String>>(mutableSetOf())
    val unselectedSubreddits = _unselectedSubreddits.asStateFlow()

    private val _allSubredditsSelected = MutableStateFlow(true)
    val allSubredditsSelected = _allSubredditsSelected.asStateFlow()

    private val _isLoadingModeratedSubreddits = MutableStateFlow(false)
    val isLoadingModeratedSubreddits = _isLoadingModeratedSubreddits.asStateFlow()

    val recentModMailConversationRepository = ModMailConversationRepository(
        oauthRetrofit, accessToken, sharedPreferences, "recent"
    )

    val modModMailConversationRepository = ModMailConversationRepository(
        oauthRetrofit, accessToken, sharedPreferences, "mod"
    )

    val userModMailConversationRepository = ModMailConversationRepository(
        oauthRetrofit, accessToken, sharedPreferences, "user"
    )

    val unreadModMailConversationRepository = ModMailConversationRepository(
        oauthRetrofit, accessToken, sharedPreferences, "unread"
    )

    fun initialLoad(repository: ModMailConversationRepository) {
        viewModelScope.launch {
            repository.initialLoadConversations(_commaDelimitedSubreddits.value, _currentState.value)
        }
    }

    fun loadNextPage(repository: ModMailConversationRepository) {
        viewModelScope.launch {
            repository.loadNextPageConversations(_commaDelimitedSubreddits.value, _currentState.value)
        }
    }

    fun refreshConversations(repository: ModMailConversationRepository) {
        viewModelScope.launch {
            repository.refreshConversations(_commaDelimitedSubreddits.value, _currentState.value)
        }
    }

    suspend fun refreshMessagesInConversation(conversation: Conversation, repository: ModMailConversationRepository) {
        repository.refreshMessagesInConversation(conversation)
    }

    suspend fun sendMessage(
        conversation: Conversation,
        content: String,
        isAuthorHidden: Boolean,
        isInternal: Boolean,
        repository: ModMailConversationRepository
    ) {
        repository.sendMessage(conversation, content, isAuthorHidden, isInternal)
    }

    suspend fun executeModConversationAction(
        conversation: Conversation,
        modConversationAction: ModConversationAction,
        repository: ModMailConversationRepository
    ) {
        repository.executeModConversationAction(conversation, modConversationAction)
    }

    fun readConversation(
        conversation: Conversation,
        repository: ModMailConversationRepository
    ) {
        viewModelScope.launch {
            try {
                executeModConversationAction(conversation, ModConversationAction.READ, repository)
                conversation.isRead = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun switchState(state: ModMailState) {
        if (_currentState.value != state) {
            resetState()
            _currentState.value = state
        }
    }

    fun changeSubreddits() {
        val newCommaDelimitedSubreddits = getCommaDelimitedSubreddits()
        if (_commaDelimitedSubreddits.value != newCommaDelimitedSubreddits) {
            resetState()
            _commaDelimitedSubreddits.value = newCommaDelimitedSubreddits
        }
    }

    private fun resetState() {
        recentModMailConversationRepository.resetState()
        modModMailConversationRepository.resetState()
        userModMailConversationRepository.resetState()
        unreadModMailConversationRepository.resetState()
    }

    // Return true if modMailUser is updated
    suspend fun executeUserAction(
        conversation: Conversation,
        modUserAction: ModUserAction,
        repository: ModMailConversationRepository
    ): Boolean {
        return repository.executeUserAction(conversation, modUserAction)
    }

    fun loadUserData(userName: String) {
        val userDataInMap = _userDataMap.value[userName]
        if (userDataInMap != null) {
            return
        }

        viewModelScope.launch {
            when (val result = fetchUserData(redditRoomDatabase, oauthRetrofit, accessToken, userName)) {
                is AppResult.Success<UserData> -> saveUserData(userName, result.data)
                is AppResult.Error<*> -> {}
            }
        }
    }

    fun getUserDataFlow(username: String): Flow<UserData?> {
        return userDataMap
            .map { it[username]}
            .distinctUntilChanged()
    }

    private fun saveUserData(userName: String, userData: UserData) {
        _userDataMap.update {
            it + (userName to userData)
        }
    }

    fun loadModeratedSubreddits() {
        if (!_isLoadingModeratedSubreddits.value) {
            viewModelScope.launch {
                _isLoadingModeratedSubreddits.value = true

                try {
                    when (
                        val result = fetchAllSubredditListingData(
                            retrofit = null,
                            oauthRetrofit = oauthRetrofit,
                            accessToken = accessToken,
                            accountName = accountName,
                            nsfw = true
                        )
                    ) {
                        is AppResult.Success<List<SubredditData>> -> {
                            _moderatedSubreddits.value = result.data
                        }

                        is AppResult.Error<*> -> {

                        }
                    }

                    _isLoadingModeratedSubreddits.value = false
                } catch (e: CancellationException) {
                    _isLoadingModeratedSubreddits.value = false
                    throw e
                }
            }
        }
    }

    fun setAllSubredditsSelected(allSubredditsSelected: Boolean) {
        _allSubredditsSelected.value = allSubredditsSelected
    }

    fun toggleSubreddit(subredditId: String) {
        if (_unselectedSubreddits.value.contains(subredditId)) {
            _unselectedSubreddits.update {
                it - subredditId
            }
        } else {
            _unselectedSubreddits.update {
                it + subredditId
            }
        }
    }

    fun clearUnSelectedSubreddits() {
        _unselectedSubreddits.value = emptySet()
    }

    private fun getCommaDelimitedSubreddits(): String {
        if (_allSubredditsSelected.value) {
            return ""
        }

        var result = ""
        _moderatedSubreddits.value?.filter {
            !_unselectedSubreddits.value.contains(it.id)
        }?.forEach {
            result = result + "," + it.name
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val redditRoomDatabase: RedditDataRoomDatabase,
        private val oauthRetrofit: Retrofit,
        private val accessToken: String,
        private val accountName: String,
        private val sharedPreferences: SharedPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ModMailConversationViewModel(redditRoomDatabase, oauthRetrofit, accessToken, accountName, sharedPreferences) as T
        }
    }
}

enum class ModMailState(
    val rawValue: String,
    val title: String
) {
    ALL("all", "All"),
    APPEALS("appeals", "Appeals"),
    NOTIFICATIONS("notifications", "Notifications"),
    INBOX("inbox", "Inbox"),
    FILTERED("filtered", "Filtered"),
    IN_PROGRESS("inprogress", "In progress"),
    MOD("mod", "Mod"),
    ARCHIVED("archived", "Archived"),
    DEFAULT("default", "Default"),
    HIGHLIGHTED("highlighted", "Highlighted"),
    ADMIN("admin", "Admin"),
    JOIN_REQUESTS("join_requests", "Join requests"),
    NEW("new", "New");

    companion object {
        fun fromString(value: String?): ModMailState {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

enum class ModConversationAction(
    val rawValue: String
) {
    ARCHIVE("archive"),
    UNARCHIVE("unarchive"),
    HIGHLIGHT("highlight"),
    // Unhighlight is DELETE, not POST
    UNHIGHLIGHT("highlight"),
    READ("read"),
    UNREAD("unread")
}

sealed class ModUserAction(
    val rawValue: String
) {
    object APPROVE : ModUserAction("approve")
    object DISAPPROVE : ModUserAction("disapprove")
    // nHours: one of (72, 168, 672)
    data class MUTE(val nHours: Int) : ModUserAction("mute")
    object UNMUTE : ModUserAction("unmute")
    // duration: an integer between 1 and 999
    data class TEMPBAN(val subreddit: String, val username: String, val duration: Int,
                       val message: String, val reason: String, val modNote: String) : ModUserAction("temp_ban")
    object UNBAN : ModUserAction("unban")
}