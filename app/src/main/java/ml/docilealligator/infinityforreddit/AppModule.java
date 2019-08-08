package ml.docilealligator.infinityforreddit;

import android.app.Application;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
class AppModule {
    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides @Named("oauth")
    @Singleton
    Retrofit provideOauthRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(RedditUtils.OAUTH_API_BASE_URI)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides @Named("no_oauth")
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(RedditUtils.API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides @Named("upload_media")
    @Singleton
    Retrofit provideUploadMediaRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(RedditUtils.API_UPLOAD_MEDIA_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides @Named("upload_video")
    @Singleton
    Retrofit provideUploadVideoRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(RedditUtils.API_UPLOAD_VIDEO_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(@Named("no_oauth") Retrofit retrofit, RedditDataRoomDatabase accountRoomDatabase) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.authenticator(new AccessTokenAuthenticator(retrofit, accountRoomDatabase));
        return okHttpClientBuilder.build();
    }

    @Provides
    @Singleton
    RedditDataRoomDatabase provideRedditDataRoomDatabase() {
        return RedditDataRoomDatabase.getDatabase(mApplication);
    }
}
