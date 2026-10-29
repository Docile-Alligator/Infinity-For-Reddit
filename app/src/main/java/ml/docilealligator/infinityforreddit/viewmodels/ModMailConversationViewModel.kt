package ml.docilealligator.infinityforreddit.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.moderation.modmail.Conversation
import ml.docilealligator.infinityforreddit.repositories.ModMailConversationRepository
import retrofit2.Retrofit

class ModMailConversationViewModel(
    private val oauthRetrofit: Retrofit,
    private val accessToken: String,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _currentState = MutableStateFlow(ModMailState.ALL)
    val currentState: StateFlow<ModMailState> = _currentState.asStateFlow()

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
            repository.initialLoadConversations(_currentState.value)
        }
    }

    fun loadNextPage(repository: ModMailConversationRepository) {
        viewModelScope.launch {
            repository.loadNextPageConversations(_currentState.value)
        }
    }

    fun refreshConversations(repository: ModMailConversationRepository) {
        viewModelScope.launch {
            repository.refreshConversations(_currentState.value)
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

    fun switchState(state: ModMailState) {
        if (_currentState.value != state) {
            recentModMailConversationRepository.resetState()
            modModMailConversationRepository.resetState()
            userModMailConversationRepository.resetState()
            unreadModMailConversationRepository.resetState()
            _currentState.value = state
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val oauthRetrofit: Retrofit,
        private val accessToken: String,
        private val sharedPreferences: SharedPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ModMailConversationViewModel(oauthRetrofit, accessToken, sharedPreferences) as T
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