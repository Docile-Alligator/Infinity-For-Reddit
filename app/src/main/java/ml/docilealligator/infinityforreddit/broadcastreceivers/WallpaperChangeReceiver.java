package ml.docilealligator.infinityforreddit.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import ml.docilealligator.infinityforreddit.services.MaterialYouService;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class WallpaperChangeReceiver extends BroadcastReceiver {
    private SharedPreferences sharedPreferences;

    public WallpaperChangeReceiver(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (sharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_MATERIAL_YOU, false)) {
            Intent materialYouIntent = new Intent(context, MaterialYouService.class);
            ContextCompat.startForegroundService(context, materialYouIntent);
        }
    }
}
