package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.util.Locale;

import retrofit2.Retrofit;

class CommentDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private Locale locale;
    private String username;

    private CommentDataSource commentDataSource;
    private MutableLiveData<CommentDataSource> commentDataSourceLiveData;

    CommentDataSourceFactory(Retrofit retrofit, Locale locale, String username) {
        this.retrofit = retrofit;
        this.locale = locale;
        this.username = username;
        commentDataSourceLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        commentDataSource = new CommentDataSource(retrofit, locale, username);
        commentDataSourceLiveData.postValue(commentDataSource);
        return commentDataSource;
    }

    public MutableLiveData<CommentDataSource> getCommentDataSourceLiveData() {
        return commentDataSourceLiveData;
    }

    CommentDataSource getCommentDataSource() {
        return commentDataSource;
    }
}
