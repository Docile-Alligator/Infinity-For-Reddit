package ml.docilealligator.infinityforreddit.post;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({LoadingMorePostsStatus.LOADING, LoadingMorePostsStatus.FAILED, LoadingMorePostsStatus.LOADED,
        LoadingMorePostsStatus.NO_MORE_POSTS,
        LoadingMorePostsStatus.NOT_LOADING})
@Retention(RetentionPolicy.SOURCE)
public @interface LoadingMorePostsStatus {
    int LOADING = 0;
    int FAILED = 1;
    int LOADED = 2;
    int NO_MORE_POSTS = 3;
    int NOT_LOADING = 4;
}
