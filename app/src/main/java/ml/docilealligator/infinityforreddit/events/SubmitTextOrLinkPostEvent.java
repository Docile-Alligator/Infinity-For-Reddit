package ml.ino6962.postinfinityforreddit.events;

import ml.ino6962.postinfinityforreddit.post.Post;

public class SubmitTextOrLinkPostEvent {
    public boolean postSuccess;
    public Post post;
    public String errorMessage;

    public SubmitTextOrLinkPostEvent(boolean postSuccess, Post post, String errorMessage) {
        this.postSuccess = postSuccess;
        this.post = post;
        this.errorMessage = errorMessage;
    }
}
