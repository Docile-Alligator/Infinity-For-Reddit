package ml.docilealligator.infinityforreddit.viewmodels

import androidx.core.graphics.Insets
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewGalleryViewModel: ViewModel() {
    private val _insets = MutableLiveData<Insets>()
    val insets: LiveData<Insets> = _insets

    fun setInsets(insets: Insets) {
        _insets.postValue(insets);
    }
}