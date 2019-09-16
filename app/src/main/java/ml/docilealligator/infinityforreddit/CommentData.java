package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

class CommentData implements Parcelable {

  public static final Creator<CommentData> CREATOR = new Creator<CommentData>() {
    @Override
    public CommentData createFromParcel(Parcel in) {
      return new CommentData(in);
    }

    @Override
    public CommentData[] newArray(int size) {
      return new CommentData[size];
    }
  };
  static final int VOTE_TYPE_NO_VOTE = 0;
  static final int VOTE_TYPE_UPVOTE = 1;
  static final int VOTE_TYPE_DOWNVOTE = -1;
  private final String fullName;
  private final int depth;
  private final boolean isPlaceHolder;
  private String id;
  private String author;
  private String linkAuthor;
  private String commentTime;
  private String commentContent;
  private String linkId;
  private String subredditName;
  private String parentId;
  private int score;
  private int voteType;
  private boolean isSubmitter;
  private String permalink;
  private boolean collapsed;
  private boolean hasReply;
  private boolean scoreHidden;
  private boolean isExpanded;
  private ArrayList<CommentData> children;
  private ArrayList<String> moreChildrenFullnames;
  private int moreChildrenStartingIndex;
  private boolean isLoadingMoreChildren;
  private boolean loadMoreChildrenFailed;

  CommentData(String id, String fullName, String author, String linkAuthor, String commentTime,
      String commentContent,
      String linkId, String subredditName, String parentId, int score, int voteType,
      boolean isSubmitter, String permalink,
      int depth, boolean collapsed, boolean hasReply, boolean scoreHidden) {
    this.id = id;
    this.fullName = fullName;
    this.author = author;
    this.linkAuthor = linkAuthor;
    this.commentTime = commentTime;
    this.commentContent = commentContent;
    this.linkId = linkId;
    this.subredditName = subredditName;
    this.parentId = parentId;
    this.score = score;
    this.voteType = voteType;
    this.isSubmitter = isSubmitter;
    this.permalink = RedditUtils.API_BASE_URI + permalink;
    this.depth = depth;
    this.collapsed = collapsed;
    this.hasReply = hasReply;
    this.scoreHidden = scoreHidden;
    this.isExpanded = false;
    moreChildrenStartingIndex = 0;
    isPlaceHolder = false;
  }

  CommentData(String parentFullName, int depth) {
    this.fullName = parentFullName;
    this.depth = depth;
    isPlaceHolder = true;
    isLoadingMoreChildren = false;
    loadMoreChildrenFailed = false;
  }

  protected CommentData(Parcel in) {
    id = in.readString();
    fullName = in.readString();
    author = in.readString();
    linkAuthor = in.readString();
    commentTime = in.readString();
    commentContent = in.readString();
    linkId = in.readString();
    subredditName = in.readString();
    parentId = in.readString();
    score = in.readInt();
    voteType = in.readInt();
    isSubmitter = in.readByte() != 0;
    permalink = in.readString();
    depth = in.readInt();
    collapsed = in.readByte() != 0;
    hasReply = in.readByte() != 0;
    scoreHidden = in.readByte() != 0;
    isExpanded = in.readByte() != 0;
    children = in.readArrayList(CommentData.class.getClassLoader());
    moreChildrenFullnames = in.readArrayList(CommentData.class.getClassLoader());
    moreChildrenStartingIndex = in.readInt();
    isPlaceHolder = in.readByte() != 0;
    isLoadingMoreChildren = in.readByte() != 0;
    loadMoreChildrenFailed = in.readByte() != 0;
  }

  public String getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getLinkAuthor() {
    return linkAuthor;
  }

  public String getCommentTime() {
    return commentTime;
  }

  public String getCommentContent() {
    return commentContent;
  }

  public void setCommentContent(String commentContent) {
    this.commentContent = commentContent;
  }

  public String getLinkId() {
    return linkId;
  }

