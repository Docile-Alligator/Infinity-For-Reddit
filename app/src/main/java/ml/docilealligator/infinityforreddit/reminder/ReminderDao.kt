package ml.docilealligator.infinityforreddit.reminder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder)

    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminders")
    fun getAllRemindersFlow(): Flow<List<Reminder>>

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}