package ml.docilealligator.infinityforreddit.reminder

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder)

    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminders")
    fun getAllRemindersFlow(): Flow<List<Reminder>>
}