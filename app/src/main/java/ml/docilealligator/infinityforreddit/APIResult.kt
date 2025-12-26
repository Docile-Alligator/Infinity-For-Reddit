package ml.docilealligator.infinityforreddit

import androidx.annotation.StringRes

sealed class APIResult<out T> {
    data class Success<T>(val data: T): APIResult<T>()
    data class Error(val error: APIError): APIResult<Nothing>()
}

sealed class APIError {
    data class Message(val message: String) : APIError()
    data class MessageRes(@StringRes val resId: Int) : APIError()
}