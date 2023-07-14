package ml.docilealligator.infinityforreddit;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({LoadingMorePostsStatus.LOADING, LoadingMorePostsStatus.FAILED, LoadingMorePostsStatus.NO_MORE_POSTS,
        LoadingMorePostsStatus.NOT_LOADING})
@Retention(RetentionPolicy.SOURCE)
public @interface LoadingMorePostsStatus {
    int LOADING = 0;
    int FAILED = 1;
    int NO_MORE_POSTS = 2;
    int NOT_LOADING = 3;
}
