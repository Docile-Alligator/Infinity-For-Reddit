package ml.docilealligator.infinityforreddit

sealed interface ActionState {
    object Idle: ActionState
    object Running: ActionState
    data class Success<T>(val data: T): ActionState
    data class Error(val message: String): ActionState
}