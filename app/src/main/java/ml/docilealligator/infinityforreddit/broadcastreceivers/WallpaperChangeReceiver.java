package ml.docilealligator.infinityforreddit.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import ml.docilealligator.infinityforreddit.services.MaterialYouService;

public class WallpaperChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent materialYouIntent = new Intent(context, MaterialYouService.class);
        ContextCompat.startForegroundService(context, materialYouIntent);
    }
}
