package ml.docilealligator.infinityforreddit

sealed class LiveDataState<out T> {
    object Uninitialized: LiveDataState<Nothing>()
    data class Value<T>(val data: T?): LiveDataState<T>()
}