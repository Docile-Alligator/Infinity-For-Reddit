package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ml.docilealligator.infinityforreddit.network.SortTypeConverterFactory;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module(includes = AppModule.class)
abstract class NetworkModule {

    private static final String NAMED_BASE_OKHTTP = "base_okhttp_client";
    private static final String NAMED_BASE_RETROFIT = "base_retrofit_client";
    private static final String NAMED_DEFAULT_OKHTTP = "default_okhttp_client";

    @Provides
    static ConnectionPool provideConnectionPool() {
        return new ConnectionPool(0, 1, TimeUnit.NANOSECONDS);
    }

    @Provides
    @Singleton
    @Named(NAMED_BASE_OKHTTP)
    static OkHttpClient provideBaseOkhttp(ConnectionPool connectionPool) {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .build();
    }

    @Provides
    @Singleton
    @Named(NAMED_BASE_RETROFIT)
    static Retrofit provideBaseRetrofit(@Named(NAMED_BASE_OKHTTP) OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.API_BASE_URI)
                .client(okHttpClient)
                .addConverterFactory(SortTypeConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(GuavaCallAdapterFactory.create())
                .build();
    }

    @Provides
    @Named("access_token_authenticator")
    static Authenticator provideAccessTokenAuthenticator(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit,
                                                         RedditDataRoomDatabase accountRoomDatabase,
                                                         @Named("current_account") SharedPreferences currentAccountSharedPreferences) {
        return new AccessTokenAuthenticator(retrofit, accountRoomDatabase, currentAccountSharedPreferences);
    }

    @Provides
    @Named(NAMED_DEFAULT_OKHTTP)
    @Singleton
    static OkHttpClient provideOkHttpClient(@Named(NAMED_BASE_OKHTTP) OkHttpClient httpClient,
                                            @Named("access_token_authenticator") Authenticator authenticator) {
        return httpClient.newBuilder()
                .authenticator(authenticator)
                .build();
    }

    @Provides
    @Named("oauth_without_authenticator")
    @Singleton
    static Retrofit provideOauthWithoutAuthenticatorRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.OAUTH_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("no_oauth")
    static Retrofit provideRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.OAUTH_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("oauth")
    static Retrofit provideOAuthRetrofit(@Named(NAMED_DEFAULT_OKHTTP) OkHttpClient okHttpClient,
                                         @Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    @Named("rpan")
    static OkHttpClient provideRPANOkHttpClient(@Named(NAMED_BASE_OKHTTP) OkHttpClient httpClient) {
        return httpClient;
    }

    @Provides
    @Named("upload_media")
    @Singleton
    static Retrofit provideUploadMediaRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.API_UPLOAD_MEDIA_URI)
                .build();
    }

    @Provides
    @Named("upload_video")
    @Singleton
    static Retrofit provideUploadVideoRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.API_UPLOAD_VIDEO_URI)
                .build();
    }

    @Provides
    @Named("download_media")
    @Singleton
    static Retrofit provideDownloadRedditVideoRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl("http://localhost/")
                .build();
    }

    @Provides
    @Named("gfycat")
    @Singleton
    static Retrofit provideGfycatRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.GFYCAT_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("RedgifsAccessTokenAuthenticator")
    static Interceptor redgifsAccessTokenAuthenticator(
            @Named("current_account") SharedPreferences currentAccountSharedPreferences) {
        return new RedgifsAccessTokenAuthenticator(currentAccountSharedPreferences);
    }

    @Provides
    @Named("redgifs")
    @Singleton
    static Retrofit provideRedgifsRetrofit(@Named("RedgifsAccessTokenAuthenticator") Interceptor accessTokenAuthenticatorInterceptor,
                                           @Named(NAMED_BASE_OKHTTP) OkHttpClient httpClient,
                                           @Named(NAMED_BASE_RETROFIT) Retrofit retrofit,
                                           ConnectionPool connectionPool) {
        OkHttpClient.Builder okHttpClientBuilder = httpClient.newBuilder()
                .addInterceptor(chain -> chain.proceed(
                        chain.request()
                                .newBuilder()
                                .header("User-Agent", APIUtils.USER_AGENT)
                                .build()
                ))
                .addInterceptor(accessTokenAuthenticatorInterceptor)
                .connectionPool(connectionPool);

        return retrofit.newBuilder()
                .baseUrl(APIUtils.REDGIFS_API_BASE_URI)
                .client(okHttpClientBuilder.build())
                .build();
    }

    @Provides
    @Named("imgur")
    @Singleton
    static Retrofit provideImgurRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.IMGUR_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("pushshift")
    @Singleton
    static Retrofit providePushshiftRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.PUSHSHIFT_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("reveddit")
    @Singleton
    static Retrofit provideRevedditRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.REVEDDIT_API_BASE_URI)
                .build();
    }

    @Provides
    @Named("vReddIt")
    @Singleton
    static Retrofit provideVReddItRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl("http://localhost/")
                .build();
    }

    @Provides
    @Named("strapi")
    @Singleton
    static Retrofit provideStrapiRetrofit(@Named(NAMED_DEFAULT_OKHTTP) OkHttpClient okHttpClient,
                                          @Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.STRAPI_BASE_URI)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Named("streamable")
    @Singleton
    static Retrofit provideStreamableRetrofit(@Named(NAMED_BASE_RETROFIT) Retrofit retrofit) {
        return retrofit.newBuilder()
                .baseUrl(APIUtils.STREAMABLE_API_BASE_URI)
                .build();
    }
}
