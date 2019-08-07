package ml.docilealligator.infinityforreddit;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import Account.Account;
import Account.AccountDao;
import SubredditDatabase.SubredditDao;
import SubredditDatabase.SubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditDao;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedUserDatabase.SubscribedUserDao;
import SubscribedUserDatabase.SubscribedUserData;
import User.UserDao;
import User.UserData;

@Database(entities = {Account.class, SubredditData.class, SubscribedSubredditData.class, UserData.class, SubscribedUserData.class}, version = 1)
public abstract class RedditDataRoomDatabase extends RoomDatabase {
    private static RedditDataRoomDatabase INSTANCE;

    public abstract AccountDao accountDao();
    public abstract SubredditDao subredditDao();
    public abstract SubscribedSubredditDao subscribedSubredditDao();
    public abstract UserDao userDao();
    public abstract SubscribedUserDao subscribedUserDao();

    public static RedditDataRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (RedditDataRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RedditDataRoomDatabase.class, "reddit_data")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
