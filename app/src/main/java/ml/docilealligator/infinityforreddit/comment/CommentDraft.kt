package ml.docilealligator.infinityforreddit.comment

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comment_draft")
data class CommentDraft(
    @PrimaryKey
    @ColumnInfo(name = "parent_full_name")
    var parentFullName: String,
    var content: String,
    @ColumnInfo(name = "last_updated")
    var lastUpdated: Long
) {
}