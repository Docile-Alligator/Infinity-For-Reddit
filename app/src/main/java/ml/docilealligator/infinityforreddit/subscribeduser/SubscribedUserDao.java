package ml.docilealligator.infinityforreddit.subscribeduser;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SubscribedUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubscribedUserData subscribedUserData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SubscribedUserData> subscribedUserDataList);

    @Query("SELECT * FROM subscribed_users WHERE username = :accountName AND name LIKE '%' || :searchQuery || '%' COLLATE NOCASE ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedUserData>> getAllSubscribedUsersWithSearchQuery(String accountName, String searchQuery);

    @Query("SELECT * FROM subscribed_users WHERE username = :accountName COLLATE NOCASE ORDER BY name COLLATE NOCASE ASC")
    List<SubscribedUserData> getAllSubscribedUsersList(String accountName);

    @Query("SELECT * FROM subscribed_users WHERE username = :accountName AND name LIKE '%' || :searchQuery || '%' COLLATE NOCASE AND is_favorite = 1 ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedUserData>> getAllFavoriteSubscribedUsersWithSearchQuery(String accountName, String searchQuery);

    @Query("SELECT * FROM subscribed_users WHERE name = :name COLLATE NOCASE AND username = :accountName COLLATE NOCASE LIMIT 1")
    SubscribedUserData getSubscribedUser(String name, String accountName);

    @Query("DELETE FROM subscribed_users WHERE name = :name COLLATE NOCASE AND username = :accountName COLLATE NOCASE")
    void deleteSubscribedUser(String name, String accountName);
}
