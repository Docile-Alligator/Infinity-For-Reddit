package ml.docilealligator.infinityforreddit.events;

import ml.docilealligator.infinityforreddit.post.Post;

public class PostUpdateEventToPostDetailFragment {
    public final Post post;

    public PostUpdateEventToPostDetailFragment(Post post) {
        this.post = post;
    }
}
