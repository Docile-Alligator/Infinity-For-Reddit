package ml.docilealligator.infinityforreddit.SubscribedUserDatabase;

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

    @Query("DELETE FROM subscribed_users")
    void deleteAllSubscribedUsers();

    @Query("SELECT * FROM subscribed_users WHERE username = :accountName ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedUserData>> getAllSubscribedUsers(String accountName);

    @Query("SELECT * FROM subscribed_users WHERE name = :name AND username = :accountName COLLATE NOCASE LIMIT 1")
    SubscribedUserData getSubscribedUser(String name, String accountName);

    @Query("DELETE FROM subscribed_users WHERE name = :name AND username = :accountName")
    void deleteSubscribedUser(String name, String accountName);
}
