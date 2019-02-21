package ml.docilealligator.infinityforreddit;

import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.util.Locale;

import retrofit2.Retrofit;

public class CommentDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private Locale locale;
    private String subredditNamePrefixed;
    private String article;
    private String comment;
    private boolean isPost;
    private CommentDataSource.OnCommentFetchedCallback onCommentFetchedCallback;

    private CommentDataSource commentDataSource;
    private MutableLiveData<CommentDataSource> commentDataSourceMutableLiveData;

    CommentDataSourceFactory(Retrofit retrofit, Locale locale, String subredditNamePrefixed,
                             String article, String comment, boolean isPost,
                             CommentDataSource.OnCommentFetchedCallback onCommentFetchedCallback) {
        this.retrofit = retrofit;
        this.locale = locale;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.article = article;
        this.comment = comment;
        this.isPost = isPost;
        commentDataSourceMutableLiveData = new MutableLiveData<>();
        this.onCommentFetchedCallback = onCommentFetchedCallback;
    }

    @Override
    public DataSource create() {
        commentDataSource = new CommentDataSource(retrofit, locale, subredditNamePrefixed, article, comment, isPost, onCommentFetchedCallback);
        commentDataSourceMutableLiveData.postValue(commentDataSource);
        return commentDataSource;
    }

    public MutableLiveData<CommentDataSource> getCommentDataSourceMutableLiveData() {
        return commentDataSourceMutableLiveData;
    }

    CommentDataSource getCommentDataSource() {
        return commentDataSource;
    }
}
