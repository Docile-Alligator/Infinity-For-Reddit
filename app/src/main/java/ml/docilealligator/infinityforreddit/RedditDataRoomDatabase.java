package ml.docilealligator.infinityforreddit;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ml.docilealligator.infinityforreddit.Account.Account;
import ml.docilealligator.infinityforreddit.Account.AccountDao;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditDao;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditDao;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserDao;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserData;
import ml.docilealligator.infinityforreddit.User.UserDao;
import ml.docilealligator.infinityforreddit.User.UserData;

@Database(entities = {Account.class, SubredditData.class, SubscribedSubredditData.class, UserData.class, SubscribedUserData.class}, version = 1)
public abstract class RedditDataRoomDatabase extends RoomDatabase {
    private static RedditDataRoomDatabase INSTANCE;

    public static RedditDataRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RedditDataRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RedditDataRoomDatabase.class, "reddit_data")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract AccountDao accountDao();

    public abstract SubredditDao subredditDao();

    public abstract SubscribedSubredditDao subscribedSubredditDao();

    public abstract UserDao userDao();

    public abstract SubscribedUserDao subscribedUserDao();
}
