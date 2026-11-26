package ml.docilealligator.infinityforreddit.user

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDaoKt {
    @Query("SELECT * FROM users WHERE name = :userName COLLATE NOCASE LIMIT 1")
    suspend fun getUserData(userName: String): UserData?
}