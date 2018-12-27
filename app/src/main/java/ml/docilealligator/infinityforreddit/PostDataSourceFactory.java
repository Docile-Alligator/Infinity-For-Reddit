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

    private MutableLiveData<PageKeyedDataSource<String, Post>> mutableLiveData;

    PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.isBestPost = isBestPost;
        mutableLiveData = new MutableLiveData<>();
    }

    @Override
    public DataSource create() {
        PostDataSource postDataSource = new PostDataSource(retrofit, accessToken, locale, isBestPost);
        mutableLiveData.postValue(postDataSource);
        return postDataSource;
    }

    public MutableLiveData<PageKeyedDataSource<String, Post>> getMutableLiveData() {
        return mutableLiveData;
    }
}
