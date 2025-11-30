package ml.docilealligator.infinityforreddit.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.APIResult
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
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
    suspend fun copyMultiReddit(multipath: String, name: String, description: String): APIResult<MultiReddit?>
}

class CopyMultiRedditActivityRepositoryImpl(
    val oauthRetrofit: Retrofit,
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

    override suspend fun copyMultiReddit(multipath: String, name: String, description: String): APIResult<MultiReddit?> {
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
            return APIResult.Error(e.localizedMessage ?: "Network error")
        } catch (e: HttpException) {
            e.printStackTrace()
            try {
                val errorMessage = JSONObject(e.response()?.errorBody()?.string() ?: "").getString(JSONUtils.EXPLANATION_KEY)
                return APIResult.Error(errorMessage)
            } catch(ignore: JSONException) {
                return APIResult.Error("Cannot copy multireddit.")
            }
        }
    }
}