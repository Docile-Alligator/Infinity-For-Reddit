package ml.docilealligator.infinityforreddit;

class CommentData {
    private String fullName;
    private String author;
    private String commentTime;
    private String commentContent;
    private int score;
    private int voteType;
    private boolean isSubmitter;
    private String permalink;
    private int depth;
    private boolean collapsed;
    private boolean hasReply;
    private boolean scoreHidden;

    CommentData(String fullName, String author, String commentTime, String commentContent, int score,
                boolean isSubmitter, String permalink, int depth, boolean collapsed, boolean hasReply,
                boolean scoreHidden) {
        this.fullName = fullName;
        this.author = author;
        this.commentTime = commentTime;
        this.commentContent = commentContent;
        this.score = score;
        this.isSubmitter = isSubmitter;
        this.permalink = RedditUtils.API_BASE_URI + permalink;
        this.depth = depth;
        this.collapsed = collapsed;
        this.hasReply = hasReply;
        this.scoreHidden = scoreHidden;
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

    public boolean isHasReply() {
        return hasReply;
    }

    public void setHasReply(boolean hasReply) {
        this.hasReply = hasReply;
    }

    public boolean isScoreHidden() {
        return scoreHidden;
    }

    public int getVoteType() {
        return voteType;
    }

    public void setVoteType(int voteType) {
        this.voteType = voteType;
    }
}
