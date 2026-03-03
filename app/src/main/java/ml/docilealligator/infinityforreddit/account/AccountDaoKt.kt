package ml.docilealligator.infinityforreddit.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AccountDaoKt {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(account: Account)

    @Query("SELECT EXISTS (SELECT 1 FROM accounts WHERE username = '-')")
    suspend fun isAnonymousAccountInserted(): Boolean
}