package ml.docilealligator.infinityforreddit.readpost

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.account.Account
import kotlin.math.max

suspend fun insertReadPost(
    redditDataRoomDatabase: RedditDataRoomDatabase,
    username: String?,
    postId: String,
    @ReadPostType readPostType: Int,
    readPostsLimit: Int
) {
    if (!username.isNullOrEmpty()) {
        val readPostDaoKt = redditDataRoomDatabase.readPostDaoKt()
        val limit = max(readPostsLimit, 100)
        val isReadPostLimit = readPostsLimit != -1
        while (readPostDaoKt.getReadPostsCount(username, readPostType) > limit && isReadPostLimit) {
            readPostDaoKt.deleteOldestReadPosts(username, readPostType)
        }

        if (!redditDataRoomDatabase.accountDaoKt().isAnonymousAccountInserted()) {
            redditDataRoomDatabase.accountDaoKt().insert(Account.getAnonymousAccount())
        }

        readPostDaoKt.insert(ReadPost(username, postId, readPostType))
        if (readPostType == ReadPostType.ANONYMOUS_UPVOTED_POSTS) {
            readPostDaoKt.deleteReadPost(username, postId, ReadPostType.ANONYMOUS_DOWNVOTED_POSTS)
        } else if (readPostType == ReadPostType.ANONYMOUS_DOWNVOTED_POSTS) {
            readPostDaoKt.deleteReadPost(username, postId, ReadPostType.ANONYMOUS_UPVOTED_POSTS)
        }
    }
}

suspend fun deleteReadPost(
    redditDataRoomDatabase: RedditDataRoomDatabase,
    username: String?,
    postId: String,
    @ReadPostType readPostType: Int
) {
    if (!username.isNullOrEmpty()) {
        redditDataRoomDatabase.readPostDaoKt().deleteReadPost(username, postId, readPostType)
    }
}