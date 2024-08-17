package ml.docilealligator.infinityforreddit.customtheme;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

public class OnlineCustomThemeMetadata implements Parcelable {
    public int id;
    public String name;
    public String username;
    @SerializedName("primary_color")
    public String colorPrimary;

    protected OnlineCustomThemeMetadata(Parcel in) {
        id = in.readInt();
        name = in.readString();
        username = in.readString();
        colorPrimary = in.readString();
    }

    public static final Creator<OnlineCustomThemeMetadata> CREATOR = new Creator<>() {
        @Override
        public OnlineCustomThemeMetadata createFromParcel(Parcel in) {
            return new OnlineCustomThemeMetadata(in);
        }

        @Override
        public OnlineCustomThemeMetadata[] newArray(int size) {
            return new OnlineCustomThemeMetadata[size];
        }
    };

    public static OnlineCustomThemeMetadata fromJson(String json) throws JsonParseException {
        Gson gson = new Gson();
        return gson.fromJson(json, OnlineCustomThemeMetadata.class);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(colorPrimary);
    }
}
