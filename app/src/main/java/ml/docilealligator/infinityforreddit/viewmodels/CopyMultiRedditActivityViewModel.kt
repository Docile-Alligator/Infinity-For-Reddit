package ml.docilealligator.infinityforreddit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.DataLoadState
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit
import ml.docilealligator.infinityforreddit.repositories.CopyMultiRedditActivityRepository
import ml.docilealligator.infinityforreddit.repositories.EditCommentActivityRepository

class CopyMultiRedditActivityViewModel(
    val multipath: String,
    val copyMultiRedditActivityRepository: CopyMultiRedditActivityRepository
): ViewModel() {
    private val _multiRedditState = MutableStateFlow<DataLoadState<MultiReddit>>(DataLoadState.Loading)
    val multiRedditState: StateFlow<DataLoadState<MultiReddit>> = _multiRedditState.asStateFlow()

    fun fetchMultiRedditInfo() {
        viewModelScope.launch {
            _multiRedditState.value = DataLoadState.Loading
            val multiReddit = copyMultiRedditActivityRepository.fetchMultiRedditInfo(multipath)
            _multiRedditState.value = multiReddit?.let {
                DataLoadState.Success(it)
            } ?: DataLoadState.Error("Failed to load multiReddit")
        }
    }

    companion object {
        fun provideFactory(multipath: String, copyMultiRedditActivityRepository: CopyMultiRedditActivityRepository) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return CopyMultiRedditActivityViewModel(
                        multipath, copyMultiRedditActivityRepository
                    ) as T
                }
            }
        }
    }
}