  public String getSubredditName() {
    return subredditName;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public boolean isSubmitter() {
    return isSubmitter;
  }

  public String getPermalink() {
    return permalink;
  }

  public int getDepth() {
    return depth;
  }

  public boolean isCollapsed() {
    return collapsed;
  }

  public boolean hasReply() {
    return hasReply;
  }

  public void setHasReply(boolean hasReply) {
    this.hasReply = hasReply;
  }

  public boolean isScoreHidden() {
    return scoreHidden;
  }

  public boolean isExpanded() {
    return isExpanded;
  }

  public void setExpanded(boolean isExpanded) {
    this.isExpanded = isExpanded;
  }

  public int getVoteType() {
    return voteType;
  }

  public void setVoteType(int voteType) {
    this.voteType = voteType;
  }

  public ArrayList<CommentData> getChildren() {
    return children;
  }

  public void setChildren(ArrayList<CommentData> children) {
    this.children = children;
  }

  public void addChildren(ArrayList<CommentData> moreChildren) {
    if (children == null || children.size() == 0) {
      setChildren(moreChildren);
    } else {
      if (children.get(children.size() - 1).isPlaceHolder) {
        children.addAll(children.size() - 2, moreChildren);
      } else {
        children.addAll(moreChildren);
      }
    }
  }

  public void addChild(CommentData comment) {
    addChild(comment, 0);
  }

  public void addChild(CommentData comment, int position) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(position, comment);
  }

  public ArrayList<String> getMoreChildrenFullnames() {
    return moreChildrenFullnames;
  }

  public void setMoreChildrenFullnames(ArrayList<String> moreChildrenFullnames) {
    this.moreChildrenFullnames = moreChildrenFullnames;
  }

  public boolean hasMoreChildrenFullnames() {
    return moreChildrenFullnames != null;
  }

  public void removeMoreChildrenFullnames() {
    moreChildrenFullnames.clear();
  }

  public int getMoreChildrenStartingIndex() {
    return moreChildrenStartingIndex;
  }

  public void setMoreChildrenStartingIndex(int moreChildrenStartingIndex) {
    this.moreChildrenStartingIndex = moreChildrenStartingIndex;
  }

  public boolean isPlaceHolder() {
    return isPlaceHolder;
  }

  public boolean isLoadingMoreChildren() {
    return isLoadingMoreChildren;
  }

  public void setLoadingMoreChildren(boolean isLoadingMoreChildren) {
    this.isLoadingMoreChildren = isLoadingMoreChildren;
  }

  public boolean isLoadMoreChildrenFailed() {
    return loadMoreChildrenFailed;
  }

  public void setLoadMoreChildrenFailed(boolean loadMoreChildrenFailed) {
    this.loadMoreChildrenFailed = loadMoreChildrenFailed;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(id);
    parcel.writeString(fullName);
    parcel.writeString(author);
    parcel.writeString(linkAuthor);
    parcel.writeString(commentTime);
    parcel.writeString(commentContent);
    parcel.writeString(linkId);
    parcel.writeString(subredditName);
    parcel.writeString(parentId);
    parcel.writeInt(score);
    parcel.writeInt(voteType);
    parcel.writeByte((byte) (isSubmitter ? 1 : 0));
    parcel.writeString(permalink);
    parcel.writeInt(depth);
    parcel.writeByte((byte) (collapsed ? 1 : 0));
    parcel.writeByte((byte) (hasReply ? 1 : 0));
    parcel.writeByte((byte) (scoreHidden ? 1 : 0));
    parcel.writeByte((byte) (isExpanded ? 1 : 0));
    parcel.writeList(children);
    parcel.writeList(moreChildrenFullnames);
    parcel.writeInt(moreChildrenStartingIndex);
    parcel.writeByte((byte) (isPlaceHolder ? 1 : 0));
    parcel.writeByte((byte) (isLoadingMoreChildren ? 1 : 0));
    parcel.writeByte((byte) (loadMoreChildrenFailed ? 1 : 0));
  }
}
