package ml.docilealligator.infinityforreddit.post;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Map;

import ml.docilealligator.infinityforreddit.MediaMetadata;
import ml.docilealligator.infinityforreddit.utils.APIUtils;

/**
 * Created by alex on 3/1/18.
 */

public class Post implements Parcelable {
    public static final int NSFW_TYPE = -1;
    public static final int TEXT_TYPE = 0;
    public static final int IMAGE_TYPE = 1;
    public static final int LINK_TYPE = 2;
    public static final int VIDEO_TYPE = 3;
    public static final int GIF_TYPE = 4;
    public static final int NO_PREVIEW_LINK_TYPE = 5;
    public static final int GALLERY_TYPE = 6;

    private final String id;
    private final String fullName;
    private final String subredditName;
    private final String subredditNamePrefixed;
    private String subredditIconUrl;
    private String author;
    private String authorNamePrefixed;
    private String authorIconUrl;
    private final String authorFlair;
    private final String authorFlairHTML;
    private String title;
    private String selfText;
    private String selfTextPlain;
    private String selfTextPlainTrimmed;
    private String url;
    private String videoUrl;
    private String videoDownloadUrl;
    @Nullable
    private String videoFallBackDirectUrl;
    private String redgifsId;
    private String streamableShortCode;
    private boolean isImgur;
    private boolean isRedgifs;
    private boolean isStreamable;
    private boolean loadRedgifsOrStreamableVideoSuccess;
    private final String permalink;
    private String flair;
    private final long postTimeMillis;
    private int score;
    private int postType;
    private int voteType;
    private int nComments;
    private int upvoteRatio;
    private boolean hidden;
    private boolean spoiler;
    private boolean nsfw;
    private final boolean stickied;
    private final boolean archived;
    private final boolean locked;
    private boolean saved;
    private final boolean isCrosspost;
    private boolean isRead;
    private String crosspostParentId;
    private final String distinguished;
    private final String suggestedSort;
    private String mp4Variant;
    private ArrayList<Preview> previews = new ArrayList<>();
    @Nullable
    private Map<String, MediaMetadata> mediaMetadataMap;
    private ArrayList<Gallery> gallery = new ArrayList<>();

