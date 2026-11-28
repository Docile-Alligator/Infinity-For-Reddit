package ml.docilealligator.infinityforreddit.subreddit

import android.os.Handler
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.account.Account
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.result.APIError
import ml.docilealligator.infinityforreddit.result.AppResult
import ml.docilealligator.infinityforreddit.subreddit.FetchSubredditData.FetchSubredditListingDataListener
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.IOException

suspend fun fetchAllSubredditListingData(
    retrofit: Retrofit?,
    oauthRetrofit: Retrofit?,
    accessToken: String?,
    accountName: String, nsfw: Boolean,
): AppResult<List<SubredditData>, String> {
    val api =
        (if (Account.ANONYMOUS_ACCOUNT == accountName) retrofit else oauthRetrofit)!!.create(
            RedditAPIKt::class.java
        )
    val headers =
        if (Account.ANONYMOUS_ACCOUNT == accountName) HashMap<String, String>() else APIUtils.getOAuthHeader(
            accessToken
        )
    var after: String? = null
    var resultList = emptyList<SubredditData>()

    while (true) {
        try {
            val response = api.getModeratedSubreddits(headers, after)
            if (response.isSuccessful) {
                when (val result = withContext(Dispatchers.Default) {
                    parseSubredditListingDataSync(
                        response.body(), nsfw
                    )
                }) {
                    is AppResult.Success<Pair<List<SubredditData>, String?>> -> {
                        resultList = resultList + result.data.first
                        if (result.data.second == null || result.data.second.equals(after) || result.data.first.isEmpty()) {
                            return AppResult.Success(resultList)
                        }

                        after = result.data.second
                    }
                    is AppResult.Error<*> -> {
                        if (resultList.isEmpty()) {
                            return AppResult.Error(response.errorBody()?.string() ?: "Response error")
                        }
                        return AppResult.Success(resultList)
                    }
                }
            } else {
                if (resultList.isEmpty()) {
                    return AppResult.Error(response.errorBody()?.string() ?: "Response error")
                }
                return AppResult.Success(resultList)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            if (resultList.isEmpty()) {
                return AppResult.Error(e.message ?: "Unknown reason")
            }
            return AppResult.Success(resultList)
        }
    }
}

@WorkerThread
private fun parseSubredditListingDataSync(
    response: String?,
    nsfw: Boolean
): AppResult<Pair<List<SubredditData>, String?>, String> {
    if (response == null) {
        return AppResult.Error("Empty response")
    }

    try {
        val jsonObject = JSONObject(response)
        val dataObject = jsonObject.getJSONObject(JSONUtils.DATA_KEY)
        val children = dataObject.getJSONArray(JSONUtils.CHILDREN_KEY)

        val subredditListingData = ArrayList<SubredditData>()
        for (i in 0..<children.length()) {
            val data = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY)
            val subredditData = ParseSubredditData.parseSubredditDataSync(data, nsfw)
            if (subredditData != null) {
                subredditListingData.add(subredditData)
            }
        }

        val after = if (dataObject.isNull(JSONUtils.AFTER_KEY)) null else dataObject.getString(JSONUtils.AFTER_KEY)

        return AppResult.Success(Pair(subredditListingData, after))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
        return AppResult.Error(e.message ?: "Unknown error")
    }
}