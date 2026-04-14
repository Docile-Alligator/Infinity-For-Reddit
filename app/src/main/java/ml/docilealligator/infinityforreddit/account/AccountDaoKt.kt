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

    @Query("UPDATE accounts SET is_current_user = 0 WHERE is_current_user = 1 AND username != '-'")
    suspend fun markAllAccountsNonCurrent()

    @Query(
        "UPDATE accounts SET profile_image_url = :profileImageUrl, banner_image_url = :bannerImageUrl, " +
                "karma = :karma, is_mod = :isMod WHERE username = :username"
    )
    suspend fun updateAccountInfo(
        username: String,
        profileImageUrl: String,
        bannerImageUrl: String?,
        karma: Int,
        isMod: Boolean
    )
}