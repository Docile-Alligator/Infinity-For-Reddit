package ml.docilealligator.infinityforreddit.mod

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit

class ModMailConversationViewModel(
    oauthRetrofit: Retrofit,
    accessToken: String,
    sharedPreferences: SharedPreferences
) : ViewModel() {
    private val updatedConversations: MutableStateFlow<MutableMap<String, Conversation>> = MutableStateFlow(mutableMapOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    val flow = updatedConversations.flatMapLatest { updatedConversationsValue ->
        Pager(
            PagingConfig(20, 4)
        ) {
            ModMailConversationPagingSource(oauthRetrofit, accessToken, sharedPreferences)
        }.flow.map { pagingData ->
            pagingData.map { conversation ->
                if (updatedConversationsValue.containsKey(conversation.id)) updatedConversationsValue[conversation.id]!! else conversation
            }
        }
    }.cachedIn(viewModelScope)

    fun updateConversation(conversation: Conversation) {
        updatedConversations.value[conversation.id!!] = conversation
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