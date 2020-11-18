package ml.docilealligator.infinityforreddit.events;

import ml.docilealligator.infinityforreddit.post.Post;

public class PostUpdateEventToPostList {
    public final Post post;
    public final int positionInList;

    public PostUpdateEventToPostList(Post post, int positionInList) {
        this.post = post;
        this.positionInList = positionInList;
    }
}
