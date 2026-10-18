package ml.docilealligator.infinityforreddit.user;

import androidx.lifecycle.LiveData;

import ml.docilealligator.infinityforreddit.database.RedditDataRoomDatabase;

public class UserRepository {
    private final LiveData<UserData> mUserLiveData;

    UserRepository(RedditDataRoomDatabase redditDataRoomDatabase, String userName) {
        mUserLiveData = redditDataRoomDatabase.userDao().getUserLiveData(userName);
    }

    LiveData<UserData> getUserLiveData() {
        return mUserLiveData;
    }
}
