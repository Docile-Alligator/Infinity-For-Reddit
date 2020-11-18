package ml.docilealligator.infinityforreddit.events;

import ml.docilealligator.infinityforreddit.post.Post;

public class PostUpdateEventToDetailActivity {
    public final Post post;

    public PostUpdateEventToDetailActivity(Post post) {
        this.post = post;
    }
}
