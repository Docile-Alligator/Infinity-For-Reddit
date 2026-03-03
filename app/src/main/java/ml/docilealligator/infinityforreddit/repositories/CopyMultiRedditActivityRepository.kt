package ml.docilealligator.infinityforreddit.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.APIError
import ml.docilealligator.infinityforreddit.APIResult
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.account.Account
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.multireddit.AnonymousMultiredditSubreddit
import ml.docilealligator.infinityforreddit.multireddit.ExpandedSubredditInMultiReddit
import ml.docilealligator.infinityforreddit.multireddit.FetchMultiRedditInfo
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException

interface CopyMultiRedditActivityRepository {
    suspend fun fetchMultiRedditInfo(multipath: String): MultiReddit?
    suspend fun copyMultiReddit(multipath: String, name: String, description: String, subreddits: List<ExpandedSubredditInMultiReddit>): APIResult<MultiReddit?>
    //suspend fun copyMultiRedditAnonymous(multipath: String, name: String, description: String, subreddits: List<ExpandedSubredditInMultiReddit>): APIResult<MultiReddit?>
}

class CopyMultiRedditActivityRepositoryImpl(
    val oauthRetrofit: Retrofit,
    val redditDataRoomDatabase: RedditDataRoomDatabase,
    val accessToken: String
): CopyMultiRedditActivityRepository {
    override suspend fun fetchMultiRedditInfo(multipath: String): MultiReddit? {
        try {
            val response = oauthRetrofit.create(RedditAPIKt::class.java)
                .getMultiRedditInfo(APIUtils.getOAuthHeader(accessToken), multipath)

            return withContext(Dispatchers.Default) {
                FetchMultiRedditInfo.parseMultiRedditInfo(response)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } catch (e: HttpException) {
            e.printStackTrace()
            return null
        }
    }

    override suspend fun copyMultiReddit(multipath: String, name: String, description: String, subreddits: List<ExpandedSubredditInMultiReddit>): APIResult<MultiReddit?> {
        if (accessToken.isEmpty()) {
            return copyMultiRedditAnonymous(name, description, subreddits)
        }

        try {
            val params = mapOf(
                APIUtils.FROM_KEY to multipath,
                APIUtils.DISPLAY_NAME_KEY to name,
                APIUtils.DESCRIPTION_MD_KEY to description
            )
            val response = oauthRetrofit.create(RedditAPIKt::class.java)
                .copyMultiReddit(APIUtils.getOAuthHeader(accessToken), params)

            return withContext(Dispatchers.Default) {
                APIResult.Success(FetchMultiRedditInfo.parseMultiRedditInfo(response))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return APIResult.Error(APIError.Message(e.localizedMessage ?: "Network error"))
        } catch (e: HttpException) {
            e.printStackTrace()
            try {
                val errorMessage = JSONObject(e.response()?.errorBody()?.string() ?: "").getString(JSONUtils.EXPLANATION_KEY)
                return APIResult.Error(APIError.Message(errorMessage))
            } catch(ignore: JSONException) {
                return APIResult.Error(APIError.MessageRes(R.string.copy_multi_reddit_failed))
            }
        }
    }

    suspend fun copyMultiRedditAnonymous(name: String, description: String, subreddits: List<ExpandedSubredditInMultiReddit>): APIResult<MultiReddit?> {
        if (!redditDataRoomDatabase.accountDaoKt().isAnonymousAccountInserted()) {
            redditDataRoomDatabase.accountDaoKt().insert(Account.getAnonymousAccount())
        }

        if (redditDataRoomDatabase.multiRedditDaoKt().getMultiReddit("/user/-/m/$name", Account.ANONYMOUS_ACCOUNT) != null) {
            return APIResult.Error(APIError.MessageRes(R.string.duplicate_multi_reddit))
        } else {
            val newMultiReddit = MultiReddit(
                "/user/-/m/$name",
                name,
                name,
                description,
                null,
                null,
                "private",
                Account.ANONYMOUS_ACCOUNT,
                0,
                System.currentTimeMillis(),
                true,
                false,
                false
            )

            redditDataRoomDatabase.multiRedditDaoKt().insert(newMultiReddit)
            val anonymousMultiRedditSubreddits: MutableList<AnonymousMultiredditSubreddit> = mutableListOf()
            for (s in subreddits) {
                anonymousMultiRedditSubreddits.add(AnonymousMultiredditSubreddit("/user/-/m/$name", s.name, s.iconUrl))
            }
            redditDataRoomDatabase.anonymousMultiredditSubredditDaoKt().insertAll(anonymousMultiRedditSubreddits)

            return APIResult.Success(newMultiReddit)
        }
    }
}