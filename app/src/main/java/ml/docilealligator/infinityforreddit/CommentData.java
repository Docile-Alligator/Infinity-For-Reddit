package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.Utils.RedditUtils;

public class CommentData implements Parcelable {
    public static final int VOTE_TYPE_NO_VOTE = 0;
    public static final int VOTE_TYPE_UPVOTE = 1;
    public static final int VOTE_TYPE_DOWNVOTE = -1;
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
    private String id;
    private String fullName;
    private String author;
    private String authorFlair;
    private String authorFlairHTML;
    private String linkAuthor;
    private String commentTime;
    private long commentTimeMillis;
    private String commentMarkdown;
    private String commentRawText;
    private String linkId;
    private String subredditName;
    private String parentId;
    private int score;
    private int voteType;
    private boolean isSubmitter;
    private String distinguished;
    private String permalink;
    private String awards;
    private int depth;
    private boolean collapsed;
    private boolean hasReply;
    private boolean scoreHidden;
    private boolean saved;
    private boolean isExpanded;
    private ArrayList<CommentData> children;
    private ArrayList<String> moreChildrenFullnames;
    private int moreChildrenStartingIndex;
    private boolean isPlaceHolder;
    private boolean isLoadingMoreChildren;
    private boolean loadMoreChildrenFailed;

    public CommentData(String id, String fullName, String author, String authorFlair,
                       String authorFlairHTML, String linkAuthor, String commentTime,
                       long commentTimeMillis, String commentMarkdown, String commentRawText,
                       String linkId, String subredditName, String parentId, int score,
                       int voteType, boolean isSubmitter, String distinguished, String permalink,
                       String awards, int depth, boolean collapsed, boolean hasReply,
                       boolean scoreHidden, boolean saved) {
        this.id = id;
        this.fullName = fullName;
        this.author = author;
        this.authorFlair = authorFlair;
        this.authorFlairHTML = authorFlairHTML;
        this.linkAuthor = linkAuthor;
        this.commentTime = commentTime;
        this.commentTimeMillis = commentTimeMillis;
        this.commentMarkdown = commentMarkdown;
        this.commentRawText = commentRawText;
        this.linkId = linkId;
        this.subredditName = subredditName;
        this.parentId = parentId;
        this.score = score;
        this.voteType = voteType;
        this.isSubmitter = isSubmitter;
        this.distinguished = distinguished;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.awards = awards;
        this.depth = depth;
        this.collapsed = collapsed;
        this.hasReply = hasReply;
        this.scoreHidden = scoreHidden;
        this.saved = saved;
        this.isExpanded = false;
        moreChildrenStartingIndex = 0;
        isPlaceHolder = false;
    }

    public CommentData(String parentFullName, int depth) {
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
        authorFlair = in.readString();
        authorFlairHTML = in.readString();
        linkAuthor = in.readString();
        commentTime = in.readString();
        commentTimeMillis = in.readLong();
        commentMarkdown = in.readString();
        commentRawText = in.readString();
        linkId = in.readString();
        subredditName = in.readString();
        parentId = in.readString();
        score = in.readInt();
        voteType = in.readInt();
        isSubmitter = in.readByte() != 0;
        distinguished = in.readString();
        permalink = in.readString();
        awards = in.readString();
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

    public String getAuthorFlair() {
        return authorFlair;
    }

    public String getAuthorFlairHTML() {
        return authorFlairHTML;
    }

    public String getLinkAuthor() {
        return linkAuthor;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public long getCommentTimeMillis() {
        return commentTimeMillis;
    }

    public String getCommentMarkdown() {
        return commentMarkdown;
    }

    public void setCommentMarkdown(String commentMarkdown) {
        this.commentMarkdown = commentMarkdown;
    }

    public String getCommentRawText() {
        return commentRawText;
    }

    public void setCommentRawText(String commentRawText) {
        this.commentRawText = commentRawText;
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

    public boolean isModerator() {
        return distinguished != null && distinguished.equals("moderator");
    }

    public String getPermalink() {
        return permalink;
    }

    public String getAwards() {
        return awards;
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

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
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
        parcel.writeString(authorFlair);
        parcel.writeString(authorFlairHTML);
        parcel.writeString(linkAuthor);
        parcel.writeString(commentTime);
        parcel.writeLong(commentTimeMillis);
        parcel.writeString(commentMarkdown);
        parcel.writeString(commentRawText);
        parcel.writeString(linkId);
        parcel.writeString(subredditName);
        parcel.writeString(parentId);
        parcel.writeInt(score);
        parcel.writeInt(voteType);
        parcel.writeByte((byte) (isSubmitter ? 1 : 0));
        parcel.writeString(distinguished);
        parcel.writeString(permalink);
        parcel.writeString(awards);
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
