package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.post.Post;

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
    public String postTitleExcludesRegex;
    public String postTitleExcludesStrings;
    public String excludesSubreddits;
    public String excludesUsers;
    public String containsFlairs;
    public String excludesFlairs;
    public boolean containsTextType = true;
    public boolean containsLinkType = true;
    public boolean containsImageType = true;
    public boolean containsGifType = true;
    public boolean containsVideoType = true;
    public boolean containsGalleryType = true;

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
        if (!postFilter.containsTextType && post.getPostType() == Post.TEXT_TYPE) {
            return false;
        }
        if (!postFilter.containsLinkType && (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE)) {
            return false;
        }
        if (!postFilter.containsImageType && post.getPostType() == Post.IMAGE_TYPE) {
            return false;
        }
        if (!postFilter.containsGifType && post.getPostType() == Post.GIF_TYPE) {
            return false;
        }
        if (!postFilter.containsVideoType && post.getPostType() == Post.VIDEO_TYPE) {
            return false;
        }
        if (!postFilter.containsGalleryType && post.getPostType() == Post.GALLERY_TYPE) {
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
        if (postFilter.excludesSubreddits != null && !postFilter.excludesSubreddits.equals("")) {
            String[] subreddits = postFilter.excludesSubreddits.split(",", 0);
            for (String s : subreddits) {
                if (post.getSubredditName().equalsIgnoreCase(s)) {
                    return false;
                }
            }
        }
        if (postFilter.excludesUsers != null && !postFilter.excludesUsers.equals("")) {
            String[] users = postFilter.excludesUsers.split(",", 0);
            for (String u : users) {
                if (post.getAuthor().equalsIgnoreCase(u)) {
                    return false;
                }
            }
        }
        if (postFilter.excludesFlairs != null && !postFilter.excludesFlairs.equals("")) {
            String[] flairs = postFilter.excludesFlairs.split(",", 0);
            for (String f : flairs) {
                if (post.getFlair().equalsIgnoreCase(f)) {
                    return false;
                }
            }
        }
        if (postFilter.containsFlairs != null && !postFilter.containsFlairs.equals("")) {
            String[] flairs = postFilter.containsFlairs.split(",", 0);
            for (String f : flairs) {
                if (post.getFlair().equalsIgnoreCase(f)) {
                    return false;
                }
            }
        }

        return true;
    }

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
        postTitleExcludesRegex = in.readString();
        postTitleExcludesStrings = in.readString();
        excludesSubreddits = in.readString();
        excludesUsers = in.readString();
        containsFlairs = in.readString();
        excludesFlairs = in.readString();
        containsTextType = in.readByte() != 0;
        containsLinkType = in.readByte() != 0;
        containsImageType = in.readByte() != 0;
        containsVideoType = in.readByte() != 0;
        containsGalleryType = in.readByte() != 0;
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
        parcel.writeString(postTitleExcludesRegex);
        parcel.writeString(postTitleExcludesStrings);
        parcel.writeString(excludesSubreddits);
        parcel.writeString(excludesUsers);
        parcel.writeString(containsFlairs);
        parcel.writeString(excludesFlairs);
        parcel.writeByte((byte) (containsTextType ? 1 : 0));
        parcel.writeByte((byte) (containsLinkType ? 1 : 0));
        parcel.writeByte((byte) (containsImageType ? 1 : 0));
        parcel.writeByte((byte) (containsVideoType ? 1 : 0));
        parcel.writeByte((byte) (containsGalleryType ? 1 : 0));
    }
}
