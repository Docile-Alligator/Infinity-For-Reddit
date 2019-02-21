package SubscribedSubredditDatabase;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {SubscribedSubredditData.class}, version = 1)
public abstract class SubscribedSubredditRoomDatabase extends RoomDatabase {
    private static SubscribedSubredditRoomDatabase INSTANCE;

    public abstract SubscribedSubredditDao subscribedSubredditDao();

    public static SubscribedSubredditRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (SubscribedSubredditRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SubscribedSubredditRoomDatabase.class, "subscribed_subreddits")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
