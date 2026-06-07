package ml.docilealligator.infinityforreddit.network

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.utils.APIUtils
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.concurrent.atomic.AtomicReference

class AnonymousAccessTokenInterceptor(
    private val retrofit: Retrofit, private val redditDataRoomDatabase: RedditDataRoomDatabase
) : Interceptor {
    private val cachedAccessToken = AtomicReference<String?>(null)
    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        if (path.contains("/api/v1/access_token") || path.contains("/api/v1/authorize")) {
            return chain.proceed(originalRequest)
        }

        val newUrl = originalRequest.url.newBuilder().host("oauth.reddit.com").build()

        val token = getOrFetchToken()

        val authenticatedRequest = originalRequest.newBuilder()
            .url(newUrl)
            .header("Authorization", "bearer $token")
            .build()

        val response = chain.proceed(authenticatedRequest)
        if (response.code == 401 || response.code == 403) {
            response.close()

            val expiredAccessToken = response.request.header("Authorization")?.replace("bearer ", "")

            synchronized(refreshLock) {
                val currentAccessToken = getOrFetchToken()

                val newAccessToken = if (currentAccessToken != expiredAccessToken && currentAccessToken != null) {
                    currentAccessToken
                } else {
                    try {
                        refreshToken()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return chain.proceed(originalRequest)
                    }
                }

                return newAccessToken?.let {
                    chain.proceed(originalRequest.newBuilder()
                        .url(newUrl)
                        .header("Authorization", "Bearer $newAccessToken")
                        .build())
                } ?: chain.proceed(response.request.newBuilder().build())
            }
        } else {
            return response
        }
    }

    private fun getOrFetchToken(): String? {
        return cachedAccessToken.get() ?: synchronized(refreshLock) {
            val accessToken = redditDataRoomDatabase.accountDaoKt().getAnonymousAccessToken()
            cachedAccessToken.set(accessToken)
            accessToken
        }
    }

    private fun refreshToken(): String? {
        return try {
            val params = mutableMapOf<String, String>()
            params[APIUtils.GRANT_TYPE_KEY] = "https://oauth.reddit.com/grants/installed_client"
            params["device_id"] = "DO_NOT_TRACK_THIS_DEVICE"

            val response = retrofit.create(RedditAPIKt::class.java).getAnonymousAccessToken(
                APIUtils.getHttpBasicAuthHeader(), params
            ).execute()
            if (response.isSuccessful) {
                val newAccessToken = JSONObject(response.body()).getString(APIUtils.ACCESS_TOKEN_KEY)
                redditDataRoomDatabase.accountDaoKt().setAnonymousAccessToken(newAccessToken)
                cachedAccessToken.set(newAccessToken)

                newAccessToken
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}