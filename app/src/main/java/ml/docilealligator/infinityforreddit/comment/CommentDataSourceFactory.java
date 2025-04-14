package ml.docilealligator.infinityforreddit.comment;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.thing.SortType;
import retrofit2.Retrofit;

class CommentDataSourceFactory extends DataSource.Factory {
    private final Executor executor;
    private final Handler handler;
    private final Retrofit retrofit;
    private final String accessToken;
    private final String accountName;
    private final String username;
    private SortType sortType;
    private final boolean areSavedComments;

    private CommentDataSource commentDataSource;
    private final MutableLiveData<CommentDataSource> commentDataSourceLiveData;

    CommentDataSourceFactory(Executor executor, Handler handler, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                             String username, SortType sortType,
                             boolean areSavedComments) {
        this.executor = executor;
        this.handler = handler;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.username = username;
        this.sortType = sortType;
        this.areSavedComments = areSavedComments;
        commentDataSourceLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        commentDataSource = new CommentDataSource(executor, handler, retrofit, accessToken, accountName, username,
                sortType, areSavedComments);
        commentDataSourceLiveData.postValue(commentDataSource);
        return commentDataSource;
    }

    public MutableLiveData<CommentDataSource> getCommentDataSourceLiveData() {
        return commentDataSourceLiveData;
    }

    CommentDataSource getCommentDataSource() {
        return commentDataSource;
    }

    void changeSortType(SortType sortType) {
        this.sortType = sortType;
    }
}
