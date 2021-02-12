package ml.docilealligator.infinityforreddit.events;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.post.Post;

public class ProvidePostListToViewPostDetailActivityEvent {
    public long postFragmentId;
    public ArrayList<Post> posts;

    public ProvidePostListToViewPostDetailActivityEvent(long postFragmentId, ArrayList<Post> posts) {
        this.postFragmentId = postFragmentId;
        this.posts = posts;
    }
}
