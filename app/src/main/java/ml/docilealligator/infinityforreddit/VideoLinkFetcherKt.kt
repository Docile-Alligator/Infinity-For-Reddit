package ml.docilealligator.infinityforreddit

import android.content.SharedPreferences
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.apis.RedgifsAPI
import ml.docilealligator.infinityforreddit.apis.RedgifsAPIKt
import ml.docilealligator.infinityforreddit.apis.StreamableAPIKt
import ml.docilealligator.infinityforreddit.apis.VReddItKt
import ml.docilealligator.infinityforreddit.post.ParsePost
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.thing.StreamableVideo
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.APIUtils.RedgifsAuthToken
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.IOException
import javax.inject.Provider
import androidx.core.content.edit

@OptIn(UnstableApi::class)
suspend fun fetchVideoLink(
    retrofit: Retrofit, vReddItRetrofit: Retrofit,
    redgifsRetrofit: Retrofit, streamableApiProvider: Provider<StreamableAPIKt>,
    currentAccountSharedPreferences: SharedPreferences, videoType: Int,
    redgifsId: String?, vRedditItUrl: String?,
    shortCode: String?
): AppResult<*, *> {
    return when (videoType) {
        ViewVideoActivity.VIDEO_TYPE_STREAMABLE -> fetchStreamableVideo(
            streamableApiProvider,
            shortCode
        )

        ViewVideoActivity.VIDEO_TYPE_REDGIFS -> fetchRedgifsVideoLinks(
            redgifsRetrofit,
            currentAccountSharedPreferences, redgifsId
        )

        ViewVideoActivity.VIDEO_TYPE_V_REDD_IT -> loadVReddItVideo(
            retrofit, vReddItRetrofit, redgifsRetrofit, streamableApiProvider,
            currentAccountSharedPreferences, vRedditItUrl
        )

        else -> AppResult.Error(R.string.error_fetching_video_invalid_type)
    }
}

data class VReddItReturnType(
    val newRedgifsId: String? = null,
    val newStreamableShortCode: String? = null,
    val post: Post,
    val optionalResult: AppResult<*, Int?>? = null
)

