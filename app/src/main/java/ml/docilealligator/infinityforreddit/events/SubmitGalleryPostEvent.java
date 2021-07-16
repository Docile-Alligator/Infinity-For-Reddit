package ml.docilealligator.infinityforreddit.events;

import ml.docilealligator.infinityforreddit.post.Post;

public class SubmitGalleryPostEvent {
    public boolean postSuccess;
    public Post post;
    public String errorMessage;

    public SubmitGalleryPostEvent(boolean postSuccess, Post post, String errorMessage) {
        this.postSuccess = postSuccess;
        this.post = post;
        this.errorMessage = errorMessage;
    }
}
