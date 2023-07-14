package ml.ino6962.postinfinityforreddit.events;

import ml.ino6962.postinfinityforreddit.post.Post;

public class PostUpdateEventToPostDetailFragment {
    public final Post post;

    public PostUpdateEventToPostDetailFragment(Post post) {
        this.post = post;
    }
}
