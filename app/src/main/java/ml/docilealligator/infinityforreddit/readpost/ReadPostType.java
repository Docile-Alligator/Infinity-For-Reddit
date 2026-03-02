package ml.docilealligator.infinityforreddit.readpost;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ReadPostType.READ_POSTS, ReadPostType.ANONYMOUS_SAVED_POSTS, ReadPostType.ANONYMOUS_UPVOTED_POSTS,
        ReadPostType.ANONYMOUS_DOWNVOTED_POSTS})
@Retention(RetentionPolicy.SOURCE)
public @interface ReadPostType {
    int READ_POSTS = 0;
    int ANONYMOUS_UPVOTED_POSTS = 1;
    int ANONYMOUS_DOWNVOTED_POSTS = 2;
    int ANONYMOUS_HIDDEN_POSTS = 3;
    int ANONYMOUS_SAVED_POSTS = 4;
}
