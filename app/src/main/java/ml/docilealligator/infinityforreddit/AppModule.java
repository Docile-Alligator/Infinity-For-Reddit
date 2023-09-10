package ml.docilealligator.infinityforreddit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LoopAvailableExoCreator;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.videoautoplay.Config;
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator;
import ml.docilealligator.infinityforreddit.videoautoplay.MediaSourceBuilder;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroExo;

@Module
abstract class AppModule {

    @Binds
    abstract Context providesContext(Application application);

    @Provides
    @Singleton
    static RedditDataRoomDatabase provideRedditDataRoomDatabase(Application application) {
        return RedditDataRoomDatabase.create(application);
    }

    @Provides
    @Named("default")
    @Singleton
    static SharedPreferences provideSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Named("light_theme")
    @Singleton
    static SharedPreferences provideLightThemeSharedPreferences(Application application) {
        return application.getSharedPreferences(CustomThemeSharedPreferencesUtils.LIGHT_THEME_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("dark_theme")
    @Singleton
    static SharedPreferences provideDarkThemeSharedPreferences(Application application) {
        return application.getSharedPreferences(CustomThemeSharedPreferencesUtils.DARK_THEME_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("amoled_theme")
    @Singleton
    static SharedPreferences provideAmoledThemeSharedPreferences(Application application) {
        return application.getSharedPreferences(CustomThemeSharedPreferencesUtils.AMOLED_THEME_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("sort_type")
    static SharedPreferences provideSortTypeSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.SORT_TYPE_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("post_layout")
    static SharedPreferences providePostLayoutSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.POST_LAYOUT_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("post_feed_scrolled_position_cache")
    static SharedPreferences providePostFeedScrolledPositionSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("main_activity_tabs")
    static SharedPreferences provideMainActivityTabsSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.MAIN_PAGE_TABS_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("nsfw_and_spoiler")
    static SharedPreferences provideNsfwAndSpoilerSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.NSFW_AND_SPOILER_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("bottom_app_bar")
    static SharedPreferences provideBottoappBarSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.BOTTOM_APP_BAR_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("post_history")
    static SharedPreferences providePostHistorySharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.POST_HISTORY_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("current_account")
    static SharedPreferences provideCurrentAccountSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.CURRENT_ACCOUNT_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("navigation_drawer")
    static SharedPreferences provideNavigationDrawerSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.NAVIGATION_DRAWER_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("post_details")
    static SharedPreferences providePostDetailsSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.POST_DETAILS_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("security")
    @Singleton
    static SharedPreferences provideSecuritySharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.SECURITY_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Named("internal")
    @Singleton
    static SharedPreferences provideInternalSharedPreferences(Application application) {
        return application.getSharedPreferences(SharedPreferencesUtils.INTERNAL_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    static CustomThemeWrapper provideCustomThemeWrapper(@Named("light_theme") SharedPreferences lightThemeSharedPreferences,
                                                 @Named("dark_theme") SharedPreferences darkThemeSharedPreferences,
                                                 @Named("amoled_theme") SharedPreferences amoledThemeSharedPreferences) {
        return new CustomThemeWrapper(lightThemeSharedPreferences, darkThemeSharedPreferences, amoledThemeSharedPreferences);
    }

    @Provides
    @Named("app_cache_dir")
    static File providesAppCache(Application application) {
        return application.getCacheDir();
    }
    @Provides
    @Named("exo_player_cache")
    static File providesExoPlayerCache(@Named("app_cache_dir") File appCache) {
        return new File(appCache, "/exoplayer");
    }

    @Provides
    static StandaloneDatabaseProvider provideExoDatabaseProvider(Application application) {
        return new StandaloneDatabaseProvider(application);
    }

    @Provides
    @Singleton
    static SimpleCache provideSimpleCache(StandaloneDatabaseProvider standaloneDatabaseProvider,
                                          @Named("exo_player_cache") File exoPlayerCache) {
        return new SimpleCache(exoPlayerCache,
                new LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024),
                standaloneDatabaseProvider);
    }

    @Provides
    static Config providesMediaConfig(Application application, SimpleCache simpleCache) {
        return new Config.Builder(application)
                .setMediaSourceBuilder(MediaSourceBuilder.DEFAULT)
                .setCache(simpleCache)
                .build();
    }

    @Provides
    static ToroExo providesToroExo(Application application) {
        return ToroExo.with(application);
    }

    @Provides
    @Singleton
    static ExoCreator provideExoCreator(Config config,
                                 ToroExo toroExo,
                                 @Named("default") SharedPreferences sharedPreferences) {
        return new LoopAvailableExoCreator(toroExo, config, sharedPreferences);
    }

    @Provides
    @Singleton
    static Executor provideExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
