package ml.docilealligator.infinityforreddit.managers

class VideoMuteManager(
    var isMuted: Boolean,
    var rememberMuteOption: Boolean
) {
    fun getMasterMutingOption(): Boolean? {
        return if (rememberMuteOption) isMuted else null
    }
}