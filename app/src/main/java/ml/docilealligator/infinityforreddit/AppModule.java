package ml.docilealligator.infinityforreddit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import im.ene.toro.exoplayer.Config;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.exoplayer.MediaSourceBuilder;
import im.ene.toro.exoplayer.ToroExo;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
class AppModule {
    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Named("oauth")
    @Singleton
    Retrofit provideOauthRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.OAUTH_API_BASE_URI)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("oauth_without_authenticator")
    @Singleton
    Retrofit provideOauthWithoutAuthenticatorRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.OAUTH_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("no_oauth")
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("upload_media")
    @Singleton
    Retrofit provideUploadMediaRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.API_UPLOAD_MEDIA_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("upload_video")
    @Singleton
    Retrofit provideUploadVideoRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.API_UPLOAD_VIDEO_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("download_media")
    @Singleton
    Retrofit provideDownloadRedditVideoRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("http://localhost/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("gfycat")
    @Singleton
    Retrofit provideGfycatRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.GFYCAT_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("redgifs")
    @Singleton
    Retrofit provideRedgifsRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.REDGIFS_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("imgur")
    @Singleton
    Retrofit provideImgurRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.IMGUR_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Named("pushshift")
    @Singleton
    Retrofit providePushshiftRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(APIUtils.PUSHSHIFT_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(@Named("no_oauth") Retrofit retrofit, RedditDataRoomDatabase accountRoomDatabase) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.authenticator(new AccessTokenAuthenticator(retrofit, accountRoomDatabase))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS));
        return okHttpClientBuilder.build();
    }

    @Provides
    @Singleton
    RedditDataRoomDatabase provideRedditDataRoomDatabase() {
        return RedditDataRoomDatabase.getDatabase(mApplication);
    }

    @Provides
    @Named("default")
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

    @Provides
    @Named("light_theme")
    @Singleton
    SharedPreferences provideLightThemeSharedPreferences() {
        return mApplication.getSharedPreferences(CustomThemeSharedPreferencesUtils.LIGHT_THEME_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("dark_theme")
    @Singleton
    SharedPreferences provideDarkThemeSharedPreferences() {
        return mApplication.getSharedPreferences(CustomThemeSharedPreferencesUtils.DARK_THEME_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("amoled_theme")
    @Singleton
    SharedPreferences provideAmoledThemeSharedPreferences() {
        return mApplication.getSharedPreferences(CustomThemeSharedPreferencesUtils.AMOLED_THEME_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("sort_type")
    SharedPreferences provideSortTypeSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.SORT_TYPE_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("post_layout")
    SharedPreferences providePostLayoutSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.POST_LAYOUT_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("post_feed_scrolled_position_cache")
    SharedPreferences providePostFeedScrolledPositionSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("main_activity_tabs")
    SharedPreferences provideMainActivityTabsSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.MAIN_PAGE_TABS_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("nsfw_and_spoiler")
    SharedPreferences provideNsfwAndSpoilerSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.NSFW_AND_SPOILER_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("bottom_app_bar")
    SharedPreferences provideBottomAppBarSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.BOTTOM_APP_BAR_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("post_history")
    SharedPreferences providePostHistorySharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.POST_HISTORY_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    CustomThemeWrapper provideCustomThemeWrapper(@Named("light_theme") SharedPreferences lightThemeSharedPreferences,
                                                 @Named("dark_theme") SharedPreferences darkThemeSharedPreferences,
                                                 @Named("amoled_theme") SharedPreferences amoledThemeSharedPreferences) {
        return new CustomThemeWrapper(lightThemeSharedPreferences, darkThemeSharedPreferences, amoledThemeSharedPreferences);
    }

    @Provides
    @Singleton
    ExoCreator provideExoCreator() {
        SimpleCache cache = new SimpleCache(new File(mApplication.getCacheDir(), "/toro_cache"),
                new LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024), new ExoDatabaseProvider(mApplication));
        Config config = new Config.Builder(mApplication).setMediaSourceBuilder(MediaSourceBuilder.LOOPING).setCache(cache)
                .build();
        return ToroExo.with(mApplication).getCreator(config);
    }
}
