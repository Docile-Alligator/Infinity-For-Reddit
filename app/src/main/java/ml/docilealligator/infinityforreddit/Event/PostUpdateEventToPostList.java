package ml.docilealligator.infinityforreddit.Event;

import ml.docilealligator.infinityforreddit.Post.Post;

public class PostUpdateEventToPostList {
    public final Post post;
    public final int positionInList;

    public PostUpdateEventToPostList(Post post, int positionInList) {
        this.post = post;
        this.positionInList = positionInList;
    }
}
