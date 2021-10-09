package ml.docilealligator.infinityforreddit.customviews;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

import im.ene.toro.exoplayer.Config;
import im.ene.toro.exoplayer.DefaultExoCreator;
import im.ene.toro.exoplayer.ToroExo;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class LoopAvailableExoCreator extends DefaultExoCreator {
    private final SharedPreferences sharedPreferences;

    public LoopAvailableExoCreator(@NonNull ToroExo toro, @NonNull Config config, SharedPreferences sharedPreferences) {
        super(toro, config);
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public SimpleExoPlayer createPlayer() {
        SimpleExoPlayer player = super.createPlayer();
        if (sharedPreferences.getBoolean(SharedPreferencesUtils.LOOP_VIDEO, true)) {
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
        } else {
            player.setRepeatMode(Player.REPEAT_MODE_OFF);
        }

        return player;
    }
}
