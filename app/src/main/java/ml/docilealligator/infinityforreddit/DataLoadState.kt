package ml.docilealligator.infinityforreddit

sealed class DataLoadState<out T> {
    object Loading: DataLoadState<Nothing>()
    data class Success<T>(val data: T): DataLoadState<T>()
    data class Error<T>(val message: String): DataLoadState<T>()
}