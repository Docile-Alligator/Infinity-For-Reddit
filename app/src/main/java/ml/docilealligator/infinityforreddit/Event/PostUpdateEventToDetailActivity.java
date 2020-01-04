package ml.docilealligator.infinityforreddit.Event;

import ml.docilealligator.infinityforreddit.Post.Post;

public class PostUpdateEventToDetailActivity {
    public final Post post;

    public PostUpdateEventToDetailActivity(Post post) {
        this.post = post;
    }
}
