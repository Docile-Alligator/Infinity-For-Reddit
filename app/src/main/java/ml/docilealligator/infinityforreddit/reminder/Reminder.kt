package ml.docilealligator.infinityforreddit.reminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import ml.docilealligator.infinityforreddit.account.Account

@Entity(
    tableName = "reminders",
    primaryKeys = ["post_id", "comment_id", "reminder_time"],
    foreignKeys = [ForeignKey(
        entity = Account::class, parentColumns = arrayOf("username"),
        childColumns = arrayOf("username"), onDelete = ForeignKey.SET_NULL
    )]
)
data class Reminder(
    @ColumnInfo(name = "username")
    val accountName: String?,
    @ColumnInfo(name = "post_id")
    val postId: String,
    @ColumnInfo(name = "comment_id")
    val commentId: String = "",
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "reminder_time")
    val reminderTime: Long
)
