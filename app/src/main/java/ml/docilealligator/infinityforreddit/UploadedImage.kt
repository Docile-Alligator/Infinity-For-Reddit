package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

public class UploadedImage implements Parcelable {
    public String imageName;
    public String imageUrl;

    public UploadedImage(String imageName, String imageUrl) {
        this.imageName = imageName;
        this.imageUrl = imageUrl;
    }

    protected UploadedImage(Parcel in) {
        imageName = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<UploadedImage> CREATOR = new Creator<UploadedImage>() {
        @Override
        public UploadedImage createFromParcel(Parcel in) {
            return new UploadedImage(in);
        }

        @Override
        public UploadedImage[] newArray(int size) {
            return new UploadedImage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imageName);
        parcel.writeString(imageUrl);
    }
}
