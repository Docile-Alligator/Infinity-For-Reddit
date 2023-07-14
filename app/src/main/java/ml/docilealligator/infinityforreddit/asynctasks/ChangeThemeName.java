package ml.docilealligator.infinityforreddit.asynctasks;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class ChangeThemeName {
    public static void changeThemeName(Executor executor, RedditDataRoomDatabase redditDataRoomDatabase,
                                       String oldName, String newName) {
        executor.execute(() -> {
            redditDataRoomDatabase.customThemeDao().updateName(oldName, newName);
        });
    }
}
