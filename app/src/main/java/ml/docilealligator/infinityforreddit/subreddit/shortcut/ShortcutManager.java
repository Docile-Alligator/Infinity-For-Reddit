package ml.docilealligator.infinityforreddit.subreddit.shortcut;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;

public class ShortcutManager {
    private static ShortcutInfoCompat getInfo(Context context, @NonNull String subreddit, @NonNull Bitmap icon) {
        final Intent shortcut = new Intent(context, ViewSubredditDetailActivity.class);
        shortcut.setPackage(context.getPackageName());
        shortcut.setAction(Intent.ACTION_MAIN);
        shortcut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcut.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subreddit);

        String shortcutId = BuildConfig.APPLICATION_ID + ".shortcut." + subreddit;
        String subredditName = "r/" + subreddit;
        return new ShortcutInfoCompat.Builder(context, shortcutId)
                .setIntent(shortcut)
                .setShortLabel(subredditName)
                .setAlwaysBadged()
                .setIcon(IconCompat.createWithBitmap(icon))
                .build();
    }

    public static boolean requestPinShortcut(Context context, @NonNull String subreddit, @NonNull Bitmap icon) {
        return ShortcutManagerCompat.requestPinShortcut(context, getInfo(context, subreddit, icon), null);
    }
}

