package ml.docilealligator.infinityforreddit.post;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;

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
    private String id;
    private String fullName;
    private String subredditName;
    private String subredditNamePrefixed;
    private String subredditIconUrl;
    private String author;
    private String authorNamePrefixed;
    private String authorIconUrl;
    private String authorFlair;
    private String authorFlairHTML;
    private String title;
    private String selfText;
    private String selfTextPlain;
    private String selfTextPlainTrimmed;
    private String url;
    private String videoUrl;
    private String videoDownloadUrl;
    private String gfycatId;
    private String streamableShortCode;
    private boolean isImgur;
    private boolean isGfycat;
    private boolean isRedgifs;
    private boolean isStreamable;
    private boolean loadGfyOrStreamableVideoSuccess;
    private String permalink;
    private String flair;
    private String awards;
    private int nAwards;
    private long postTimeMillis;
    private int score;
    private int postType;
    private int voteType;
    private int nComments;
    private int upvoteRatio;
    private boolean hidden;
    private boolean spoiler;
    private boolean nsfw;
    private boolean stickied;
    private boolean archived;
    private boolean locked;
    private boolean saved;
    private boolean isCrosspost;
    private boolean isRead;
    private boolean isHiddenInRecyclerView = false;
    private boolean isHiddenManuallyByUser = false;
    private String crosspostParentId;
    private ArrayList<Preview> previews = new ArrayList<>();
    private ArrayList<Gallery> gallery = new ArrayList<>();

    public Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
                String author, String authorFlair, String authorFlairHTML, long postTimeMillis,
                String title, String permalink, int score, int postType, int voteType, int nComments,
                int upvoteRatio, String flair, String awards, int nAwards, boolean hidden, boolean spoiler,
                boolean nsfw, boolean stickied, boolean archived, boolean locked, boolean saved,
                boolean isCrosspost) {
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
        this.awards = awards;
        this.nAwards = nAwards;
        this.hidden = hidden;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.locked = locked;
        this.saved = saved;
        this.isCrosspost = isCrosspost;
        isRead = false;
    }

    public Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
                String author, String authorFlair, String authorFlairHTML, long postTimeMillis, String title,
                String url, String permalink, int score, int postType, int voteType, int nComments,
                int upvoteRatio, String flair, String awards, int nAwards, boolean hidden, boolean spoiler,
                boolean nsfw, boolean stickied, boolean archived, boolean locked, boolean saved,
                boolean isCrosspost) {
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
        this.awards = awards;
        this.nAwards = nAwards;
        this.hidden = hidden;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.locked = locked;
        this.saved = saved;
        this.isCrosspost = isCrosspost;
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
        authorFlair = in.readString();
        authorFlairHTML = in.readString();
        authorIconUrl = in.readString();
        postTimeMillis = in.readLong();
        title = in.readString();
        selfText = in.readString();
        selfTextPlain = in.readString();
        selfTextPlainTrimmed = in.readString();
        url = in.readString();
        videoUrl = in.readString();
        videoDownloadUrl = in.readString();
        gfycatId = in.readString();
        streamableShortCode = in.readString();
        isImgur = in.readByte() != 0;
        isGfycat = in.readByte() != 0;
        isRedgifs = in.readByte() != 0;
        isStreamable = in.readByte() != 0;
        loadGfyOrStreamableVideoSuccess = in.readByte() != 0;
        permalink = in.readString();
        flair = in.readString();
        awards = in.readString();
        nAwards = in.readInt();
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
        isHiddenInRecyclerView = in.readByte() != 0;
        isHiddenManuallyByUser = in.readByte() != 0;
        crosspostParentId = in.readString();
        in.readTypedList(previews, Preview.CREATOR);
        in.readTypedList(gallery, Gallery.CREATOR);
    }

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

    public String getGfycatId() {
        return gfycatId;
    }

    public void setGfycatId(String gfycatId) {
        this.gfycatId = gfycatId;
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

    public boolean isGfycat() {
        return isGfycat;
    }

    public void setIsGfycat(boolean isGfycat) {
        this.isGfycat = isGfycat;
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

    public boolean isLoadGfycatOrStreamableVideoSuccess() {
        return loadGfyOrStreamableVideoSuccess;
    }

    public void setLoadGfyOrStreamableVideoSuccess(boolean loadGfyOrStreamableVideoSuccess) {
        this.loadGfyOrStreamableVideoSuccess = loadGfyOrStreamableVideoSuccess;
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

    public String getAwards() {
        return awards;
    }

    public void addAwards(String newAwardsHTML) {
        awards += newAwardsHTML;
    }

    public int getNAwards() {
        return nAwards;
    }

    public void addAwards(int newNAwards) {
        nAwards += newNAwards;
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

    public void markAsRead(boolean isHiddenManuallyByUser) {
        isRead = true;
        this.isHiddenManuallyByUser = isHiddenManuallyByUser;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isHiddenInRecyclerView() {
        return isHiddenInRecyclerView;
    }

    public void hidePostInRecyclerView() {
        isHiddenInRecyclerView = true;
    }

    public boolean isHiddenManuallyByUser() {
        return isHiddenManuallyByUser;
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

    public ArrayList<Gallery> getGallery() {
        return gallery;
    }

    public void setGallery(ArrayList<Gallery> gallery) {
        this.gallery = gallery;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(fullName);
        parcel.writeString(subredditName);
        parcel.writeString(subredditNamePrefixed);
        parcel.writeString(subredditIconUrl);
        parcel.writeString(author);
        parcel.writeString(authorNamePrefixed);
        parcel.writeString(authorFlair);
        parcel.writeString(authorFlairHTML);
        parcel.writeString(authorIconUrl);
        parcel.writeLong(postTimeMillis);
        parcel.writeString(title);
        parcel.writeString(selfText);
        parcel.writeString(selfTextPlain);
        parcel.writeString(selfTextPlainTrimmed);
        parcel.writeString(url);
        parcel.writeString(videoUrl);
        parcel.writeString(videoDownloadUrl);
        parcel.writeString(gfycatId);
        parcel.writeString(streamableShortCode);
        parcel.writeByte((byte) (isImgur ? 1 : 0));
        parcel.writeByte((byte) (isGfycat ? 1 : 0));
        parcel.writeByte((byte) (isRedgifs ? 1 : 0));
        parcel.writeByte((byte) (isStreamable ? 1 : 0));
        parcel.writeByte((byte) (loadGfyOrStreamableVideoSuccess ? 1 : 0));
        parcel.writeString(permalink);
        parcel.writeString(flair);
        parcel.writeString(awards);
        parcel.writeInt(nAwards);
        parcel.writeInt(score);
        parcel.writeInt(postType);
        parcel.writeInt(voteType);
        parcel.writeInt(nComments);
        parcel.writeInt(upvoteRatio);
        parcel.writeByte((byte) (hidden ? 1 : 0));
        parcel.writeByte((byte) (spoiler ? 1 : 0));
        parcel.writeByte((byte) (nsfw ? 1 : 0));
        parcel.writeByte((byte) (stickied ? 1 : 0));
        parcel.writeByte((byte) (archived ? 1 : 0));
        parcel.writeByte((byte) (locked ? 1 : 0));
        parcel.writeByte((byte) (saved ? 1 : 0));
        parcel.writeByte((byte) (isCrosspost ? 1 : 0));
        parcel.writeByte((byte) (isRead ? 1 : 0));
        parcel.writeByte((byte) (isHiddenInRecyclerView ? 1 : 0));
        parcel.writeByte((byte) (isHiddenManuallyByUser ? 1 : 0));
        parcel.writeString(crosspostParentId);
        parcel.writeTypedList(previews);
        parcel.writeTypedList(gallery);
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
