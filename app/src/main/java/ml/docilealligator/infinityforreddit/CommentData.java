package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

class CommentData implements Parcelable {
    private String id;
    private String fullName;
    private String author;
    private String commentTime;
    private String commentContent;
    private String parentId;
    private int score;
    private int voteType;
    private boolean isSubmitter;
    private String permalink;
    private int depth;
    private boolean collapsed;
    private boolean hasReply;
    private boolean scoreHidden;
    private boolean isExpanded;
    private ArrayList<CommentData> children;
    private ArrayList<String> moreChildrenIds;

    CommentData(String id, String fullName, String author, String commentTime, String commentContent,
                String parentId, int score, boolean isSubmitter, String permalink, int depth,
                boolean collapsed, boolean hasReply, boolean scoreHidden) {
        this.id = id;
        this.fullName = fullName;
        this.author = author;
        this.commentTime = commentTime;
        this.commentContent = commentContent;
        this.parentId = parentId;
        this.score = score;
        this.isSubmitter = isSubmitter;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.depth = depth;
        this.collapsed = collapsed;
        this.hasReply = hasReply;
        this.scoreHidden = scoreHidden;
        this.isExpanded = false;
    }

    protected CommentData(Parcel in) {
        id = in.readString();
        fullName = in.readString();
        author = in.readString();
        commentTime = in.readString();
        commentContent = in.readString();
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
        children = in.readArrayList(null);
        moreChildrenIds = in.readArrayList(null);
    }

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

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAuthor() {
        return author;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public String getCommentContent() {
        return commentContent;
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
        if(children == null) {
            setChildren(moreChildren);
        } else {
            children.addAll(moreChildren);
        }
    }

    public void addChild(CommentData comment) {
        if(children == null) {
            children = new ArrayList<>();
        }
        children.add(0, comment);
    }

    public ArrayList<String> getMoreChildrenIds() {
        return moreChildrenIds;
    }

    public void setMoreChildrenIds(ArrayList<String> moreChildrenIds) {
        this.moreChildrenIds = moreChildrenIds;
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
        parcel.writeString(commentTime);
        parcel.writeString(commentContent);
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
        parcel.writeList(moreChildrenIds);
    }
}
