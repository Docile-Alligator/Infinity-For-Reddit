package ml.ino6962.postinfinityforreddit.asynctasks;

import android.os.Handler;

import java.util.concurrent.Executor;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;
import ml.ino6962.postinfinityforreddit.subscribeduser.SubscribedUserData;

public class CheckIsFollowingUser {
    public static void checkIsFollowingUser(Executor executor, Handler handler,
                                            RedditDataRoomDatabase redditDataRoomDatabase, String username,
                                            String accountName, CheckIsFollowingUserListener checkIsFollowingUserListener) {
        executor.execute(() -> {
            SubscribedUserData subscribedUserData = redditDataRoomDatabase.subscribedUserDao().getSubscribedUser(username, accountName == null ? "-" : accountName);
            handler.post(() -> {
                if (subscribedUserData != null) {
                    checkIsFollowingUserListener.isSubscribed();
                } else {
                    checkIsFollowingUserListener.isNotSubscribed();
                }
            });
        });
    }

    public interface CheckIsFollowingUserListener {
        void isSubscribed();

        void isNotSubscribed();
    }
}
