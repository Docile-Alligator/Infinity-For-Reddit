package ml.ino6962.postinfinityforreddit.events;

import ml.ino6962.postinfinityforreddit.post.Post;

public class PostUpdateEventToPostList {
    public final Post post;
    public final int positionInList;

    public PostUpdateEventToPostList(Post post, int positionInList) {
        this.post = post;
        this.positionInList = positionInList;
    }
}
