package ml.docilealligator.infinityforreddit

import android.os.Parcel
import android.os.Parcelable

open class UploadedImage(
    var imageName: String?,
    var imageUrl: String?
    ) : Parcelable {
    protected constructor(parcel: Parcel) : this(
        parcel.readString(), parcel.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(imageName)
        parcel.writeString(imageUrl)
    }

    companion object CREATOR: Parcelable.Creator<UploadedImage?> {
        override fun createFromParcel(parcel: Parcel) =
            UploadedImage(parcel)
        override fun newArray(size: Int) =
            arrayOfNulls<UploadedImage?>(size)
    }
}
