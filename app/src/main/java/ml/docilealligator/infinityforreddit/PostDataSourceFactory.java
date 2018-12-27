package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.PageKeyedDataSource;

import java.util.Locale;

import retrofit2.Retrofit;

class PostDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String accessToken;
    private Locale locale;
    private boolean isBestPost;
    private String subredditName;

    private MutableLiveData<PageKeyedDataSource<String, Post>> mutableLiveData;

    PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.isBestPost = isBestPost;
        mutableLiveData = new MutableLiveData<>();
    }

    PostDataSourceFactory(Retrofit retrofit, Locale locale, boolean isBestPost, String subredditName) {
        this.retrofit = retrofit;
        this.locale = locale;
        this.isBestPost = isBestPost;
        mutableLiveData = new MutableLiveData<>();
        this.subredditName = subredditName;
    }

    @Override
    public DataSource create() {
        PostDataSource postDataSource;
        if(isBestPost) {
            postDataSource = new PostDataSource(retrofit, accessToken, locale, isBestPost);
        } else {
            postDataSource = new PostDataSource(retrofit, locale, isBestPost, subredditName);
        }
        mutableLiveData.postValue(postDataSource);
        return postDataSource;
    }

    public MutableLiveData<PageKeyedDataSource<String, Post>> getMutableLiveData() {
        return mutableLiveData;
    }
}
