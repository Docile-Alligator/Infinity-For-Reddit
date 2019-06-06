package ml.docilealligator.infinityforreddit;

public class PostUpdateEventToDetailActivity {
    public final String postId;
    public final int voteType;

    public PostUpdateEventToDetailActivity(String postId, int voteType) {
        this.postId = postId;
        this.voteType = voteType;
    }
}
