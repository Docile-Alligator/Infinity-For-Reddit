package ml.docilealligator.infinityforreddit.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

import ml.docilealligator.infinityforreddit.services.MaterialYouService;

public class WallpaperChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("asfasdf", "s " + intent.getAction());
        if (Objects.equals(intent.getAction(), "android.intent.action.WALLPAPER_CHANGED")) {
            Intent materialYouIntent = new Intent(context, MaterialYouService.class);
            context.startService(materialYouIntent);
        }
    }
}
