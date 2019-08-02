package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alex on 3/1/18.
 */

class Post implements Parcelable {
    static final int TEXT_TYPE = 0;
    static final int IMAGE_TYPE = 1;
    static final int LINK_TYPE = 2;
    static final int VIDEO_TYPE = 3;
    static final int GIF_VIDEO_TYPE = 4;
    static final int NO_PREVIEW_LINK_TYPE = 5;

    private String id;
    private String fullName;
    private String subredditNamePrefixed;
    private String subredditIconUrl;
    private String author;
    private String authorNamePrefixed;
    private String authorIconUrl;
    private String postTime;
    private String title;
    private String selfText;
    private String previewUrl;
    private String url;
    private String videoUrl;
    private String gifOrVideoDownloadUrl;
    private String permalink;
    private String flair;
    private int score;
    private int postType;
    private int voteType;
    private int gilded;
    private int previewWidth;
    private int previewHeight;
    private boolean spoiler;
    private boolean nsfw;
    private boolean stickied;
    private boolean archived;
    private boolean isCrosspost;
    private boolean isDashVideo;
    private boolean isDownloadableGifOrVideo;

    Post(String id, String fullName, String subredditNamePrefixed, String author, String postTime,
         String title, String previewUrl, String permalink, int score, int postType, int voteType,
         int gilded, String flair, boolean spoiler, boolean nsfw, boolean stickied, boolean archived,
         boolean isCrosspost, boolean isDashVideo) {
        this.id = id;
        this.fullName = fullName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
        this.postTime = postTime;
        this.title = title;
        this.previewUrl = previewUrl;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.gilded = gilded;
        this.flair = flair;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.isCrosspost = isCrosspost;
        this.isDashVideo = isDashVideo;
    }

    Post(String id, String fullName, String subredditNamePrefixed, String author, String postTime,
         String title, String previewUrl, String url, String permalink, int score, int postType,
         int voteType, int gilded, String flair, boolean spoiler, boolean nsfw, boolean stickied,
         boolean archived, boolean isCrosspost) {
        this.id = id;
        this.fullName = fullName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
        this.postTime = postTime;
        this.title = title;
        this.previewUrl = previewUrl;
        this.url = url;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.gilded = gilded;
        this.flair = flair;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.isCrosspost = isCrosspost;
    }

    Post(String id, String fullName, String subredditNamePrefixed, String author, String postTime,
         String title, String permalink, int score, int postType, int voteType, int gilded, String flair,
         boolean spoiler, boolean nsfw, boolean stickied, boolean archived, boolean isCrosspost) {
        this.id = id;
        this.fullName = fullName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
        this.postTime = postTime;
        this.title = title;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.gilded = gilded;
        this.flair = flair;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.isCrosspost= isCrosspost;
    }

    protected Post(Parcel in) {
        id = in.readString();
        fullName = in.readString();
        subredditNamePrefixed = in.readString();
        subredditIconUrl = in.readString();
        author = in.readString();
        authorNamePrefixed = in.readString();
        authorIconUrl = in.readString();
        postTime = in.readString();
        title = in.readString();
        selfText = in.readString();
        previewUrl = in.readString();
        url = in.readString();
        videoUrl = in.readString();
        gifOrVideoDownloadUrl = in.readString();
        permalink = in.readString();
        flair = in.readString();
        score = in.readInt();
        postType = in.readInt();
        voteType = in.readInt();
        gilded = in.readInt();
        previewWidth = in.readInt();
        previewHeight = in.readInt();
        spoiler = in.readByte() != 0;
        nsfw = in.readByte() != 0;
        stickied = in.readByte() != 0;
        archived = in.readByte() != 0;
        isCrosspost = in.readByte() != 0;
        isDashVideo = in.readByte() != 0;
        isDownloadableGifOrVideo = in.readByte() != 0;
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

    String getFullName() {
        return fullName;
    }

    String getSubredditNamePrefixed() {
        return subredditNamePrefixed;
    }

    String getSubredditIconUrl() {
        return subredditIconUrl;
    }

    void setSubredditIconUrl(String subredditIconUrl) {
        this.subredditIconUrl = subredditIconUrl;
    }

    String getAuthor() {
        return author;
    }

    String getAuthorNamePrefixed() {
        return authorNamePrefixed;
    }

    String getAuthorIconUrl() {
        return authorIconUrl;
    }

    void setAuthorIconUrl(String authorIconUrl) {
        this.authorIconUrl = authorIconUrl;
    }

    String getPostTime() {
        return postTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    void setSelfText(String selfText) {
        this.selfText = selfText;
    }

    String getSelfText() {
        return selfText;
    }

    String getPreviewUrl() {
        return previewUrl;
    }

    String getUrl() {
        return url;
    }

    void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    String getVideoUrl() {
        return videoUrl;
    }

    String getGifOrVideoDownloadUrl() {
        return gifOrVideoDownloadUrl;
    }

    void setGifOrVideoDownloadUrl(String gifOrVideoDownloadUrl) {
        this.gifOrVideoDownloadUrl = gifOrVideoDownloadUrl;
    }

    String getPermalink() {
        return permalink;
    }

    public String getFlair() {
        return flair;
    }

    void setScore(int score) {
        this.score = score;
    }

    int getScore() {
        return score;
    }

    int getPostType() {
        return postType;
    }

    void setVoteType(int voteType) {
        this.voteType = voteType;
    }

    int getVoteType() {
        return voteType;
    }

    int getGilded() {
        return gilded;
    }

    void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    int getPreviewWidth() {
        return previewWidth;
    }

    void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    int getPreviewHeight() {
        return previewHeight;
    }

    boolean isSpoiler() {
        return spoiler;
    }

    boolean isNSFW() {
        return nsfw;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    boolean isDashVideo() {
        return isDashVideo;
    }

    void setDownloadableGifOrVideo(boolean isDownloadableGifOrVideo) {
        this.isDownloadableGifOrVideo = isDownloadableGifOrVideo;
    }

    boolean isDownloadableGifOrVideo() {
        return isDownloadableGifOrVideo;
    }

    boolean isStickied() {
        return stickied;
    }

    boolean isArchived() {
        return archived;
    }

    boolean isCrosspost() {
        return isCrosspost;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(fullName);
        parcel.writeString(subredditNamePrefixed);
        parcel.writeString(subredditIconUrl);
        parcel.writeString(author);
        parcel.writeString(authorNamePrefixed);
        parcel.writeString(authorIconUrl);
        parcel.writeString(postTime);
        parcel.writeString(title);
        parcel.writeString(selfText);
        parcel.writeString(previewUrl);
        parcel.writeString(url);
        parcel.writeString(videoUrl);
        parcel.writeString(gifOrVideoDownloadUrl);
        parcel.writeString(permalink);
        parcel.writeString(flair);
        parcel.writeInt(score);
        parcel.writeInt(postType);
        parcel.writeInt(voteType);
        parcel.writeInt(gilded);
        parcel.writeInt(previewWidth);
        parcel.writeInt(previewHeight);
        parcel.writeByte((byte) (spoiler ? 1 : 0));
        parcel.writeByte((byte) (nsfw ? 1 : 0));
        parcel.writeByte((byte) (stickied ? 1 : 0));
        parcel.writeByte((byte) (archived ? 1 : 0));
        parcel.writeByte((byte) (isCrosspost ? 1 : 0));
        parcel.writeByte((byte) (isDashVideo ? 1 : 0));
        parcel.writeByte((byte) (isDownloadableGifOrVideo ? 1 : 0));
    }
}