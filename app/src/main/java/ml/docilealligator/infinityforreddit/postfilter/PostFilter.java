package ml.docilealligator.infinityforreddit.postfilter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
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
    @ColumnInfo(name = "post_title_contains_regex")
    public String postTitleContainsRegex;
    @ColumnInfo(name = "post_title_excludes_strings")
    public String postTitleExcludesStrings;
    @ColumnInfo(name = "post_title_contains_strings")
    public String postTitleContainsStrings;
    @ColumnInfo(name = "exclude_subreddits")
    public String excludeSubreddits;
    @ColumnInfo(name = "exclude_users")
    public String excludeUsers;
    @ColumnInfo(name = "contain_flairs")
    public String containFlairs;
    @ColumnInfo(name = "exclude_flairs")
    public String excludeFlairs;
    @ColumnInfo(name =  "exclude_domains")
    public String excludeDomains;
    @ColumnInfo(name =  "contain_domains")
    public String containDomains;
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
        postTitleContainsRegex = in.readString();
        postTitleExcludesStrings = in.readString();
        postTitleContainsStrings = in.readString();
        excludeSubreddits = in.readString();
        excludeUsers = in.readString();
        containFlairs = in.readString();
        excludeFlairs = in.readString();
        excludeDomains = in.readString();
        containDomains = in.readString();
        containTextType = in.readByte() != 0;
        containLinkType = in.readByte() != 0;
        containImageType = in.readByte() != 0;
        containGifType = in.readByte() != 0;
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
            if (postFilter.onlySpoiler) {
                return post.isSpoiler();
            }
            return false;
        }
        if (postFilter.onlySpoiler && !post.isSpoiler()) {
            if (postFilter.onlyNSFW) {
                return post.isNSFW();
            }
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
            Pattern pattern = Pattern.compile(postFilter.postTitleExcludesRegex);
            Matcher matcher = pattern.matcher(post.getTitle());
            if (matcher.find()) {
                return false;
            }
        }
        if (postFilter.postTitleContainsRegex != null && !postFilter.postTitleContainsRegex.equals("")) {
            Pattern pattern = Pattern.compile(postFilter.postTitleContainsRegex);
            Matcher matcher = pattern.matcher(post.getTitle());
            if (!matcher.find()) {
                return false;
            }
        }
        if (postFilter.postTitleExcludesStrings != null && !postFilter.postTitleExcludesStrings.equals("")) {
            String[] titles = postFilter.postTitleExcludesStrings.split(",", 0);
            for (String t : titles) {
                if (!t.trim().equals("") && post.getTitle().toLowerCase().contains(t.toLowerCase().trim())) {
                    return false;
                }
            }
        }
        if (postFilter.postTitleContainsStrings != null && !postFilter.postTitleContainsStrings.equals("")) {
            String[] titles = postFilter.postTitleContainsStrings.split(",", 0);
            boolean hasRequiredString = false;
            for (String t : titles) {
                if (post.getTitle().toLowerCase().contains(t.toLowerCase().trim())) {
                    hasRequiredString = true;
                    break;
                }
            }
            if (!hasRequiredString) {
                return false;
            }
        }
        if (postFilter.excludeSubreddits != null && !postFilter.excludeSubreddits.equals("")) {
            String[] subreddits = postFilter.excludeSubreddits.split(",", 0);
            for (String s : subreddits) {
                if (!s.trim().equals("") && post.getSubredditName().equalsIgnoreCase(s.trim())) {
                    return false;
                }
            }
        }
        if (postFilter.excludeUsers != null && !postFilter.excludeUsers.equals("")) {
            String[] users = postFilter.excludeUsers.split(",", 0);
            for (String u : users) {
                if (!u.trim().equals("") && post.getAuthor().equalsIgnoreCase(u.trim())) {
                    return false;
                }
            }
        }
        if (postFilter.excludeFlairs != null && !postFilter.excludeFlairs.equals("")) {
            String[] flairs = postFilter.excludeFlairs.split(",", 0);
            for (String f : flairs) {
                if (!f.trim().equals("") && post.getFlair().equalsIgnoreCase(f.trim())) {
                    return false;
                }
            }
        }
        if (post.getUrl() != null && postFilter.excludeDomains != null && !postFilter.excludeDomains.equals("")) {
            String[] domains = postFilter.excludeDomains.split(",", 0);
            String url = post.getUrl().toLowerCase();
            for (String f : domains) {
                if (!f.trim().equals("") && url.contains(f.trim().toLowerCase())) {
                    return false;
                }
            }
        }
        if (post.getUrl() != null && postFilter.containDomains != null && !postFilter.containDomains.equals("")) {
            String[] domains = postFilter.containDomains.split(",", 0);
            String url = post.getUrl().toLowerCase();
            boolean hasRequiredDomain = false;
            for (String f : domains) {
                if (url.contains(f.trim().toLowerCase())) {
                    hasRequiredDomain = true;
                    break;
                }
            }
            if (!hasRequiredDomain) {
                return false;
            }
        }
        if (postFilter.containFlairs != null && !postFilter.containFlairs.equals("")) {
            String[] flairs = postFilter.containFlairs.split(",", 0);
            if (flairs.length > 0) {
                boolean match = false;
                for (int i = 0; i < flairs.length; i++) {
                    String flair = flairs[i].trim();
                    if (flair.equals("") && i == flairs.length - 1) {
                       return false;
                    }
                    if (!flair.equals("") && post.getFlair().equalsIgnoreCase(flair)) {
                        match = true;
                        break;
                    }
                }

                return match;
            }
        }

        return true;
    }

    public static PostFilter mergePostFilter(List<PostFilter> postFilterList) {
        if (postFilterList.size() == 1) {
            return postFilterList.get(0);
        }
        PostFilter postFilter = new PostFilter();
        StringBuilder stringBuilder;
        postFilter.name = "Merged";
        for (PostFilter p : postFilterList) {
            postFilter.maxVote = Math.min(p.maxVote, postFilter.maxVote);
            postFilter.minVote = Math.max(p.minVote, postFilter.minVote);
            postFilter.maxComments = Math.min(p.maxComments, postFilter.maxComments);
            postFilter.minComments = Math.max(p.minComments, postFilter.minComments);
            postFilter.maxAwards = Math.min(p.maxAwards, postFilter.maxAwards);
            postFilter.minAwards = Math.max(p.minAwards, postFilter.minAwards);

            postFilter.onlyNSFW = p.onlyNSFW ? p.onlyNSFW : postFilter.onlyNSFW;
            postFilter.onlySpoiler = p.onlySpoiler ? p.onlySpoiler : postFilter.onlySpoiler;

            if (p.postTitleExcludesRegex != null && !p.postTitleExcludesRegex.equals("")) {
                postFilter.postTitleExcludesRegex = p.postTitleExcludesRegex;
            }

            if (p.postTitleContainsRegex != null && !p.postTitleContainsRegex.equals("")) {
                postFilter.postTitleContainsRegex = p.postTitleContainsRegex;
            }

            if (p.postTitleExcludesStrings != null && !p.postTitleExcludesStrings.equals("")) {
                stringBuilder = new StringBuilder(postFilter.postTitleExcludesStrings == null ? "" : postFilter.postTitleExcludesStrings);
                stringBuilder.append(",").append(p.postTitleExcludesStrings);
                postFilter.postTitleExcludesStrings = stringBuilder.toString();
            }

            if (p.postTitleContainsStrings != null && !p.postTitleContainsStrings.equals("")) {
                stringBuilder = new StringBuilder(postFilter.postTitleContainsStrings == null ? "" : postFilter.postTitleContainsStrings);
                stringBuilder.append(",").append(p.postTitleContainsStrings);
                postFilter.postTitleContainsStrings = stringBuilder.toString();
            }

            if (p.excludeSubreddits != null && !p.excludeSubreddits.equals("")) {
                stringBuilder = new StringBuilder(postFilter.excludeSubreddits == null ? "" : postFilter.excludeSubreddits);
                stringBuilder.append(",").append(p.excludeSubreddits);
                postFilter.excludeSubreddits = stringBuilder.toString();
            }

            if (p.excludeUsers != null && !p.excludeUsers.equals("")) {
                stringBuilder = new StringBuilder(postFilter.excludeUsers == null ? "" : postFilter.excludeUsers);
                stringBuilder.append(",").append(p.excludeUsers);
                postFilter.excludeUsers = stringBuilder.toString();
            }

            if (p.containFlairs != null && !p.containFlairs.equals("")) {
                stringBuilder = new StringBuilder(postFilter.containFlairs == null ? "" : postFilter.containFlairs);
                stringBuilder.append(",").append(p.containFlairs);
                postFilter.containFlairs = stringBuilder.toString();
            }

            if (p.excludeFlairs != null && !p.excludeFlairs.equals("")) {
                stringBuilder = new StringBuilder(postFilter.excludeFlairs == null ? "" : postFilter.excludeFlairs);
                stringBuilder.append(",").append(p.excludeFlairs);
                postFilter.excludeFlairs = stringBuilder.toString();
            }

            if (p.excludeDomains != null && !p.excludeDomains.equals("")) {
                stringBuilder = new StringBuilder(postFilter.excludeDomains == null ? "" : postFilter.excludeDomains);
                stringBuilder.append(",").append(p.excludeDomains);
                postFilter.excludeDomains = stringBuilder.toString();
            }

            if (p.containDomains != null && !p.containDomains.equals("")) {
                stringBuilder = new StringBuilder(postFilter.containDomains == null ? "" : postFilter.containDomains);
                stringBuilder.append(",").append(p.containDomains);
                postFilter.containDomains = stringBuilder.toString();
            }

            postFilter.containTextType = p.containTextType || postFilter.containTextType;
            postFilter.containLinkType = p.containLinkType || postFilter.containLinkType;
            postFilter.containImageType = p.containImageType || postFilter.containImageType;
            postFilter.containGifType = p.containGifType || postFilter.containGifType;
            postFilter.containVideoType = p.containVideoType || postFilter.containVideoType;
            postFilter.containGalleryType = p.containGalleryType || postFilter.containGalleryType;
        }

        return postFilter;
    }

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
        parcel.writeString(postTitleContainsRegex);
        parcel.writeString(postTitleExcludesStrings);
        parcel.writeString(postTitleContainsStrings);
        parcel.writeString(excludeSubreddits);
        parcel.writeString(excludeUsers);
        parcel.writeString(containFlairs);
        parcel.writeString(excludeFlairs);
        parcel.writeString(excludeDomains);
        parcel.writeString(containDomains);
        parcel.writeByte((byte) (containTextType ? 1 : 0));
        parcel.writeByte((byte) (containLinkType ? 1 : 0));
        parcel.writeByte((byte) (containImageType ? 1 : 0));
        parcel.writeByte((byte) (containGifType ? 1 : 0));
        parcel.writeByte((byte) (containVideoType ? 1 : 0));
        parcel.writeByte((byte) (containGalleryType ? 1 : 0));
    }
}
