package ml.docilealligator.infinityforreddit;

public class VoteEventToDetailActivity {
    public final String postId;
    public final int voteType;

    public VoteEventToDetailActivity(String postId, int voteType) {
        this.postId = postId;
        this.voteType = voteType;
    }
}
