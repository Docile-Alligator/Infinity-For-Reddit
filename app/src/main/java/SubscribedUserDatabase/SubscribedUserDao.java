package SubscribedUserDatabase;

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

    @Query("SELECT * FROM subscribed_users ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedUserData>> getAllSubscribedUsers();

    @Query("SELECT * FROM subscribed_users WHERE name = :userName COLLATE NOCASE LIMIT 1")
    SubscribedUserData getSubscribedUser(String userName);

    @Query("DELETE FROM subscribed_users WHERE name = :userName")
    void deleteSubscribedUser(String userName);
}
