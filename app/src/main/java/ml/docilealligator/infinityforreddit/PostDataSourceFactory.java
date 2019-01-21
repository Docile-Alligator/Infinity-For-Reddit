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
    private int postType;

    private PostDataSource postDataSource;
    private MutableLiveData<PostDataSource> postDataSourceLiveData;

    PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, int postType) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
    }

    PostDataSourceFactory(Retrofit retrofit, Locale locale, String subredditName, int postType) {
        this.retrofit = retrofit;
        this.locale = locale;
        this.subredditName = subredditName;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
    }

    @Override
    public DataSource create() {
        if(postType == PostDataSource.TYPE_FRONT_PAGE) {
            postDataSource = new PostDataSource(retrofit, accessToken, locale, postType);
        } else {
            postDataSource = new PostDataSource(retrofit, locale, subredditName, postType);
        }

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
