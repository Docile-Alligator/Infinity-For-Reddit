package ml.docilealligator.infinityforreddit.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.MaterialYouUtils;

public class MaterialYouService extends IntentService {

    @Inject
    @Named("light_theme")
    SharedPreferences lightThemeSharedPreferences;
    @Inject
    @Named("dark_theme")
    SharedPreferences darkThemeSharedPreferences;
    @Inject
    @Named("amoled_theme")
    SharedPreferences amoledThemeSharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor executor;

    public MaterialYouService() {
        super("MaterialYouService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        MaterialYouUtils.changeTheme(this, executor, new Handler(), redditDataRoomDatabase,
                customThemeWrapper, lightThemeSharedPreferences, darkThemeSharedPreferences,
                amoledThemeSharedPreferences);
    }
}