package ml.docilealligator.infinityforreddit

sealed class APIResult<out T> {
    data class Success<T>(val data: T): APIResult<T>()
    data class Error(val message: String): APIResult<Nothing>()
}