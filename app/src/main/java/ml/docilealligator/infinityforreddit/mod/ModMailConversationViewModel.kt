package ml.docilealligator.infinityforreddit.mod

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import retrofit2.Retrofit

class ModMailConversationViewModel(
    oauthRetrofit: Retrofit,
    accessToken: String,
    sharedPreferences: SharedPreferences
) : ViewModel() {
    val recentFlow = Pager(
        PagingConfig(20, 4)
    ) {
        ModMailConversationPagingSource(oauthRetrofit, accessToken, sharedPreferences, "recent")
    }
        .flow
        .cachedIn(viewModelScope)

    val modFlow = Pager(
        PagingConfig(20, 4)
    ) {
        ModMailConversationPagingSource(oauthRetrofit, accessToken, sharedPreferences, "mod")
    }
        .flow
        .cachedIn(viewModelScope)

    val userFlow = Pager(
        PagingConfig(20, 4)
    ) {
        ModMailConversationPagingSource(oauthRetrofit, accessToken, sharedPreferences, "user")
    }
        .flow
        .cachedIn(viewModelScope)

    val unreadFlow = Pager(
        PagingConfig(20, 4)
    ) {
        ModMailConversationPagingSource(oauthRetrofit, accessToken, sharedPreferences, "unread")
    }
        .flow
        .cachedIn(viewModelScope)

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