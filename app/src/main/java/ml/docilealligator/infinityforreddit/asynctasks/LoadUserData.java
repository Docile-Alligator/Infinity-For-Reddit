package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import androidx.annotation.Nullable;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.user.FetchUserData;
import ml.docilealligator.infinityforreddit.user.UserDao;
import ml.docilealligator.infinityforreddit.user.UserData;
import retrofit2.Retrofit;

public class LoadUserData {

    public static void loadUserData(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                    String accessToken, String userName, @Nullable Retrofit oauthRetrofit,
                                    Retrofit retrofit, LoadUserDataAsyncTaskListener loadUserDataAsyncTaskListener) {
        executor.execute(() -> {
            UserDao userDao = redditDataRoomDatabase.userDao();
            UserData userData = userDao.getUserData(userName);
            if (userData != null) {
                String iconImageUrl = userData.getIconUrl();
                handler.post(() -> loadUserDataAsyncTaskListener.loadUserDataSuccess(iconImageUrl));
            } else {
                handler.post(() -> FetchUserData.fetchUserData(executor, handler, redditDataRoomDatabase,
                        oauthRetrofit, retrofit, accessToken, userName, new FetchUserData.FetchUserDataListener() {
                            @Override
                            public void onFetchUserDataSuccess(UserData userData, int inboxCount) {
                                InsertUserData.insertUserData(executor, handler, redditDataRoomDatabase, userData,
                                        () -> loadUserDataAsyncTaskListener.loadUserDataSuccess(userData.getIconUrl()));
                            }

                            @Override
                            public void onFetchUserDataFailed() {
                                loadUserDataAsyncTaskListener.loadUserDataSuccess(null);
                            }
                        }));
            }
        });
    }

    public interface LoadUserDataAsyncTaskListener {
        void loadUserDataSuccess(String iconImageUrl);
    }
}
