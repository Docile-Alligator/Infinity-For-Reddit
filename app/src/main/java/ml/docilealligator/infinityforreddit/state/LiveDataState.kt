package ml.docilealligator.infinityforreddit.state

sealed class LiveDataState<out T> {
    object Uninitialized: LiveDataState<Nothing>()
    data class Value<T>(val data: T?): LiveDataState<T>()
}