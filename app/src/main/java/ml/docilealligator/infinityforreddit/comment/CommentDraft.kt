package ml.docilealligator.infinityforreddit.comment

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "comment_draft",
    primaryKeys = ["full_name", "draft_type"]
)
data class CommentDraft(
    @ColumnInfo(name = "full_name")
    var parentFullName: String,
    var content: String,
    @ColumnInfo(name = "last_updated")
    var lastUpdated: Long,
    @ColumnInfo(name = "draft_type")
    var draftType: DraftType
)

enum class DraftType {
    REPLY,
    EDIT
}