package ml.docilealligator.infinityforreddit.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.services.MaterialYouService;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class WallpaperChangeReceiver extends BroadcastReceiver {
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_MATERIAL_YOU, false)) {
            Intent materialYouIntent = new Intent(context, MaterialYouService.class);
            context.startService(materialYouIntent);
        }
    }
}
