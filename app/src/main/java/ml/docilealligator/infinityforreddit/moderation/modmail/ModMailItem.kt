package ml.docilealligator.infinityforreddit.moderation.modmail

import android.os.Parcelable

sealed interface ModMailItem: Parcelable {
    val id: String?
}