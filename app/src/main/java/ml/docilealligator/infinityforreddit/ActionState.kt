package ml.docilealligator.infinityforreddit

import androidx.annotation.StringRes

sealed interface ActionState {
    object Idle: ActionState
    object Running: ActionState
    data class Success<T>(val data: T): ActionState
    data class Error(val error: ActionStateError): ActionState
}

sealed class ActionStateError {
    data class Message(val message: String) : ActionStateError()
    data class MessageRes(@StringRes val resId: Int) : ActionStateError()
}