suspend fun loadVReddItVideo(
    retrofit: Retrofit, mVReddItRetrofit: Retrofit,
    redgifsRetrofit: Retrofit, streamableApiProvider: Provider<StreamableAPIKt>,
    currentAccountSharedPreferences: SharedPreferences,
    vRedditItUrl: String?
): AppResult<VReddItReturnType, Int?> {
    return vRedditItUrl?.let {
        try {
            val response = mVReddItRetrofit.create(VReddItKt::class.java).getRedirectUrl(vRedditItUrl)

            if (response.isSuccessful) {
                val redirectUri = response.raw().request.url.toString().toUri()
                val redirectPath = redirectUri.path
                if (redirectPath != null && (redirectPath.matches("/r/\\w+/comments/\\w+/?\\w+/?".toRegex()) || redirectPath.matches(
                        "/user/\\w+/comments/\\w+/?\\w+/?".toRegex()
                    ))
                ) {
                    val segments = redirectUri.pathSegments
                    val commentsIndex = segments.lastIndexOf("comments")
                    segments.getOrNull(commentsIndex + 1)?.let { postId ->
                        val postResponse = retrofit.create(RedditAPIKt::class.java).getPost(postId)
                        if (postResponse.isSuccessful) {
                            val post = withContext(Dispatchers.Default) {
                                ParsePost.parsePostSync(response.body())
                            }
                            return post?.let { post ->
                                if (post.isRedgifs) {
                                    var redgifsId = post.redgifsId
                                    if (redgifsId != null && redgifsId.contains("-")) {
                                        redgifsId = redgifsId.substring(0, redgifsId.indexOf('-'))
                                    }

                                    AppResult.Success(VReddItReturnType(
                                        newRedgifsId = redgifsId,
                                        post = post,
                                        optionalResult = fetchRedgifsVideoLinks(
                                            redgifsRetrofit,
                                            currentAccountSharedPreferences,
                                            redgifsId
                                        )
                                    ))
                                } else if (post.isStreamable) {
                                    val shortCode = post.streamableShortCode

                                    AppResult.Success(VReddItReturnType(
                                        newStreamableShortCode = shortCode,
                                        post = post,
                                        optionalResult = fetchStreamableVideo(
                                            streamableApiProvider,
                                            shortCode
                                        )
                                    ))
                                } else if (post.isImgur) {
                                    AppResult.Success(VReddItReturnType(
                                        post = post,
                                        optionalResult = AppResult.Success(Pair(post.videoUrl, post.videoDownloadUrl))
                                    ))
                                } else {
                                    if (post.videoUrl != null) {
                                        AppResult.Success(VReddItReturnType(
                                            post = post
                                        ))
                                    } else {
                                        AppResult.Error(R.string.error_fetching_v_redd_it_video_cannot_get_video_url)
                                    }
                                }
                            } ?: AppResult.Error(R.string.error_fetching_v_redd_it_video_cannot_get_post)
                        } else {
                            AppResult.Error(R.string.error_fetching_v_redd_it_video_cannot_get_post)
                        }
                    } ?: AppResult.Error(R.string.error_fetching_v_redd_it_video_cannot_parse_redirect_url)
                } else {
                    AppResult.Error(R.string.error_fetching_v_redd_it_video_cannot_get_post_id)
                }
            } else {
                AppResult.Error(R.string.error_fetching_v_redd_it_video_cannot_get_redirect_url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(R.string.error_fetching_v_redd_it_video_cannot_get_redirect_url)
        }
    } ?: AppResult.Error(R.string.error_fetching_v_redd_it_video_invalid_url)
}

suspend fun fetchRedgifsVideoLinks(
    redgifsRetrofit: Retrofit,
    currentAccountSharedPreferences: SharedPreferences,
    redgifsId: String?
): AppResult<Pair<String, String>, Int?> {
    return redgifsId?.let {
        try {
            // Get valid token
            getValidAccessToken(
                redgifsRetrofit,
                currentAccountSharedPreferences
            )?.let { accessToken ->
                if (accessToken.isEmpty()) {
                    return AppResult.Error(null)
                }

                var response = redgifsRetrofit
                    .create(RedgifsAPIKt::class.java)
                    .getRedgifsData(
                        APIUtils.getRedgifsOAuthHeader(accessToken),
                        redgifsId, APIUtils.USER_AGENT
                    )
                if (response.isSuccessful) {
                    parseRedgifsVideoLinks(
                        response.body()
                    )
                } else if (response.code() == 401) {
                    // Token expired, try once more with new token
                    val accessToken = refreshRedgifsAccessToken(
                        redgifsRetrofit,
                        currentAccountSharedPreferences
                    )
                    accessToken?.let {
                        response = redgifsRetrofit
                            .create(RedgifsAPIKt::class.java)
                            .getRedgifsData(
                                APIUtils.getRedgifsOAuthHeader(it),
                                redgifsId, APIUtils.USER_AGENT
                            )
                        if (response.isSuccessful) {
                            parseRedgifsVideoLinks(
                                response.body()
                            )
                        } else {
                            AppResult.Error(null)
                        }
                    } ?: AppResult.Error(null)
                } else {
                    return AppResult.Error(null)
                }
            } ?: AppResult.Error(null)
        } catch (e: IOException) {
            e.printStackTrace()
            AppResult.Error(null)
        }
    } ?: AppResult.Error(null)
}

private suspend fun getValidAccessToken(
    redgifsRetrofit: Retrofit,
    currentAccountSharedPreferences: SharedPreferences
): String? {
    // Check if existing token is valid
    val currentToken = APIUtils.REDGIFS_TOKEN.get()
    if (currentToken.isValid) {
        return currentToken.token
    }

    // Get new token if current one is invalid
    return refreshRedgifsAccessToken(
        redgifsRetrofit,
        currentAccountSharedPreferences
    )
}

private suspend fun refreshRedgifsAccessToken(
    redgifsRetrofit: Retrofit,
    currentAccountSharedPreferences: SharedPreferences
): String? {
    return try {
        val api = redgifsRetrofit.create(RedgifsAPIKt::class.java)
        val response = api.getRedgifsTemporaryToken()

        if (response.isSuccessful && response.body() != null) {
            val newAccessToken = JSONObject(response.body()).getString("token")

            // Update both the atomic reference and shared preferences
            val newToken = RedgifsAuthToken.expireIn1day(newAccessToken)
            APIUtils.REDGIFS_TOKEN.set(newToken)
            currentAccountSharedPreferences.edit {
                putString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, newAccessToken)
            }

            newAccessToken
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun parseRedgifsVideoLinks(
    response: String?
): AppResult<Pair<String, String>, Int?> {
    return withContext(Dispatchers.Default) {
        try {
            val jsonResponse = JSONObject(response)
            val gif = jsonResponse.getJSONObject(JSONUtils.GIF_KEY)
            val urls = gif.getJSONObject(JSONUtils.URLS_KEY)

            // Try HD first, fall back to SD if not available
            var mp4: String
            if (urls.has(JSONUtils.HD_KEY)) {
                mp4 = urls.getString(JSONUtils.HD_KEY)
            } else if (urls.has("sd")) {
                mp4 = urls.getString("sd")
            } else {
                return@withContext AppResult.Error(null)
            }

            if (mp4.contains("-silent")) {
                mp4 = mp4.substring(0, mp4.indexOf("-silent")) + ".mp4"
            }
            val mp4Name = mp4

            AppResult.Success(Pair(mp4Name, mp4Name))
        } catch (e: JSONException) {
            e.printStackTrace()
            AppResult.Error(null)
        }
    }
}

suspend fun fetchStreamableVideo(
    streamableApiProvider: Provider<StreamableAPIKt>,
    shortCode: String?
): AppResult<StreamableVideo, Int?> {
    return shortCode?.let {
        try {
            val response = streamableApiProvider.get().getStreamableData(it)
            if (response.isSuccessful) {
                val jsonObject = JSONObject(response.body())
                val title = jsonObject.getString(JSONUtils.TITLE_KEY)
                val filesObject = jsonObject.getJSONObject(JSONUtils.FILES_KEY)
                val mp4 = parseMedia(filesObject.getJSONObject(JSONUtils.MP4_KEY))
                var mp4MobileTemp: StreamableVideo.Media? = null
                if (filesObject.has(JSONUtils.MP4_MOBILE_KEY)) {
                    mp4MobileTemp = parseMedia(filesObject.getJSONObject(JSONUtils.MP4_MOBILE_KEY))
                }
                if (mp4 == null && mp4MobileTemp == null) {
                    return AppResult.Error(null)
                }
                val mp4Mobile = mp4MobileTemp

                AppResult.Success(StreamableVideo(title, mp4, mp4Mobile))
            } else {
                AppResult.Error(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(null)
        }
    } ?: AppResult.Error(null)
}

private suspend fun parseMedia(jsonObject: JSONObject): StreamableVideo.Media? {
    return withContext(Dispatchers.Default) {
        try {
            StreamableVideo.Media(
                jsonObject.getString(JSONUtils.URL_KEY),
                jsonObject.getInt(JSONUtils.WIDTH_KEY),
                jsonObject.getInt(JSONUtils.HEIGHT_KEY)
            )
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }
}