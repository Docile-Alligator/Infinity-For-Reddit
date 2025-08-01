package ml.docilealligator.infinityforreddit.comment;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Map;

import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.thing.MediaMetadata;
import ml.docilealligator.infinityforreddit.utils.APIUtils;

public class Comment implements Parcelable {
    public static final int VOTE_TYPE_NO_VOTE = 0;
    public static final int VOTE_TYPE_UPVOTE = 1;
    public static final int VOTE_TYPE_DOWNVOTE = -1;
    public static final int NOT_PLACEHOLDER = 0;
    public static final int PLACEHOLDER_LOAD_MORE_COMMENTS = 1;
    public static final int PLACEHOLDER_CONTINUE_THREAD = 2;
    public static final Creator<Comment> CREATOR = new Creator<>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
    private String id;
    private String fullName;
    private String author;
    private String authorFullName;
    private String authorFlair;
    private String authorFlairHTML;
    private String authorIconUrl;
    private String linkAuthor;
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
    private int depth;
    private int childCount;
    private boolean collapsed;
    private boolean hasReply;
    private boolean scoreHidden;
    private boolean saved;
    private boolean sendReplies;
    private boolean locked;
    private boolean canModComment;
    private boolean approved;
    private long approvedAtUTC;
    private String approvedBy;
    private boolean removed;
    private boolean spam;
    private boolean isExpanded;
    private boolean hasExpandedBefore;
    private boolean isFilteredOut;
    private ArrayList<Comment> children;
    private ArrayList<String> moreChildrenIds;
    private int placeholderType;
    private boolean isLoadingMoreChildren;
    private boolean loadMoreChildrenFailed;
    private long editedTimeMillis;
    private Map<String, MediaMetadata> mediaMetadataMap;

    public Comment(String id, String fullName, String author, String authorFullName, String authorFlair,
                   String authorFlairHTML, String linkAuthor,
                   long commentTimeMillis, String commentMarkdown, String commentRawText,
                   String linkId, String subredditName, String parentId, int score,
                   int voteType, boolean isSubmitter, String distinguished, String permalink,
                   int depth, boolean collapsed, boolean hasReply,
                   boolean scoreHidden, boolean saved, boolean sendReplies, boolean locked, boolean canModComment,
                   boolean approved, long approvedAtUTC, String approvedBy, boolean removed, boolean spam,
                   long edited, Map<String, MediaMetadata> mediaMetadataMap) {
        this.id = id;
        this.fullName = fullName;
        this.author = author;
        this.authorFullName = authorFullName;
        this.authorFlair = authorFlair;
        this.authorFlairHTML = authorFlairHTML;
        this.linkAuthor = linkAuthor;
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
        this.permalink = APIUtils.API_BASE_URI + permalink;
        this.depth = depth;
        this.collapsed = collapsed;
        this.hasReply = hasReply;
        this.scoreHidden = scoreHidden;
        this.saved = saved;
        this.sendReplies = sendReplies;
        this.locked = locked;
        this.canModComment = canModComment;
        this.approved = approved;
        this.approvedAtUTC = approvedAtUTC;
        this.approvedBy = approvedBy;
        this.removed = removed;
        this.spam = spam;
        this.isExpanded = false;
        this.hasExpandedBefore = false;
        this.editedTimeMillis = edited;
        this.mediaMetadataMap = mediaMetadataMap;
        placeholderType = NOT_PLACEHOLDER;
    }

    public Comment(String parentFullName, int depth, int placeholderType) {
        if (placeholderType == PLACEHOLDER_LOAD_MORE_COMMENTS) {
            this.fullName = parentFullName;
        } else {
            this.fullName = parentFullName;
            this.parentId = parentFullName.substring(3);
        }
        this.depth = depth;
        this.placeholderType = placeholderType;
        isLoadingMoreChildren = false;
        loadMoreChildrenFailed = false;
    }

    public Comment(String parentFullName) {

    }

