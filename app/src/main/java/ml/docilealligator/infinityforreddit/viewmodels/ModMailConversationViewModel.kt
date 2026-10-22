package ml.docilealligator.infinityforreddit.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.repositories.ModMailConversationRepository
import retrofit2.Retrofit

class ModMailConversationViewModel(
    private val oauthRetrofit: Retrofit,
    private val accessToken: String,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
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
            repository.initialLoad()
        }
    }

    fun refresh(repository: ModMailConversationRepository) {
        viewModelScope.launch {
            repository.refresh()
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