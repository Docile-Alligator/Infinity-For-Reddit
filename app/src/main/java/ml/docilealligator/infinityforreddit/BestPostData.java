package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alex on 3/1/18.
 */

class BestPostData implements Parcelable {
    static final int TEXT_TYPE = 0;
    static final int IMAGE_TYPE = 1;
    static final int LINK_TYPE = 2;
    static final int VIDEO_TYPE = 3;
    static final int GIF_VIDEO_TYPE = 4;
    static final int NO_PREVIEW_LINK_TYPE = 5;

    private String id;
    private String fullName;
    private String subredditName;
    private String subredditIconUrl;
    private String postTime;
    private String title;
    private String selfText;
    private String previewUrl;
    private String url;
    private String videoUrl;
    private String gifOrVideoDownloadUrl;
    private String permalink;
    private int score;
    private int postType;
    private int voteType;
    private boolean nsfw;
    private boolean isDashVideo;
    private boolean isDownloadableGifOrVideo;

    BestPostData(String id, String fullName, String subredditName, String postTime, String title, String previewUrl, String permalink, int score, int postType, int voteType, boolean nsfw, boolean isDashVideo) {
        this.id = id;
        this.fullName = fullName;
        this.subredditName = subredditName;
        this.postTime = postTime;
        this.title = title;
        this.previewUrl = previewUrl;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.nsfw = nsfw;
        this.isDashVideo = isDashVideo;
    }

    BestPostData(String id, String fullName, String subredditName, String postTime, String title, String previewUrl, String url, String permalink, int score, int postType, int voteType, boolean nsfw) {
        this.id = id;
        this.fullName = fullName;
        this.subredditName = subredditName;
        this.postTime = postTime;
        this.title = title;
        this.previewUrl = previewUrl;
        this.url = url;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.nsfw = nsfw;
    }

    BestPostData(String id, String fullName, String subredditName, String postTime, String title, String permalink, int score, int postType, int voteType, boolean nsfw) {
        this.id = id;
        this.fullName = fullName;
        this.subredditName = subredditName;
        this.postTime = postTime;
        this.title = title;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.nsfw = nsfw;
    }

    protected BestPostData(Parcel in) {
        id = in.readString();
        fullName = in.readString();
        subredditName = in.readString();
        subredditIconUrl = in.readString();
        postTime = in.readString();
        title = in.readString();
        selfText = in.readString();
        previewUrl = in.readString();
        url = in.readString();
        videoUrl = in.readString();
        gifOrVideoDownloadUrl = in.readString();
        permalink = in.readString();
        score = in.readInt();
        postType = in.readInt();
        voteType = in.readInt();
        nsfw = in.readByte() != 0;
        isDashVideo = in.readByte() != 0;
        isDownloadableGifOrVideo = in.readByte() != 0;
    }

    public static final Creator<BestPostData> CREATOR = new Creator<BestPostData>() {
        @Override
        public BestPostData createFromParcel(Parcel in) {
            return new BestPostData(in);
        }

        @Override
        public BestPostData[] newArray(int size) {
            return new BestPostData[size];
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

    public String getSubredditIconUrl() {
        return subredditIconUrl;
    }

    public void setSubredditIconUrl(String subredditIconUrl) {
        this.subredditIconUrl = subredditIconUrl;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setSelfText(String selfText) {
        this.selfText = selfText;
    }

    public String getSelfText() {
        return selfText;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getGifOrVideoDownloadUrl() {
        return gifOrVideoDownloadUrl;
    }

    public void setGifOrVideoDownloadUrl(String gifOrVideoDownloadUrl) {
        this.gifOrVideoDownloadUrl = gifOrVideoDownloadUrl;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public int getPostType() {
        return postType;
    }

    public void setVoteType(int voteType) {
        this.voteType = voteType;
    }

    public int getVoteType() {
        return voteType;
    }

    public boolean getNSFW() {
        return nsfw;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isDashVideo() {
        return isDashVideo;
    }

    public void setDownloadableGifOrVideo(boolean isDownloadableGifOrVideo) {
        this.isDownloadableGifOrVideo = isDownloadableGifOrVideo;
    }

    public boolean isDownloadableGifOrVideo() {
        return isDownloadableGifOrVideo;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(fullName);
        parcel.writeString(subredditName);
        parcel.writeString(subredditIconUrl);
        parcel.writeString(postTime);
        parcel.writeString(title);
        parcel.writeString(selfText);
        parcel.writeString(previewUrl);
        parcel.writeString(url);
        parcel.writeString(videoUrl);
        parcel.writeString(gifOrVideoDownloadUrl);
        parcel.writeString(permalink);
        parcel.writeInt(score);
        parcel.writeInt(postType);
        parcel.writeInt(voteType);
        parcel.writeByte((byte) (nsfw ? 1 : 0));
        parcel.writeByte((byte) (isDashVideo ? 1 : 0));
        parcel.writeByte((byte) (isDownloadableGifOrVideo ? 1 : 0));
    }
}
