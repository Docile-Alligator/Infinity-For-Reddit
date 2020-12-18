package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

public class PostFilter implements Parcelable {
    public int maxVote = -1;
    public int minVote = -1;
    public int maxComments = -1;
    public int minComments = -1;
    public int maxAwards = -1;
    public int minAwards = -1;
    public boolean allowNSFW;
    public boolean onlyNSFW;
    public boolean onlySpoiler;
    public String postTitleRegex;
    public String postTitleExcludesStrings;
    public String excludesSubreddits;
    public String excludesUsers;
    public String containsFlairs;
    public String excludesFlairs;
    public boolean containsTextType;
    public boolean containsLinkType;
    public boolean containsImageType;
    public boolean containsVideoType;

    public PostFilter() {

    }

    protected PostFilter(Parcel in) {
        maxVote = in.readInt();
        minVote = in.readInt();
        maxComments = in.readInt();
        minComments = in.readInt();
        maxAwards = in.readInt();
        minAwards = in.readInt();
        allowNSFW = in.readByte() != 0;
        onlyNSFW = in.readByte() != 0;
        onlySpoiler = in.readByte() != 0;
        postTitleRegex = in.readString();
        postTitleExcludesStrings = in.readString();
        excludesSubreddits = in.readString();
        excludesUsers = in.readString();
        containsFlairs = in.readString();
        excludesFlairs = in.readString();
        containsTextType = in.readByte() != 0;
        containsLinkType = in.readByte() != 0;
        containsImageType = in.readByte() != 0;
        containsVideoType = in.readByte() != 0;
    }

    public static final Creator<PostFilter> CREATOR = new Creator<PostFilter>() {
        @Override
        public PostFilter createFromParcel(Parcel in) {
            return new PostFilter(in);
        }

        @Override
        public PostFilter[] newArray(int size) {
            return new PostFilter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(maxVote);
        parcel.writeInt(minVote);
        parcel.writeInt(maxComments);
        parcel.writeInt(minComments);
        parcel.writeInt(maxAwards);
        parcel.writeInt(minAwards);
        parcel.writeByte((byte) (allowNSFW ? 1 : 0));
        parcel.writeByte((byte) (onlyNSFW ? 1 : 0));
        parcel.writeByte((byte) (onlySpoiler ? 1 : 0));
        parcel.writeString(postTitleRegex);
        parcel.writeString(postTitleExcludesStrings);
        parcel.writeString(excludesSubreddits);
        parcel.writeString(excludesUsers);
        parcel.writeString(containsFlairs);
        parcel.writeString(excludesFlairs);
        parcel.writeByte((byte) (containsTextType ? 1 : 0));
        parcel.writeByte((byte) (containsLinkType ? 1 : 0));
        parcel.writeByte((byte) (containsImageType ? 1 : 0));
        parcel.writeByte((byte) (containsVideoType ? 1 : 0));
    }
}
