package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

public class UserFlair implements Parcelable {
    private String id;
    private String text;
    private String htmlText;
    private boolean editable;
    private int maxEmojis;

    public UserFlair(String id, String text, String htmlText, boolean editable, int maxEmojis) {
        this.id = id;
        this.text = text;
        this.htmlText = htmlText;
        this.editable = editable;
        this.maxEmojis = maxEmojis;
    }

    protected UserFlair(Parcel in) {
        id = in.readString();
        text = in.readString();
        htmlText = in.readString();
        editable = in.readByte() != 0;
        maxEmojis = in.readInt();
    }

    public static final Creator<UserFlair> CREATOR = new Creator<UserFlair>() {
        @Override
        public UserFlair createFromParcel(Parcel in) {
            return new UserFlair(in);
        }

        @Override
        public UserFlair[] newArray(int size) {
            return new UserFlair[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public boolean isEditable() {
        return editable;
    }

    public int getMaxEmojis() {
        return maxEmojis;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(text);
        parcel.writeString(htmlText);
        parcel.writeByte((byte) (editable ? 1 : 0));
        parcel.writeInt(maxEmojis);
    }
}
