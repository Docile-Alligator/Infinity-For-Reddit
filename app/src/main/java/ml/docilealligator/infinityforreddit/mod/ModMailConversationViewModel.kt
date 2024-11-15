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
    private val pagingSource: ModMailConversationPagingSource =
        ModMailConversationPagingSource(oauthRetrofit, accessToken, sharedPreferences)

    val flow = Pager(
        PagingConfig(20)
    ) {
        pagingSource
    }.flow.cachedIn(viewModelScope)

    fun refresh() {
        pagingSource.invalidate()
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