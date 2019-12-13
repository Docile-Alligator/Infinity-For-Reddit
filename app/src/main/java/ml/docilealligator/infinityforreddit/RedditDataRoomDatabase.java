package ml.docilealligator.infinityforreddit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import ml.docilealligator.infinityforreddit.Account.Account;
import ml.docilealligator.infinityforreddit.Account.AccountDao;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiRedditDao;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditDao;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditDao;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserDao;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserData;
import ml.docilealligator.infinityforreddit.User.UserDao;
import ml.docilealligator.infinityforreddit.User.UserData;

@Database(entities = {Account.class, SubredditData.class, SubscribedSubredditData.class, UserData.class,
        SubscribedUserData.class, MultiReddit.class}, version = 4)
public abstract class RedditDataRoomDatabase extends RoomDatabase {
    private static RedditDataRoomDatabase INSTANCE;

    public static RedditDataRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RedditDataRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RedditDataRoomDatabase.class, "reddit_data")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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

    public abstract MultiRedditDao multiRedditDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE subscribed_subreddits"
                    + " ADD COLUMN is_favorite INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE subscribed_users"
                    + " ADD COLUMN is_favorite INTEGER DEFAULT 0 NOT NULL");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE subscribed_subreddits_temp " +
                    "(id TEXT NOT NULL, name TEXT, icon TEXT, username TEXT NOT NULL, " +
                    "is_favorite INTEGER NOT NULL, PRIMARY KEY(id, username), " +
                    "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");
            database.execSQL(
                    "INSERT INTO subscribed_subreddits_temp SELECT * FROM subscribed_subreddits");
            database.execSQL("DROP TABLE subscribed_subreddits");
            database.execSQL("ALTER TABLE subscribed_subreddits_temp RENAME TO subscribed_subreddits");

            database.execSQL("CREATE TABLE subscribed_users_temp " +
                    "(name TEXT NOT NULL, icon TEXT, username TEXT NOT NULL, " +
                    "is_favorite INTEGER NOT NULL, PRIMARY KEY(name, username), " +
                    "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");
            database.execSQL(
                    "INSERT INTO subscribed_users_temp SELECT * FROM subscribed_users");
            database.execSQL("DROP TABLE subscribed_users");
            database.execSQL("ALTER TABLE subscribed_users_temp RENAME TO subscribed_users");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE multi_reddits" +
                    "(path TEXT NOT NULL, username TEXT NOT NULL, name TEXT NOT NULL, " +
                    "display_name TEXT NOT NULL, description TEXT, copied_from TEXT, " +
                    "n_subscribers INTEGER NOT NULL, icon_url TEXT, created_UTC INTEGER NOT NULL, " +
                    "visibility TEXT, over_18 INTEGER NOT NULL, is_subscriber INTEGER NOT NULL, " +
                    "is_favorite INTEGER NOT NULL, PRIMARY KEY(path, username), " +
                    "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");
        }
    };
}
