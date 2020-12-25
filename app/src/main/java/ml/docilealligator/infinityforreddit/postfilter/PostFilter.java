package ml.docilealligator.infinityforreddit.postfilter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.post.Post;

@Entity(tableName = "post_filter")
public class PostFilter implements Parcelable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    public String name = "New Filter";
    @ColumnInfo(name = "max_vote")
    public int maxVote = -1;
    @ColumnInfo(name = "min_vote")
    public int minVote = -1;
    @ColumnInfo(name = "max_comments")
    public int maxComments = -1;
    @ColumnInfo(name = "min_comments")
    public int minComments = -1;
    @ColumnInfo(name = "max_awards")
    public int maxAwards = -1;
    @ColumnInfo(name = "min_awards")
    public int minAwards = -1;
    @Ignore
    public boolean allowNSFW;
    @ColumnInfo(name = "only_nsfw")
    public boolean onlyNSFW;
    @ColumnInfo(name = "only_spoiler")
    public boolean onlySpoiler;
    @ColumnInfo(name = "post_title_excludes_regex")
    public String postTitleExcludesRegex;
    @ColumnInfo(name = "post_title_excludes_strings")
    public String postTitleExcludesStrings;
    @ColumnInfo(name = "exclude_subreddits")
    public String excludeSubreddits;
    @ColumnInfo(name = "exclude_users")
    public String excludeUsers;
    @ColumnInfo(name = "contain_flairs")
    public String containFlairs;
    @ColumnInfo(name = "exclude_flairs")
    public String excludeFlairs;
    @ColumnInfo(name = "contain_text_type")
    public boolean containTextType = true;
    @ColumnInfo(name = "contain_link_type")
    public boolean containLinkType = true;
    @ColumnInfo(name = "contain_image_type")
    public boolean containImageType = true;
    @ColumnInfo(name = "contain_gif_type")
    public boolean containGifType = true;
    @ColumnInfo(name = "contain_video_type")
    public boolean containVideoType = true;
    @ColumnInfo(name = "contain_gallery_type")
    public boolean containGalleryType = true;

    public PostFilter() {

    }

    public static boolean isPostAllowed(Post post, PostFilter postFilter) {
        if (postFilter == null || post == null) {
            return true;
        }
        if (post.isNSFW() && !postFilter.allowNSFW) {
            return false;
        }
        if (postFilter.maxVote > 0 && post.getVoteType() + post.getScore() > postFilter.maxVote) {
            return false;
        }
        if (postFilter.minVote > 0 && post.getVoteType() + post.getScore() < postFilter.minVote) {
            return false;
        }
        if (postFilter.maxComments > 0 && post.getNComments() > postFilter.maxComments) {
            return false;
        }
        if (postFilter.minComments > 0 && post.getNComments() < postFilter.minComments) {
            return false;
        }
        if (postFilter.maxAwards > 0 && post.getNAwards() > postFilter.maxAwards) {
            return false;
        }
        if (postFilter.minAwards > 0 && post.getNAwards() < postFilter.minAwards) {
            return false;
        }
        if (postFilter.onlyNSFW && !post.isNSFW()) {
            return false;
        }
        if (postFilter.onlySpoiler && !post.isSpoiler()) {
            return false;
        }
        if (!postFilter.containTextType && post.getPostType() == Post.TEXT_TYPE) {
            return false;
        }
        if (!postFilter.containLinkType && (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE)) {
            return false;
        }
        if (!postFilter.containImageType && post.getPostType() == Post.IMAGE_TYPE) {
            return false;
        }
        if (!postFilter.containGifType && post.getPostType() == Post.GIF_TYPE) {
            return false;
        }
        if (!postFilter.containVideoType && post.getPostType() == Post.VIDEO_TYPE) {
            return false;
        }
        if (!postFilter.containGalleryType && post.getPostType() == Post.GALLERY_TYPE) {
            return false;
        }
        if (postFilter.postTitleExcludesRegex != null && !postFilter.postTitleExcludesRegex.equals("")) {
            Pattern spoilerPattern = Pattern.compile(postFilter.postTitleExcludesRegex);
            Matcher matcher = spoilerPattern.matcher(postFilter.postTitleExcludesRegex);
            if (matcher.find()) {
                return false;
            }
        }
        if (postFilter.postTitleExcludesStrings != null && !postFilter.postTitleExcludesStrings.equals("")) {
            String[] titles = postFilter.postTitleExcludesStrings.split(",", 0);
            for (String t : titles) {
                if (post.getTitle().contains(t)) {
                    return false;
                }
            }
        }
        if (postFilter.excludeSubreddits != null && !postFilter.excludeSubreddits.equals("")) {
            String[] subreddits = postFilter.excludeSubreddits.split(",", 0);
            for (String s : subreddits) {
                if (post.getSubredditName().equalsIgnoreCase(s)) {
                    return false;
                }
            }
        }
        if (postFilter.excludeUsers != null && !postFilter.excludeUsers.equals("")) {
            String[] users = postFilter.excludeUsers.split(",", 0);
            for (String u : users) {
                if (post.getAuthor().equalsIgnoreCase(u)) {
                    return false;
                }
            }
        }
        if (postFilter.excludeFlairs != null && !postFilter.excludeFlairs.equals("")) {
            String[] flairs = postFilter.excludeFlairs.split(",", 0);
            for (String f : flairs) {
                if (post.getFlair().equalsIgnoreCase(f)) {
                    return false;
                }
            }
        }
        if (postFilter.containFlairs != null && !postFilter.containFlairs.equals("")) {
            String[] flairs = postFilter.containFlairs.split(",", 0);
            for (String f : flairs) {
                if (post.getFlair().equalsIgnoreCase(f)) {
                    return false;
                }
            }
        }

        return true;
    }

    protected PostFilter(Parcel in) {
        name = in.readString();
        maxVote = in.readInt();
        minVote = in.readInt();
        maxComments = in.readInt();
        minComments = in.readInt();
        maxAwards = in.readInt();
        minAwards = in.readInt();
        allowNSFW = in.readByte() != 0;
        onlyNSFW = in.readByte() != 0;
        onlySpoiler = in.readByte() != 0;
        postTitleExcludesRegex = in.readString();
        postTitleExcludesStrings = in.readString();
        excludeSubreddits = in.readString();
        excludeUsers = in.readString();
        containFlairs = in.readString();
        excludeFlairs = in.readString();
        containTextType = in.readByte() != 0;
        containLinkType = in.readByte() != 0;
        containImageType = in.readByte() != 0;
        containVideoType = in.readByte() != 0;
        containGalleryType = in.readByte() != 0;
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
        parcel.writeString(name);
        parcel.writeInt(maxVote);
        parcel.writeInt(minVote);
        parcel.writeInt(maxComments);
        parcel.writeInt(minComments);
        parcel.writeInt(maxAwards);
        parcel.writeInt(minAwards);
        parcel.writeByte((byte) (allowNSFW ? 1 : 0));
        parcel.writeByte((byte) (onlyNSFW ? 1 : 0));
        parcel.writeByte((byte) (onlySpoiler ? 1 : 0));
        parcel.writeString(postTitleExcludesRegex);
        parcel.writeString(postTitleExcludesStrings);
        parcel.writeString(excludeSubreddits);
        parcel.writeString(excludeUsers);
        parcel.writeString(containFlairs);
        parcel.writeString(excludeFlairs);
        parcel.writeByte((byte) (containTextType ? 1 : 0));
        parcel.writeByte((byte) (containLinkType ? 1 : 0));
        parcel.writeByte((byte) (containImageType ? 1 : 0));
        parcel.writeByte((byte) (containVideoType ? 1 : 0));
        parcel.writeByte((byte) (containGalleryType ? 1 : 0));
    }
}
