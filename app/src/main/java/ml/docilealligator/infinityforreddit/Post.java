package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alex on 3/1/18.
 */

class Post implements Parcelable {

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
  static final int NSFW_TYPE = -1;
  static final int TEXT_TYPE = 0;
  static final int IMAGE_TYPE = 1;
  static final int LINK_TYPE = 2;
  static final int VIDEO_TYPE = 3;
  static final int GIF_VIDEO_TYPE = 4;
  static final int NO_PREVIEW_LINK_TYPE = 5;
  private final String id;
  private final String fullName;
  private final String subredditName;
  private final String subredditNamePrefixed;
  private final String author;
  private final String authorNamePrefixed;
  private final String postTime;
  private final String permalink;
  private final int postType;
  private final int gilded;
  private final boolean stickied;
  private final boolean archived;
  private final boolean locked;
  private final boolean isCrosspost;
  private String subredditIconUrl;
  private String authorIconUrl;
  private String title;
  private String selfText;
  private String previewUrl;
  private String url;
  private String videoUrl;
  private String gifOrVideoDownloadUrl;
  private String flair;
  private int score;
  private int voteType;
  private int previewWidth;
  private int previewHeight;
  private boolean hidden;
  private boolean spoiler;
  private boolean nsfw;
  private boolean saved;
  private boolean isHLSVideo;
  private boolean isDownloadableGifOrVideo;
  private String crosspostParentId;

  Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
      String author,
      String postTime, String title, String previewUrl, String permalink, int score, int postType,
      int voteType, int gilded, String flair, boolean hidden, boolean spoiler, boolean nsfw,
      boolean stickied, boolean archived, boolean locked, boolean saved, boolean isCrosspost,
      boolean isHLSVideo) {
    this.id = id;
    this.fullName = fullName;
    this.subredditName = subredditName;
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
    this.hidden = hidden;
    this.spoiler = spoiler;
    this.nsfw = nsfw;
    this.stickied = stickied;
    this.archived = archived;
    this.locked = locked;
    this.saved = saved;
    this.isCrosspost = isCrosspost;
    this.isHLSVideo = isHLSVideo;
  }

  Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
      String author,
      String postTime, String title, String previewUrl, String url, String permalink, int score,
      int postType, int voteType, int gilded, String flair, boolean hidden, boolean spoiler,
      boolean nsfw, boolean stickied, boolean archived, boolean locked, boolean saved,
      boolean isCrosspost) {
    this.id = id;
    this.fullName = fullName;
    this.subredditName = subredditName;
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
    this.hidden = hidden;
    this.spoiler = spoiler;
    this.nsfw = nsfw;
    this.stickied = stickied;
    this.archived = archived;
    this.locked = locked;
    this.saved = saved;
    this.isCrosspost = isCrosspost;
  }

  Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
      String author,
      String postTime, String title, String permalink, int score, int postType, int voteType,
      int gilded,
      String flair, boolean hidden, boolean spoiler, boolean nsfw, boolean stickied,
      boolean archived,
      boolean locked, boolean saved, boolean isCrosspost) {
    this.id = id;
    this.fullName = fullName;
    this.subredditName = subredditName;
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
    this.hidden = hidden;
    this.spoiler = spoiler;
    this.nsfw = nsfw;
    this.stickied = stickied;
    this.archived = archived;
    this.locked = locked;
    this.saved = saved;
    this.isCrosspost = isCrosspost;
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
    hidden = in.readByte() != 0;
    spoiler = in.readByte() != 0;
    nsfw = in.readByte() != 0;
    stickied = in.readByte() != 0;
    archived = in.readByte() != 0;
    locked = in.readByte() != 0;
    saved = in.readByte() != 0;
    isCrosspost = in.readByte() != 0;
    isHLSVideo = in.readByte() != 0;
    isDownloadableGifOrVideo = in.readByte() != 0;
    crosspostParentId = in.readString();
  }

  public String getId() {
    return id;
  }

  String getFullName() {
    return fullName;
  }

  String getSubredditName() {
    return subredditName;
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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  String getSelfText() {
    return selfText;
  }

  void setSelfText(String selfText) {
    this.selfText = selfText;
  }

  String getPreviewUrl() {
    return previewUrl;
  }

  String getUrl() {
    return url;
  }

  String getVideoUrl() {
    return videoUrl;
  }

  void setVideoUrl(String videoUrl) {
    this.videoUrl = videoUrl;
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

  void setFlair(String flair) {
    this.flair = flair;
  }

  int getScore() {
    return score;
  }

  void setScore(int score) {
    this.score = score;
  }

  int getPostType() {
    return postType;
  }

  int getVoteType() {
    return voteType;
  }

  void setVoteType(int voteType) {
    this.voteType = voteType;
  }

  int getGilded() {
    return gilded;
  }

  int getPreviewWidth() {
    return previewWidth;
  }

  void setPreviewWidth(int previewWidth) {
    this.previewWidth = previewWidth;
  }

  int getPreviewHeight() {
    return previewHeight;
  }

  void setPreviewHeight(int previewHeight) {
    this.previewHeight = previewHeight;
  }

  boolean isHidden() {
    return hidden;
  }

  void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  boolean isSpoiler() {
    return spoiler;
  }

  void setSpoiler(boolean spoiler) {
    this.spoiler = spoiler;
  }

  boolean isNSFW() {
    return nsfw;
  }

  void setNSFW(boolean nsfw) {
    this.nsfw = nsfw;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  boolean isHLSVideo() {
    return isHLSVideo;
  }

  boolean isDownloadableGifOrVideo() {
    return isDownloadableGifOrVideo;
  }

  void setDownloadableGifOrVideo(boolean isDownloadableGifOrVideo) {
    this.isDownloadableGifOrVideo = isDownloadableGifOrVideo;
  }

  boolean isStickied() {
    return stickied;
  }

  boolean isArchived() {
    return archived;
  }

  boolean isLocked() {
    return locked;
  }

  boolean isSaved() {
    return saved;
  }

  void setSaved(boolean saved) {
    this.saved = saved;
  }

  boolean isCrosspost() {
    return isCrosspost;
  }

  public String getCrosspostParentId() {
    return crosspostParentId;
  }

  public void setCrosspostParentId(String crosspostParentId) {
    this.crosspostParentId = crosspostParentId;
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
    parcel.writeByte((byte) (hidden ? 1 : 0));
    parcel.writeByte((byte) (spoiler ? 1 : 0));
    parcel.writeByte((byte) (nsfw ? 1 : 0));
    parcel.writeByte((byte) (stickied ? 1 : 0));
    parcel.writeByte((byte) (archived ? 1 : 0));
    parcel.writeByte((byte) (locked ? 1 : 0));
    parcel.writeByte((byte) (saved ? 1 : 0));
    parcel.writeByte((byte) (isCrosspost ? 1 : 0));
    parcel.writeByte((byte) (isHLSVideo ? 1 : 0));
    parcel.writeByte((byte) (isDownloadableGifOrVideo ? 1 : 0));
    parcel.writeString(crosspostParentId);
  }
}