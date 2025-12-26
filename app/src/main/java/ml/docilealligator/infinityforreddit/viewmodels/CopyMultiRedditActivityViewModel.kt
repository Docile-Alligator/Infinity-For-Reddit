package ml.docilealligator.infinityforreddit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.APIError
import ml.docilealligator.infinityforreddit.APIResult
import ml.docilealligator.infinityforreddit.ActionState
import ml.docilealligator.infinityforreddit.ActionStateError
import ml.docilealligator.infinityforreddit.DataLoadState
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit
import ml.docilealligator.infinityforreddit.repositories.CopyMultiRedditActivityRepository

class CopyMultiRedditActivityViewModel(
    val multipath: String,
    val copyMultiRedditActivityRepository: CopyMultiRedditActivityRepository
): ViewModel() {
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _multiRedditState = MutableStateFlow<DataLoadState<MultiReddit>>(DataLoadState.Loading)
    val multiRedditState: StateFlow<DataLoadState<MultiReddit>> = _multiRedditState.asStateFlow()

    private val  _copyMultiRedditState = MutableStateFlow<ActionState>(ActionState.Idle)
    val copyMultiRedditState: StateFlow<ActionState> = _copyMultiRedditState.asStateFlow()

    fun setName(name: String) {
        _name.value = name
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun fetchMultiRedditInfo() {
        if (_multiRedditState.value is DataLoadState.Success) {
            return
        }

        _multiRedditState.value = DataLoadState.Loading

        viewModelScope.launch {
            val multiReddit = copyMultiRedditActivityRepository.fetchMultiRedditInfo(multipath)
            _multiRedditState.value = multiReddit?.let {
                _name.value = it.name
                _description.value = it.description

                DataLoadState.Success(it)
            } ?: DataLoadState.Error("Failed to load multiReddit")
        }
    }

    fun copyMultiRedditInfo() {
        if (_copyMultiRedditState.value == ActionState.Running) {
            return
        }

        _copyMultiRedditState.value = ActionState.Running

        viewModelScope.launch {
            when (val result = copyMultiRedditActivityRepository.copyMultiReddit(multipath, _name.value, _description.value, (multiRedditState.value as DataLoadState.Success).data.subreddits)) {
                is APIResult.Success -> {
                    _copyMultiRedditState.value = ActionState.Success(result.data)
                }
                is APIResult.Error -> {
                    val error =result.error
                    when (error) {
                        is APIError.Message -> _copyMultiRedditState.value = ActionState.Error(
                            ActionStateError.Message(error.message)
                        )
                        is APIError.MessageRes -> _copyMultiRedditState.value = ActionState.Error(
                            ActionStateError.MessageRes(error.resId)
                        )
                    }
                }
            }
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