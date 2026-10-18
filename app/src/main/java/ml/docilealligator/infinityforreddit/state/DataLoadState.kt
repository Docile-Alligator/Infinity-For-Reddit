package ml.docilealligator.infinityforreddit.state

sealed class DataLoadState<out T> {
    object Idle: DataLoadState<Nothing>()
    object Loading: DataLoadState<Nothing>()
    data class Success<T>(val data: T): DataLoadState<T>()
    data class Error<T>(val message: String): DataLoadState<T>()
}