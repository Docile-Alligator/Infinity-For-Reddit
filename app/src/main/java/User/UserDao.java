package User;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(UserData userData);

  @Query("DELETE FROM users")
  void deleteAllUsers();

  @Query("SELECT * FROM users WHERE name = :userName COLLATE NOCASE LIMIT 1")
  LiveData<UserData> getUserLiveData(String userName);

  @Query("SELECT * FROM users WHERE name = :userName COLLATE NOCASE LIMIT 1")
  UserData getUserData(String userName);
}
