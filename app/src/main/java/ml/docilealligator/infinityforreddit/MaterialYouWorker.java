package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.MaterialYouUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class MaterialYouWorker extends Worker {
    public static final String UNIQUE_WORKER_NAME = "MYWT";

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
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
    private Context context;

    public MaterialYouWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        ((Infinity) context.getApplicationContext()).getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_MATERIAL_YOU, false)) {
            MaterialYouUtils.changeThemeSync(context, redditDataRoomDatabase,
                    customThemeWrapper, lightThemeSharedPreferences, darkThemeSharedPreferences,
                    amoledThemeSharedPreferences);
        }

        return Result.success();
    }
}
