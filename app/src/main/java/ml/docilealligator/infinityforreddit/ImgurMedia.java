package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

public class ImgurMedia implements Parcelable {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;
    private String id;
    private String title;
    private String description;
    private String link;
    private int type;

    public ImgurMedia(String id, String title, String description, String type, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        if (type.contains("mp4")) {
            this.type = TYPE_VIDEO;
        } else {
            this.type = TYPE_IMAGE;
        }
        this.link = link;
    }

    protected ImgurMedia(Parcel in) {
        title = in.readString();
        description = in.readString();
        link = in.readString();
    }

    public static final Creator<ImgurMedia> CREATOR = new Creator<ImgurMedia>() {
        @Override
        public ImgurMedia createFromParcel(Parcel in) {
            return new ImgurMedia(in);
        }

        @Override
        public ImgurMedia[] newArray(int size) {
            return new ImgurMedia[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public String getLink() {
        return link;
    }

    public String getFileName() {
        if (type == TYPE_VIDEO) {
            return "imgur-" + id + ".mp4";
        }

        return "imgur-" + id + ".jpg";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(link);
    }
}
