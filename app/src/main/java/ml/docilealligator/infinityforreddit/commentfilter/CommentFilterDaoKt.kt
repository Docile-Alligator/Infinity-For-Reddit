package ml.docilealligator.infinityforreddit.commentfilter

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CommentFilterDaoKt {
    @Query(
        ("SELECT * FROM comment_filter WHERE (comment_filter.name IN " +
                "(SELECT comment_filter_usage.name FROM comment_filter_usage WHERE (usage = :usage AND name_of_usage = :nameOfUsage COLLATE NOCASE)))" +
                " OR (comment_filter.name NOT IN (SELECT comment_filter_usage.name FROM comment_filter_usage))")
    )
    suspend fun getValidCommentFilters(usage: Int, nameOfUsage: String): List<CommentFilter>
}