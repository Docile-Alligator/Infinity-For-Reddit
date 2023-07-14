package ml.ino6962.postinfinityforreddit.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ml.ino6962.postinfinityforreddit.MaterialYouWorker;
import ml.ino6962.postinfinityforreddit.utils.SharedPreferencesUtils;

public class WallpaperChangeReceiver extends BroadcastReceiver {
    private SharedPreferences sharedPreferences;

    public WallpaperChangeReceiver(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (sharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_MATERIAL_YOU, false)) {
            OneTimeWorkRequest materialYouRequest = OneTimeWorkRequest.from(MaterialYouWorker.class);
            WorkManager.getInstance(context).enqueueUniqueWork(MaterialYouWorker.UNIQUE_WORKER_NAME,
                    ExistingWorkPolicy.REPLACE, materialYouRequest);
        }
    }
}
