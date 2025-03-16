package ml.docilealligator.infinityforreddit.comment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import ml.docilealligator.infinityforreddit.thing.SortType;
import retrofit2.Retrofit;

class CommentDataSourceFactory extends DataSource.Factory {
    private final Retrofit retrofit;
    private final String accessToken;
    private final String accountName;
    private final String username;
    private SortType sortType;
    private final boolean areSavedComments;

    private CommentDataSource commentDataSource;
    private final MutableLiveData<CommentDataSource> commentDataSourceLiveData;

    CommentDataSourceFactory(Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                             String username, SortType sortType,
                             boolean areSavedComments) {
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
        commentDataSource = new CommentDataSource(retrofit, accessToken, accountName, username,
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