    //Text and video posts
    public Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
                String author, String authorFlair, String authorFlairHTML, long postTimeMillis,
                String title, String permalink, int score, int postType, int voteType, int nComments,
                int upvoteRatio, String flair, boolean hidden, boolean spoiler,
                boolean nsfw, boolean stickied, boolean archived, boolean locked, boolean saved,
                boolean isCrosspost, String distinguished, String suggestedSort) {
        this.id = id;
        this.fullName = fullName;
        this.subredditName = subredditName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
        this.authorFlair = authorFlair;
        this.authorFlairHTML = authorFlairHTML;
        this.postTimeMillis = postTimeMillis;
        this.title = title;
        this.permalink = APIUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.nComments = nComments;
        this.upvoteRatio = upvoteRatio;
        this.flair = flair;
        this.hidden = hidden;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.locked = locked;
        this.saved = saved;
        this.isCrosspost = isCrosspost;
        this.distinguished = distinguished;
        this.suggestedSort = suggestedSort;
        isRead = false;
    }

    public Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
                String author, String authorFlair, String authorFlairHTML, long postTimeMillis, String title,
                String url, String permalink, int score, int postType, int voteType, int nComments,
                int upvoteRatio, String flair, boolean hidden, boolean spoiler,
                boolean nsfw, boolean stickied, boolean archived, boolean locked, boolean saved,
                boolean isCrosspost, String distinguished, String suggestedSort) {
        this.id = id;
        this.fullName = fullName;
        this.subredditName = subredditName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
        this.authorFlair = authorFlair;
        this.authorFlairHTML = authorFlairHTML;
        this.postTimeMillis = postTimeMillis;
        this.title = title;
        this.url = url;
        this.permalink = APIUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.nComments = nComments;
        this.upvoteRatio = upvoteRatio;
        this.flair = flair;
        this.hidden = hidden;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.locked = locked;
        this.saved = saved;
        this.isCrosspost = isCrosspost;
        this.distinguished = distinguished;
        this.suggestedSort = suggestedSort;
        isRead = false;
    }

    protected Post(Parcel in) {
        id = in.readString();
        fullName = in.readString();
        subredditName = in.readString();
        subredditNamePrefixed = in.readString();
        subredditIconUrl = in.readString();
        author = in.readString();
        authorNamePrefixed = in.readString();
        authorIconUrl = in.readString();
        authorFlair = in.readString();
        authorFlairHTML = in.readString();
        title = in.readString();
        selfText = in.readString();
        selfTextPlain = in.readString();
        selfTextPlainTrimmed = in.readString();
        url = in.readString();
        videoUrl = in.readString();
        videoDownloadUrl = in.readString();
        videoFallBackDirectUrl = in.readString();
        redgifsId = in.readString();
        streamableShortCode = in.readString();
        isImgur = in.readByte() != 0;
        isRedgifs = in.readByte() != 0;
        isStreamable = in.readByte() != 0;
        loadRedgifsOrStreamableVideoSuccess = in.readByte() != 0;
        permalink = in.readString();
        flair = in.readString();
        postTimeMillis = in.readLong();
        score = in.readInt();
        postType = in.readInt();
        voteType = in.readInt();
        nComments = in.readInt();
        upvoteRatio = in.readInt();
        hidden = in.readByte() != 0;
        spoiler = in.readByte() != 0;
        nsfw = in.readByte() != 0;
        stickied = in.readByte() != 0;
        archived = in.readByte() != 0;
        locked = in.readByte() != 0;
        saved = in.readByte() != 0;
        isCrosspost = in.readByte() != 0;
        isRead = in.readByte() != 0;
        crosspostParentId = in.readString();
        distinguished = in.readString();
        suggestedSort = in.readString();
        mp4Variant = in.readString();
        previews = in.createTypedArrayList(Preview.CREATOR);
        mediaMetadataMap = (Map<String, MediaMetadata>) in.readValue(getClass().getClassLoader());
        gallery = in.createTypedArrayList(Gallery.CREATOR);
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSubredditName() {
        return subredditName;
    }

    public String getSubredditNamePrefixed() {
        return subredditNamePrefixed;
    }

    public String getSubredditIconUrl() {
        return subredditIconUrl;
    }

    public void setSubredditIconUrl(String subredditIconUrl) {
        this.subredditIconUrl = subredditIconUrl;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAuthorDeleted() {
        return author != null && author.equals("[deleted]");
    }

    public void setAuthor(String author) {
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
    }

    public String getAuthorNamePrefixed() {
        return authorNamePrefixed;
    }

    public String getAuthorFlair() {
        return authorFlair;
    }

    public String getAuthorFlairHTML() {
        return authorFlairHTML;
    }

    public String getAuthorIconUrl() {
        return authorIconUrl;
    }

    public void setAuthorIconUrl(String authorIconUrl) {
        this.authorIconUrl = authorIconUrl;
    }

    public long getPostTimeMillis() {
        return postTimeMillis;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSelfText() {
        return selfText;
    }

    public void setSelfText(String selfText) {
        this.selfText = selfText;
    }

    public String getSelfTextPlain() {
        return selfTextPlain;
    }

    public void setSelfTextPlain(String selfTextPlain) {
        this.selfTextPlain = selfTextPlain;
    }

    public String getSelfTextPlainTrimmed() {
        return selfTextPlainTrimmed;
    }

    public void setSelfTextPlainTrimmed(String selfTextPlainTrimmed) {
        this.selfTextPlainTrimmed = selfTextPlainTrimmed;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoDownloadUrl() {
        return videoDownloadUrl;
    }

    public void setVideoDownloadUrl(String videoDownloadUrl) {
        this.videoDownloadUrl = videoDownloadUrl;
    }

    @Nullable
    public String getVideoFallBackDirectUrl() {
        return videoFallBackDirectUrl;
    }

    public void setVideoFallBackDirectUrl(@Nullable String videoFallBackDirectUrl) {
        this.videoFallBackDirectUrl = videoFallBackDirectUrl;
    }

    public String getRedgifsId() {
        return redgifsId;
    }

    public void setRedgifsId(String redgifsId) {
        this.redgifsId = redgifsId;
    }

    public String getStreamableShortCode() {
        return streamableShortCode;
    }

    public void setStreamableShortCode(String shortCode) {
        this.streamableShortCode = shortCode;
    }

    public void setIsImgur(boolean isImgur) {
        this.isImgur = isImgur;
    }

    public boolean isImgur() {
        return isImgur;
    }

    public boolean isRedgifs() {
        return isRedgifs;
    }

    public void setIsRedgifs(boolean isRedgifs) {
        this.isRedgifs = isRedgifs;
    }

    public boolean isStreamable() {
        return isStreamable;
    }

    public void setIsStreamable(boolean isStreamable) {
        this.isStreamable = isStreamable;
    }

    public boolean isLoadRedgifsOrStreamableVideoSuccess() {
        return loadRedgifsOrStreamableVideoSuccess;
    }

    public void setLoadRedgifsOrStreamableVideoSuccess(boolean loadRedgifsOrStreamableVideoSuccess) {
        this.loadRedgifsOrStreamableVideoSuccess = loadRedgifsOrStreamableVideoSuccess;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getFlair() {
        return flair;
    }

    public void setFlair(String flair) {
        this.flair = flair;
    }

    public boolean isModerator() {
        return distinguished != null && distinguished.equals("moderator");
    }

    public boolean isAdmin() {
        return distinguished != null && distinguished.equals("admin");
    }

    public String getSuggestedSort() {
        return suggestedSort;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPostType() {
        return postType;
    }

    public void setPostType(int postType) {
        this.postType = postType;
    }

    public int getVoteType() {
        return voteType;
    }

    public void setVoteType(int voteType) {
        this.voteType = voteType;
    }

    public int getNComments() {
        return nComments;
    }

    public void setNComments(int nComments) {
        this.nComments = nComments;
    }

    public int getUpvoteRatio() {
        return upvoteRatio;
    }

    public void setUpvoteRatio(int upvoteRatio) {
        this.upvoteRatio = upvoteRatio;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isSpoiler() {
        return spoiler;
    }

    public void setSpoiler(boolean spoiler) {
        this.spoiler = spoiler;
    }

    public boolean isNSFW() {
        return nsfw;
    }

    public void setNSFW(boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(fullName);
        dest.writeString(subredditName);
        dest.writeString(subredditNamePrefixed);
        dest.writeString(subredditIconUrl);
        dest.writeString(author);
        dest.writeString(authorNamePrefixed);
        dest.writeString(authorIconUrl);
        dest.writeString(authorFlair);
        dest.writeString(authorFlairHTML);
        dest.writeString(title);
        dest.writeString(selfText);
        dest.writeString(selfTextPlain);
        dest.writeString(selfTextPlainTrimmed);
        dest.writeString(url);
        dest.writeString(videoUrl);
        dest.writeString(videoDownloadUrl);
        dest.writeString(videoFallBackDirectUrl);
        dest.writeString(redgifsId);
        dest.writeString(streamableShortCode);
        dest.writeByte((byte) (isImgur ? 1 : 0));
        dest.writeByte((byte) (isRedgifs ? 1 : 0));
        dest.writeByte((byte) (isStreamable ? 1 : 0));
        dest.writeByte((byte) (loadRedgifsOrStreamableVideoSuccess ? 1 : 0));
        dest.writeString(permalink);
        dest.writeString(flair);
        dest.writeLong(postTimeMillis);
        dest.writeInt(score);
        dest.writeInt(postType);
        dest.writeInt(voteType);
        dest.writeInt(nComments);
        dest.writeInt(upvoteRatio);
        dest.writeByte((byte) (hidden ? 1 : 0));
        dest.writeByte((byte) (spoiler ? 1 : 0));
        dest.writeByte((byte) (nsfw ? 1 : 0));
        dest.writeByte((byte) (stickied ? 1 : 0));
        dest.writeByte((byte) (archived ? 1 : 0));
        dest.writeByte((byte) (locked ? 1 : 0));
        dest.writeByte((byte) (saved ? 1 : 0));
        dest.writeByte((byte) (isCrosspost ? 1 : 0));
        dest.writeByte((byte) (isRead ? 1 : 0));
        dest.writeString(crosspostParentId);
        dest.writeString(distinguished);
        dest.writeString(suggestedSort);
        dest.writeString(mp4Variant);
        dest.writeTypedList(previews);
        dest.writeValue(mediaMetadataMap);
        dest.writeTypedList(gallery);
    }

    public boolean isStickied() {
        return stickied;
    }

    public boolean isArchived() {
        return archived;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isCrosspost() {
        return isCrosspost;
    }

    public void markAsRead() {
        isRead = true;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getCrosspostParentId() {
        return crosspostParentId;
    }

    public void setCrosspostParentId(String crosspostParentId) {
        this.crosspostParentId = crosspostParentId;
    }

    public ArrayList<Preview> getPreviews() {
        return previews;
    }

    public void setPreviews(ArrayList<Preview> previews) {
        this.previews = previews;
    }

    @Nullable
    public Map<String, MediaMetadata> getMediaMetadataMap() {
        return mediaMetadataMap;
    }

    public void setMediaMetadataMap(@Nullable Map<String, MediaMetadata> mediaMetadataMap) {
        this.mediaMetadataMap = mediaMetadataMap;
    }

    public ArrayList<Gallery> getGallery() {
        return gallery;
    }

    public void setGallery(ArrayList<Gallery> gallery) {
        this.gallery = gallery;
    }

    public String getMp4Variant() {
        return mp4Variant;
    }

    public void setMp4Variant(String mp4Variant) {
        this.mp4Variant = mp4Variant;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Post)) {
            return false;
        }
        return ((Post) obj).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class Gallery implements Parcelable {
        public static final int TYPE_IMAGE = 0;
        public static final int TYPE_GIF = 1;
        public static final int TYPE_VIDEO = 2;

        public String mimeType;
        public String url;
        public String fallbackUrl;
        private boolean hasFallback;
        public String fileName;
        public int mediaType;
        public String caption;
        public String captionUrl;

        public Gallery(String mimeType, String url, String fallbackUrl, String fileName, String caption, String captionUrl) {
            this.mimeType = mimeType;
            this.url = url;
            this.fallbackUrl = fallbackUrl;
            this.fileName = fileName;
            if (mimeType.contains("gif")) {
                mediaType = TYPE_GIF;
            } else if (mimeType.contains("jpg") || mimeType.contains("png")) {
                mediaType = TYPE_IMAGE;
            } else {
                mediaType = TYPE_VIDEO;
            }
            this.caption = caption;
            this.captionUrl = captionUrl;
        }

        protected Gallery(Parcel in) {
            mimeType = in.readString();
            url = in.readString();
            fallbackUrl = in.readString();
            hasFallback = in.readByte() != 0;
            fileName = in.readString();
            mediaType = in.readInt();
            caption = in.readString();
            captionUrl = in.readString();
        }

        public static final Creator<Gallery> CREATOR = new Creator<Gallery>() {
            @Override
            public Gallery createFromParcel(Parcel in) {
                return new Gallery(in);
            }

            @Override
            public Gallery[] newArray(int size) {
                return new Gallery[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mimeType);
            parcel.writeString(url);
            parcel.writeString(fallbackUrl);
            parcel.writeByte((byte) (hasFallback ? 1 : 0));
            parcel.writeString(fileName);
            parcel.writeInt(mediaType);
            parcel.writeString(caption);
            parcel.writeString(captionUrl);
        }

        public void setFallbackUrl(String fallbackUrl) { this.fallbackUrl = fallbackUrl; }

        public void setHasFallback(boolean hasFallback) { this.hasFallback = hasFallback; }

        public boolean hasFallback() { return this.hasFallback; }
    }

    public static class Preview implements Parcelable {
        private String previewUrl;
        private int previewWidth;
        private int previewHeight;
        private String previewCaption;
        private String previewCaptionUrl;

        public Preview(String previewUrl, int previewWidth, int previewHeight, String previewCaption, String previewCaptionUrl) {
            this.previewUrl = previewUrl;
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
            this.previewCaption = previewCaption;
            this.previewCaptionUrl = previewCaptionUrl;
        }

        protected Preview(Parcel in) {
            previewUrl = in.readString();
            previewWidth = in.readInt();
            previewHeight = in.readInt();
            previewCaption = in.readString();
            previewCaptionUrl = in.readString();
        }

        public static final Creator<Preview> CREATOR = new Creator<Preview>() {
            @Override
            public Preview createFromParcel(Parcel in) {
                return new Preview(in);
            }

            @Override
            public Preview[] newArray(int size) {
                return new Preview[size];
            }
        };

        public String getPreviewUrl() {
            return previewUrl;
        }

        public void setPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
        }

        public int getPreviewWidth() {
            return previewWidth;
        }

        public void setPreviewWidth(int previewWidth) {
            this.previewWidth = previewWidth;
        }

        public int getPreviewHeight() {
            return previewHeight;
        }

        public void setPreviewHeight(int previewHeight) {
            this.previewHeight = previewHeight;
        }

        public String getPreviewCaption() {
            return previewCaption;
        }

        public void setPreviewCaption(String previewCaption) { this.previewCaption = previewCaption; }

        public String getPreviewCaptionUrl() {
            return previewCaptionUrl;
        }

        public void setPreviewCaptionUrl(String previewCaptionUrl) { this.previewCaptionUrl = previewCaptionUrl; }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(previewUrl);
            parcel.writeInt(previewWidth);
            parcel.writeInt(previewHeight);
            parcel.writeString(previewCaption);
            parcel.writeString(previewCaptionUrl);
        }
    }
}
