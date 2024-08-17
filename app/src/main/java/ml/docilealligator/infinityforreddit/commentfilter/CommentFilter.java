package ml.docilealligator.infinityforreddit.commentfilter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import ml.docilealligator.infinityforreddit.comment.Comment;

@Entity(tableName = "comment_filter")
public class CommentFilter implements Parcelable {
    @PrimaryKey
    @NonNull
    public String name = "New Filter";
    @DisplayMode
    @ColumnInfo(name = "display_mode")
    public int displayMode;
    @ColumnInfo(name = "max_vote")
    public int maxVote = -1;
    @ColumnInfo(name = "min_vote")
    public int minVote = -1;
    @ColumnInfo(name = "exclude_strings")
    public String excludeStrings;
    @ColumnInfo(name = "exclude_users")
    public String excludeUsers;

    public CommentFilter() {

    }

    protected CommentFilter(Parcel in) {
        name = in.readString();
        displayMode = in.readInt();
        maxVote = in.readInt();
        minVote = in.readInt();
        excludeStrings = in.readString();
        excludeUsers = in.readString();
    }

    public static final Creator<CommentFilter> CREATOR = new Creator<CommentFilter>() {
        @Override
        public CommentFilter createFromParcel(Parcel in) {
            return new CommentFilter(in);
        }

        @Override
        public CommentFilter[] newArray(int size) {
            return new CommentFilter[size];
        }
    };

    public static boolean isCommentAllowed(Comment comment, CommentFilter commentFilter) {
        if (commentFilter.maxVote > 0 && comment.getVoteType() + comment.getScore() > commentFilter.maxVote) {
            return false;
        }
        if (commentFilter.minVote > 0 && comment.getVoteType() + comment.getScore() < commentFilter.minVote) {
            return false;
        }
        if (commentFilter.excludeStrings != null && !commentFilter.excludeStrings.equals("")) {
            String[] titles = commentFilter.excludeStrings.split(",", 0);
            for (String t : titles) {
                if (!t.trim().equals("") && comment.getCommentRawText().toLowerCase().contains(t.toLowerCase().trim())) {
                    return false;
                }
            }
        }
        if (commentFilter.excludeUsers != null && !commentFilter.excludeUsers.equals("")) {
            String[] users = commentFilter.excludeUsers.split(",", 0);
            for (String u : users) {
                if (!u.trim().equals("") && comment.getAuthor().equalsIgnoreCase(u.trim())) {
                    return false;
                }
            }
        }

        return true;
    }

    public static CommentFilter mergeCommentFilter(List<CommentFilter> commentFilterList) {
        if (commentFilterList.size() == 1) {
            return commentFilterList.get(0);
        }
        CommentFilter commentFilter = new CommentFilter();
        StringBuilder stringBuilder;
        commentFilter.name = "Merged";

        for (CommentFilter c : commentFilterList) {
            commentFilter.displayMode = Math.max(c.displayMode, commentFilter.displayMode);
            commentFilter.maxVote = Math.min(c.maxVote, commentFilter.maxVote);
            commentFilter.minVote = Math.max(c.minVote, commentFilter.minVote);

            if (c.excludeStrings != null && !c.excludeStrings.isEmpty()) {
                stringBuilder = new StringBuilder(commentFilter.excludeStrings == null ? "" : commentFilter.excludeStrings);
                stringBuilder.append(",").append(c.excludeStrings);
                commentFilter.excludeStrings = stringBuilder.toString();
            }

            if (c.excludeUsers != null && !c.excludeUsers.isEmpty()) {
                stringBuilder = new StringBuilder(commentFilter.excludeUsers == null ? "" : commentFilter.excludeUsers);
                stringBuilder.append(",").append(c.excludeUsers);
                commentFilter.excludeUsers = stringBuilder.toString();
            }
        }

        return commentFilter;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(displayMode);
        dest.writeInt(maxVote);
        dest.writeInt(minVote);
        dest.writeString(excludeStrings);
        dest.writeString(excludeUsers);
    }

    @IntDef({DisplayMode.REMOVE_COMMENT, DisplayMode.COLLAPSE_COMMENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DisplayMode {
        int REMOVE_COMMENT = 0;
        int COLLAPSE_COMMENT = 10;
    }
}
