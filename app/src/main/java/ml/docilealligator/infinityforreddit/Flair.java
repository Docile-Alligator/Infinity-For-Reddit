package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

public class Flair implements Parcelable {
    public static final Creator<Flair> CREATOR = new Creator<Flair>() {
        @Override
        public Flair createFromParcel(Parcel in) {
            return new Flair(in);
        }

        @Override
        public Flair[] newArray(int size) {
            return new Flair[size];
        }
    };
    private String id;
    private String text;
    private boolean editable;

    Flair(String id, String text, boolean editable) {
        this.id = id;
        this.text = text;
        this.editable = editable;
    }

    protected Flair(Parcel in) {
        id = in.readString();
        text = in.readString();
        editable = in.readByte() != 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(text);
        parcel.writeByte((byte) (editable ? 1 : 0));
    }
}
