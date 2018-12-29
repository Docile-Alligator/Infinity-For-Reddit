package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;

import java.util.Locale;

import retrofit2.Retrofit;

class PostDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String accessToken;
    private Locale locale;
    private String subredditName;

    private PostDataSource postDataSource;
    private MutableLiveData<PostDataSource> postDataSourceLiveData;

    PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        postDataSourceLiveData = new MutableLiveData<>();

        if(isBestPost) {
            postDataSource = new PostDataSource(retrofit, accessToken, locale, isBestPost);
        } else {
            postDataSource = new PostDataSource(retrofit, locale, isBestPost, subredditName);
        }
    }

    PostDataSourceFactory(Retrofit retrofit, Locale locale, boolean isBestPost, String subredditName) {
        this.retrofit = retrofit;
        this.locale = locale;
        this.subredditName = subredditName;
        postDataSourceLiveData = new MutableLiveData<>();

        if(isBestPost) {
            postDataSource = new PostDataSource(retrofit, accessToken, locale, isBestPost);
        } else {
            postDataSource = new PostDataSource(retrofit, locale, isBestPost, subredditName);
        }
    }

    @Override
    public DataSource create() {
        postDataSourceLiveData.postValue(postDataSource);
        return postDataSource;
    }

    public MutableLiveData<PostDataSource> getPostDataSourceLiveData() {
        return postDataSourceLiveData;
    }

    PostDataSource getPostDataSource() {
        return postDataSource;
    }
}
