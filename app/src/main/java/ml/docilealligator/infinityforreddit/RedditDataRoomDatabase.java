package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.account.AccountDao;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeDao;
import ml.docilealligator.infinityforreddit.multireddit.AnonymousMultiredditSubreddit;
import ml.docilealligator.infinityforreddit.multireddit.AnonymousMultiredditSubredditDao;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditDao;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterDao;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsageDao;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.readpost.ReadPostDao;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQuery;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQueryDao;
import ml.docilealligator.infinityforreddit.subreddit.SubredditDao;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditDao;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserDao;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.user.UserDao;
import ml.docilealligator.infinityforreddit.user.UserData;

@Database(entities = {Account.class, SubredditData.class, SubscribedSubredditData.class, UserData.class,
        SubscribedUserData.class, MultiReddit.class, CustomTheme.class, RecentSearchQuery.class,
        ReadPost.class, PostFilter.class, PostFilterUsage.class, AnonymousMultiredditSubreddit.class}, version = 22)
public abstract class RedditDataRoomDatabase extends RoomDatabase {
    private static RedditDataRoomDatabase INSTANCE;

    public static RedditDataRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RedditDataRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RedditDataRoomDatabase.class, "reddit_data")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                                    MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                                    MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
                                    MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
                                    MIGRATION_21_22)
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

    public abstract CustomThemeDao customThemeDao();

    public abstract RecentSearchQueryDao recentSearchQueryDao();

    public abstract ReadPostDao readPostDao();

    public abstract PostFilterDao postFilterDao();

    public abstract PostFilterUsageDao postFilterUsageDao();

    public abstract AnonymousMultiredditSubredditDao anonymousMultiredditSubredditDao();

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

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE subreddits"
                    + " ADD COLUMN sidebar_description TEXT");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE custom_themes" +
                    "(name TEXT NOT NULL PRIMARY KEY, is_light_theme INTEGER NOT NULL," +
                    "is_dark_theme INTEGER NOT NULL, is_amoled_theme INTEGER NOT NULL, color_primary INTEGER NOT NULL," +
                    "color_primary_dark INTEGER NOT NULL, color_accent INTEGER NOT NULL," +
                    "color_primary_light_theme INTEGER NOT NULL, primary_text_color INTEGER NOT NULL," +
                    "secondary_text_color INTEGER NOT NULL, post_title_color INTEGER NOT NULL," +
                    "post_content_color INTEGER NOT NULL, comment_color INTEGER NOT NULL," +
                    "button_text_color INTEGER NOT NULL, background_color INTEGER NOT NULL," +
                    "card_view_background_color INTEGER NOT NULL, comment_background_color INTEGER NOT NULL," +
                    "bottom_app_bar_background_color INTEGER NOT NULL, primary_icon_color INTEGER NOT NULL," +
                    "post_icon_and_info_color INTEGER NOT NULL," +
                    "comment_icon_and_info_color INTEGER NOT NULL, toolbar_primary_text_and_icon_color INTEGER NOT NULL," +
                    "toolbar_secondary_text_color INTEGER NOT NULL, circular_progress_bar_background INTEGER NOT NULL," +
                    "tab_layout_with_expanded_collapsing_toolbar_tab_background INTEGER NOT NULL," +
                    "tab_layout_with_expanded_collapsing_toolbar_text_color INTEGER NOT NULL," +
                    "tab_layout_with_expanded_collapsing_toolbar_tab_indicator INTEGER NOT NULL," +
                    "tab_layout_with_collapsed_collapsing_toolbar_tab_background INTEGER NOT NULL," +
                    "tab_layout_with_collapsed_collapsing_toolbar_text_color INTEGER NOT NULL," +
                    "tab_layout_with_collapsed_collapsing_toolbar_tab_indicator INTEGER NOT NULL," +
                    "nav_bar_color INTEGER NOT NULL, upvoted INTEGER NOT NULL, downvoted INTEGER NOT NULL," +
                    "post_type_background_color INTEGER NOT NULL, post_type_text_color INTEGER NOT NULL," +
                    "spoiler_background_color INTEGER NOT NULL, spoiler_text_color INTEGER NOT NULL," +
                    "nsfw_background_color INTEGER NOT NULL, nsfw_text_color INTEGER NOT NULL," +
                    "flair_background_color INTEGER NOT NULL, flair_text_color INTEGER NOT NULL," +
                    "archived_tint INTEGER NOT NULL, locked_icon_tint INTEGER NOT NULL," +
                    "crosspost_icon_tint INTEGER NOT NULL, stickied_post_icon_tint INTEGER NOT NULL, subscribed INTEGER NOT NULL," +
                    "unsubscribed INTEGER NOT NULL, username INTEGER NOT NULL, subreddit INTEGER NOT NULL," +
                    "author_flair_text_color INTEGER NOT NULL, submitter INTEGER NOT NULL," +
                    "moderator INTEGER NOT NULL, single_comment_thread_background_color INTEGER NOT NULL," +
                    "unread_message_background_color INTEGER NOT NULL, divider_color INTEGER NOT NULL," +
                    "no_preview_link_background_color INTEGER NOT NULL," +
                    "vote_and_reply_unavailable_button_color INTEGER NOT NULL," +
                    "comment_vertical_bar_color_1 INTEGER NOT NULL, comment_vertical_bar_color_2 INTEGER NOT NULL," +
                    "comment_vertical_bar_color_3 INTEGER NOT NULL, comment_vertical_bar_color_4 INTEGER NOT NULL," +
                    "comment_vertical_bar_color_5 INTEGER NOT NULL, comment_vertical_bar_color_6 INTEGER NOT NULL," +
                    "comment_vertical_bar_color_7 INTEGER NOT NULL, fab_icon_color INTEGER NOT NULL," +
                    "chip_text_color INTEGER NOT NULL, is_light_status_bar INTEGER NOT NULL," +
                    "is_light_nav_bar INTEGER NOT NULL," +
                    "is_change_status_bar_icon_color_after_toolbar_collapsed_in_immersive_interface INTEGER NOT NULL)");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN awards_background_color INTEGER DEFAULT " + Color.parseColor("#EEAB02") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN awards_text_color INTEGER DEFAULT " + Color.parseColor("#FFFFFF") + " NOT NULL");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE users_temp " +
                    "(name TEXT NOT NULL PRIMARY KEY, icon TEXT, banner TEXT, " +
                    "link_karma INTEGER NOT NULL, comment_karma INTEGER DEFAULT 0 NOT NULL, created_utc INTEGER DEFAULT 0 NOT NULL," +
                    "is_gold INTEGER NOT NULL, is_friend INTEGER NOT NULL, can_be_followed INTEGER NOT NULL," +
                    "description TEXT)");
            database.execSQL(
                    "INSERT INTO users_temp(name, icon, banner, link_karma, is_gold, is_friend, can_be_followed) SELECT * FROM users");
            database.execSQL("DROP TABLE users");
            database.execSQL("ALTER TABLE users_temp RENAME TO users");

            database.execSQL("ALTER TABLE subreddits"
                    + " ADD COLUMN created_utc INTEGER DEFAULT 0 NOT NULL");

            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN bottom_app_bar_icon_color INTEGER DEFAULT " + Color.parseColor("#000000") + " NOT NULL");
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN link_color INTEGER DEFAULT " + Color.parseColor("#FF1868") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN received_message_text_color INTEGER DEFAULT " + Color.parseColor("#FFFFFF") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN sent_message_text_color INTEGER DEFAULT " + Color.parseColor("#FFFFFF") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN received_message_background_color INTEGER DEFAULT " + Color.parseColor("#4185F4") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN sent_message_background_color INTEGER DEFAULT " + Color.parseColor("#31BF7D") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN send_message_icon_color INTEGER DEFAULT " + Color.parseColor("#4185F4") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN fully_collapsed_comment_background_color INTEGER DEFAULT " + Color.parseColor("#8EDFBA") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN awarded_comment_background_color INTEGER DEFAULT " + Color.parseColor("#FFF162") + " NOT NULL");

        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE recent_search_queries" +
                    "(username TEXT NOT NULL, search_query TEXT NOT NULL, PRIMARY KEY(username, search_query), " +
                    "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");

            database.execSQL("ALTER TABLE subreddits"
                    + " ADD COLUMN suggested_comment_sort TEXT");

            database.execSQL("ALTER TABLE subreddits"
                    + " ADD COLUMN over18 INTEGER DEFAULT 0 NOT NULL");
        }
    };

    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users"
                    + " ADD COLUMN awarder_karma INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE users"
                    + " ADD COLUMN awardee_karma INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE users"
                    + " ADD COLUMN total_karma INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE users"
                    + " ADD COLUMN over_18 INTEGER DEFAULT 0 NOT NULL");
        }
    };

    private static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE subreddit_filter" +
                    "(subreddit_name TEXT NOT NULL, type INTEGER NOT NULL, PRIMARY KEY(subreddit_name, type))");
        }
    };

    private static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN no_preview_post_type_icon_tint INTEGER DEFAULT " + Color.parseColor("#808080") + " NOT NULL");
        }
    };

    private static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE read_posts"
                    + "(username TEXT NOT NULL, id TEXT NOT NULL, PRIMARY KEY(username, id), "
                    + "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN read_post_title_color INTEGER DEFAULT " + Color.parseColor("#9D9D9D") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN read_post_content_color INTEGER DEFAULT " + Color.parseColor("#9D9D9D") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN read_post_card_view_background_color INTEGER DEFAULT " + Color.parseColor("#F5F5F5") + " NOT NULL");
        }
    };

    private static final Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE post_filter"
                    + "(name TEXT NOT NULL PRIMARY KEY, max_vote INTEGER NOT NULL, min_vote INTEGER NOT NULL, " +
                    "max_comments INTEGER NOT NULL, min_comments INTEGER NOT NULL, max_awards INTEGER NOT NULL, " +
                    "min_awards INTEGER NOT NULL, only_nsfw INTEGER NOT NULL, only_spoiler INTEGER NOT NULL, " +
                    "post_title_excludes_regex TEXT, post_title_excludes_strings TEXT, exclude_subreddits TEXT, " +
                    "exclude_users TEXT, contain_flairs TEXT, exclude_flairs TEXT, contain_text_type INTEGER NOT NULL, " +
                    "contain_link_type INTEGER NOT NULL, contain_image_type INTEGER NOT NULL, " +
                    "contain_gif_type INTEGER NOT NULL, contain_video_type INTEGER NOT NULL, " +
                    "contain_gallery_type INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE post_filter_usage (name TEXT NOT NULL, usage INTEGER NOT NULL, " +
                    "name_of_usage TEXT NOT NULL, PRIMARY KEY(name, usage, name_of_usage), FOREIGN KEY(name) REFERENCES post_filter(name) ON DELETE CASCADE)");
        }
    };

    private static final Migration MIGRATION_15_16 = new Migration(15, 16) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE subreddit_filter");
        }
    };

    private static final Migration MIGRATION_16_17 = new Migration(16, 17) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("UPDATE accounts SET is_current_user = 0");
        }
    };

    private static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN current_user INTEGER DEFAULT " + Color.parseColor("#00D5EA") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN upvote_ratio_icon_tint INTEGER DEFAULT " + Color.parseColor("#0256EE") + " NOT NULL");
        }
    };

    private static final Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO accounts(username, karma, is_current_user) VALUES (\"-\", 0, 0)");
        }
    };

    private static final Migration MIGRATION_19_20 = new Migration(19, 20) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE post_filter ADD COLUMN exclude_domains TEXT");
        }
    };

    private static final Migration MIGRATION_20_21 = new Migration(20, 21) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE anonymous_multireddit_subreddits (path TEXT NOT NULL, " +
                    "username TEXT NOT NULL, subreddit_name TEXT NOT NULL, " +
                    "PRIMARY KEY(path, username, subreddit_name), FOREIGN KEY(path, username) REFERENCES multi_reddits(path, username) ON DELETE CASCADE ON UPDATE CASCADE)");
            database.execSQL("ALTER TABLE recent_search_queries ADD COLUMN time INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN media_indicator_icon_color INTEGER DEFAULT " + Color.parseColor("#FFFFFF") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN media_indicator_background_color INTEGER DEFAULT " + Color.parseColor("#000000") + " NOT NULL");
            database.execSQL("ALTER TABLE post_filter ADD COLUMN post_title_contains_strings TEXT");
            database.execSQL("ALTER TABLE post_filter ADD COLUMN post_title_contains_regex TEXT");
            database.execSQL("ALTER TABLE post_filter ADD COLUMN contain_domains TEXT");
        }
    };
    private static final Migration MIGRATION_21_22 = new Migration(21, 22) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN title TEXT");
        }
    };
}