    protected Comment(Parcel in) {
        id = in.readString();
        fullName = in.readString();
        author = in.readString();
        authorFullName = in.readString();
        authorFlair = in.readString();
        authorFlairHTML = in.readString();
        authorIconUrl = in.readString();
        linkAuthor = in.readString();
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
        depth = in.readInt();
        childCount = in.readInt();
        collapsed = in.readByte() != 0;
        hasReply = in.readByte() != 0;
        scoreHidden = in.readByte() != 0;
        saved = in.readByte() != 0;
        sendReplies = in.readByte() != 0;
        locked = in.readByte() != 0;
        canModComment = in.readByte() != 0;
        approved = in.readByte() != 0;
        approvedAtUTC = in.readLong();
        approvedBy = in.readString();
        removed = in.readByte() != 0;
        spam = in.readByte() != 0;
        isExpanded = in.readByte() != 0;
        hasExpandedBefore = in.readByte() != 0;
        editedTimeMillis = in.readLong();
        isFilteredOut = in.readByte() != 0;
        children = new ArrayList<>();
        in.readTypedList(children, Comment.CREATOR);
        moreChildrenIds = new ArrayList<>();
        in.readStringList(moreChildrenIds);
        placeholderType = in.readInt();
        isLoadingMoreChildren = in.readByte() != 0;
        loadMoreChildrenFailed = in.readByte() != 0;
        mediaMetadataMap = (Map<String, MediaMetadata>) in.readValue(getClass().getClassLoader());
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

    public boolean isAuthorDeleted() {
        return author != null && author.equals("[deleted]");
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorFullName() {
        return authorFullName;
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

    public String getLinkAuthor() {
        return linkAuthor;
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

    public void setSubmittedByAuthor(boolean isSubmittedByAuthor) {
        this.isSubmitter = isSubmittedByAuthor;
    }

    public boolean isModerator() {
        return distinguished != null && distinguished.equals("moderator");
    }

    public boolean isAdmin() {
        return distinguished != null && distinguished.equals("admin");
    }

    public String getPermalink() {
        return permalink;
    }

    public int getDepth() {
        return depth;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
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

    public boolean isSendReplies() {
        return sendReplies;
    }

    public void toggleSendReplies() {
        sendReplies = !sendReplies;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isCanModComment() {
        return canModComment;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public long getApprovedAtUTC() {
        return approvedAtUTC;
    }

    public void setApprovedAtUTC(long approvedAtUTC) {
        this.approvedAtUTC = approvedAtUTC;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed, boolean spam) {
        this.removed = removed;
        this.spam = spam;
    }

    public boolean isSpam() {
        return spam;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
        if (isExpanded && !hasExpandedBefore) {
            hasExpandedBefore = true;
        }
    }

    public boolean hasExpandedBefore() {
        return hasExpandedBefore;
    }

    public boolean isFilteredOut() {
        return isFilteredOut;
    }

    public void setIsFilteredOut(boolean isFilteredOut) {
        this.isFilteredOut = isFilteredOut;
    }

    public int getVoteType() {
        return voteType;
    }

    public void setVoteType(int voteType) {
        this.voteType = voteType;
    }

    public ArrayList<Comment> getChildren() {
        return children;
    }

    public void addChildren(ArrayList<Comment> moreChildren) {
        if (children == null || children.size() == 0) {
            children = moreChildren;
        } else {
            if (children.size() > 1 && children.get(children.size() - 1).placeholderType == PLACEHOLDER_LOAD_MORE_COMMENTS) {
                children.addAll(children.size() - 2, moreChildren);
            } else {
                children.addAll(moreChildren);
            }
        }
        childCount += moreChildren == null ? 0 : moreChildren.size();
        assertChildrenDepth();
    }

    public void addChild(Comment comment) {
        addChild(comment, 0);
        childCount++;
        assertChildrenDepth();
    }

    public void addChild(Comment comment, int position) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(position, comment);
        assertChildrenDepth();
    }

    private void assertChildrenDepth() {
        if (BuildConfig.DEBUG) {
            for (Comment child: children) {
                if (child.depth != depth + 1) {
                    throw new IllegalStateException("Child depth is not one more than parent depth");
                }
            }
        }
    }

    public ArrayList<String> getMoreChildrenIds() {
        return moreChildrenIds;
    }

    public void setMoreChildrenIds(ArrayList<String> moreChildrenIds) {
        this.moreChildrenIds = moreChildrenIds;
    }

    public boolean hasMoreChildrenIds() {
        return moreChildrenIds != null;
    }

    public void removeMoreChildrenIds() {
        moreChildrenIds.clear();
    }

    public int getPlaceholderType() {
        return placeholderType;
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

    public boolean isEdited() {
        return editedTimeMillis != 0;
    }

    public long getEditedTimeMillis() {
        return editedTimeMillis;
    }

    public Map<String, MediaMetadata> getMediaMetadataMap() {
        return mediaMetadataMap;
    }

    public void setMediaMetadataMap(Map<String, MediaMetadata> mediaMetadataMap) {
        this.mediaMetadataMap = mediaMetadataMap;
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
        parcel.writeString(authorFullName);
        parcel.writeString(authorFlair);
        parcel.writeString(authorFlairHTML);
        parcel.writeString(authorIconUrl);
        parcel.writeString(linkAuthor);
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
        parcel.writeInt(depth);
        parcel.writeInt(childCount);
        parcel.writeByte((byte) (collapsed ? 1 : 0));
        parcel.writeByte((byte) (hasReply ? 1 : 0));
        parcel.writeByte((byte) (scoreHidden ? 1 : 0));
        parcel.writeByte((byte) (saved ? 1 : 0));
        parcel.writeByte((byte) (sendReplies ? 1 : 0));
        parcel.writeByte((byte) (locked ? 1 : 0));
        parcel.writeByte((byte) (canModComment ? 1 : 0));
        parcel.writeByte((byte) (approved ? 1 : 0));
        parcel.writeLong(approvedAtUTC);
        parcel.writeString(approvedBy);
        parcel.writeByte((byte) (removed ? 1 : 0));
        parcel.writeByte((byte) (spam ? 1 : 0));
        parcel.writeByte((byte) (isExpanded ? 1 : 0));
        parcel.writeByte((byte) (hasExpandedBefore ? 1 : 0));
        parcel.writeLong(editedTimeMillis);
        parcel.writeByte((byte) (isFilteredOut ? 1 : 0));
        parcel.writeTypedList(children);
        parcel.writeStringList(moreChildrenIds);
        parcel.writeInt(placeholderType);
        parcel.writeByte((byte) (isLoadingMoreChildren ? 1 : 0));
        parcel.writeByte((byte) (loadMoreChildrenFailed ? 1 : 0));
        parcel.writeValue(mediaMetadataMap);
    }
}
