package ml.docilealligator.infinityforreddit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LoopAvailableExoCreator;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.videoautoplay.Config;
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator;
import ml.docilealligator.infinityforreddit.videoautoplay.MediaSourceBuilder;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroExo;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
class AppModule {

    private final Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    Context providesContext() {
        return mApplication;
    }

    @Provides
    Application providesApplication() {
        return mApplication;
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
    @Named("current_account")
    SharedPreferences provideCurrentAccountSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.CURRENT_ACCOUNT_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("navigation_drawer")
    SharedPreferences provideNavigationDrawerSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.NAVIGATION_DRAWER_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("post_details")
    SharedPreferences providePostDetailsSharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.POST_DETAILS_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("security")
    @Singleton
    SharedPreferences provideSecuritySharedPreferences() {
        return mApplication.getSharedPreferences(SharedPreferencesUtils.SECURITY_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
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
    SimpleCache provideSimpleCache() {
        return new SimpleCache(new File(mApplication.getCacheDir(), "/exoplayer"),
                new LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024), new ExoDatabaseProvider(mApplication));
    }

    @Provides
    @Singleton
    ExoCreator provideExoCreator(SimpleCache simpleCache, @Named("default") SharedPreferences sharedPreferences) {
        Config config = new Config.Builder(mApplication).setMediaSourceBuilder(MediaSourceBuilder.DEFAULT).setCache(simpleCache)
                .build();
        return new LoopAvailableExoCreator(ToroExo.with(mApplication), config, sharedPreferences);
    }

    @Provides
    @Singleton
    Executor provideExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
