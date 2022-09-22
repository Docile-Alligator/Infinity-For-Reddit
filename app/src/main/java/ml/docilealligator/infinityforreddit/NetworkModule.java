package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module(includes = AppModule.class)
abstract class NetworkModule {

    @Provides
    @Singleton
    static OkHttpClient providesBaseOkhttp() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    static Retrofit providesBaseRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.API_BASE_URI)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(GuavaCallAdapterFactory.create())
                .build();
    }

    @Provides
    static ConnectionPool providesConnectionPool() {
        return new ConnectionPool(0, 1, TimeUnit.NANOSECONDS);
    }

    @Provides
    @Named("no_oauth")
    static Retrofit provideRetrofit(Retrofit retrofit) {
        return retrofit;
    }

    @Provides
    @Named("oauth")
    static Retrofit providesOAuthetrofit(Retrofit retrofit) {
        return retrofit;
    }

    @Provides
    @Named("default")
    @Reusable
    static OkHttpClient provideOkHttpClient(OkHttpClient httpClient,
                                            Retrofit retrofit,
                                            RedditDataRoomDatabase accountRoomDatabase,
                                            @Named("current_account") SharedPreferences currentAccountSharedPreferences,
                                            ConnectionPool connectionPool) {
        return httpClient.newBuilder()
                .authenticator(new AccessTokenAuthenticator(retrofit, accountRoomDatabase, currentAccountSharedPreferences))
                .connectionPool(connectionPool)
                .build();
    }

    @Provides
    @Named("rpan")
    static OkHttpClient provideRPANOkHttpClient(OkHttpClient httpClient) {
        return httpClient;
    }

    @Provides
    @Named("oauth_without_authenticator")
    @Reusable
    static Retrofit provideOauthWithoutAuthenticatorRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.OAUTH_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("upload_media")
    @Reusable
    static Retrofit provideUploadMediaRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.API_UPLOAD_MEDIA_URI)
                .build();
    }

    @Provides
    @Named("upload_video")
    @Reusable
    static Retrofit provideUploadVideoRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.API_UPLOAD_VIDEO_URI)
                .build();
    }

    @Provides
    @Named("download_media")
    @Reusable
    static Retrofit provideDownloadRedditVideoRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl("http://localhost/")
                .build();
    }

    @Provides
    @Named("gfycat")
    @Reusable
    static Retrofit provideGfycatRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.GFYCAT_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("redgifs")
    @Reusable
    static Retrofit provideRedgifsRetrofit(@Named("current_account") SharedPreferences currentAccountSharedPreferences,
                                           OkHttpClient httpClient,
                                           Retrofit retrofit,
                                           ConnectionPool connectionPool) {
        OkHttpClient.Builder okHttpClientBuilder = httpClient.newBuilder()
                .addInterceptor(chain -> chain.proceed(
                        chain.request()
                                .newBuilder()
                                .header("User-Agent", APIUtils.USER_AGENT)
                                .build()
                ))
                .addInterceptor(new RedgifsAccessTokenAuthenticator(currentAccountSharedPreferences))
                .connectionPool(connectionPool);

        return retrofit.newBuilder()
                .baseUrl(APIUtils.REDGIFS_API_BASE_URI)
                .client(okHttpClientBuilder.build())
                .build();
    }

    @Provides
    @Named("imgur")
    @Reusable
    static Retrofit provideImgurRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.IMGUR_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("pushshift")
    @Reusable
    static Retrofit providePushshiftRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.PUSHSHIFT_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("reveddit")
    @Reusable
    static Retrofit provideRevedditRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.REVEDDIT_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("vReddIt")
    @Reusable
    static Retrofit provideVReddItRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl("http://localhost/")
                .build();
    }

    @Provides
    @Named("strapi")
    @Reusable
    static Retrofit providestrapiRetrofit(@Named("default") OkHttpClient okHttpClient,
                                          Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.STRAPI_BASE_URI)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Named("streamable")
    @Reusable
    static Retrofit provideStreamableRetrofit(Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.STREAMABLE_API_BASE_URI)
                .build();
    }
}
