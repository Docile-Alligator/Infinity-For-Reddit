package ml.docilealligator.infinityforreddit.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.multireddit.FetchMultiRedditInfo
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit
import ml.docilealligator.infinityforreddit.utils.APIUtils
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException

interface CopyMultiRedditActivityRepository {
    suspend fun fetchMultiRedditInfo(multipath: String): MultiReddit?
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
}