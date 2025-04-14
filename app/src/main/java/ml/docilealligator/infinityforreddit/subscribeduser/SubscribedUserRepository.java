package ml.docilealligator.infinityforreddit.subscribeduser;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedUserRepository {
    private final SubscribedUserDao mSubscribedUserDao;
    private final String mAccountName;

    SubscribedUserRepository(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        mSubscribedUserDao = redditDataRoomDatabase.subscribedUserDao();
        mAccountName = accountName;
    }

    LiveData<List<SubscribedUserData>> getAllSubscribedUsersWithSearchQuery(String searchQuery) {
        return mSubscribedUserDao.getAllSubscribedUsersWithSearchQuery(mAccountName, searchQuery);
    }

    LiveData<List<SubscribedUserData>> getAllFavoriteSubscribedUsersWithSearchQuery(String searchQuery) {
        return mSubscribedUserDao.getAllFavoriteSubscribedUsersWithSearchQuery(mAccountName, searchQuery);
    }
}
