package SubredditDatabase;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {SubredditData.class}, version = 1)
public abstract class SubredditRoomDatabase extends RoomDatabase{
    private static SubredditRoomDatabase INSTANCE;

    public abstract SubredditDao subredditDao();

    public static SubredditRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (SubredditRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SubredditRoomDatabase.class, "subreddits")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
