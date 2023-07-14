package ml.docilealligator.infinityforreddit.subreddit;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;

public class SubredditWithSelection implements Parcelable {
    private String name;
    private String iconUrl;
    private boolean selected;

    public SubredditWithSelection(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
        selected = false;
    }

    protected SubredditWithSelection(Parcel in) {
        name = in.readString();
        iconUrl = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<SubredditWithSelection> CREATOR = new Creator<SubredditWithSelection>() {
        @Override
        public SubredditWithSelection createFromParcel(Parcel in) {
            return new SubredditWithSelection(in);
        }

        @Override
        public SubredditWithSelection[] newArray(int size) {
            return new SubredditWithSelection[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static ArrayList<SubredditWithSelection> convertSubscribedSubreddits(
            List<SubscribedSubredditData> subscribedSubredditData) {
        ArrayList<SubredditWithSelection> subredditWithSelections = new ArrayList<>();
        for (SubscribedSubredditData s : subscribedSubredditData) {
            subredditWithSelections.add(new SubredditWithSelection(s.getName(), s.getIconUrl()));
        }

        return subredditWithSelections;
    }

    public static SubredditWithSelection convertSubreddit(SubredditData subreddit) {
        return new SubredditWithSelection(subreddit.getName(), subreddit.getIconUrl());
    }

    public int compareName(SubredditWithSelection subredditWithSelection) {
        if (subredditWithSelection != null) {
            return name.compareToIgnoreCase(subredditWithSelection.getName());
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SubredditWithSelection)) {
            return false;
        } else {
            return this.getName().compareToIgnoreCase(((SubredditWithSelection) obj).getName()) == 0;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(iconUrl);
        parcel.writeByte((byte) (selected ? 1 : 0));
    }
}
