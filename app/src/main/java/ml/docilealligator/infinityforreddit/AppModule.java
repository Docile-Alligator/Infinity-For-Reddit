package ml.docilealligator.infinityforreddit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

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

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(@Named("no_oauth") Retrofit retrofit, @Named("auth_info") SharedPreferences sharedPreferences) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.authenticator(new AccessTokenAuthenticator(retrofit, sharedPreferences));
        return okHttpClientBuilder.build();
    }

    @Provides @Named("auth_info")
    @Singleton
    SharedPreferences provideAuthInfoSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE);
    }

    @Provides @Named("user_info")
    SharedPreferences provideUserInfoSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE);
    }
}
