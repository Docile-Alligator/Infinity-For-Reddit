package ml.docilealligator.infinityforreddit.user

import androidx.annotation.WorkerThread
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.database.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.result.APIError
import ml.docilealligator.infinityforreddit.result.AppResult
import ml.docilealligator.infinityforreddit.utils.APIUtils
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit

suspend fun fetchUserData(
    redditDataRoomDatabase: RedditDataRoomDatabase,
    oauthRetrofit: Retrofit,
    accessToken: String?,
    username: String
): AppResult<UserData, APIError> {
    try {
        redditDataRoomDatabase.userDaoKt().getUserData(username)?.let {
            return AppResult.Success(it)
        }

        val response: Response<String> = oauthRetrofit.create(RedditAPIKt::class.java)
            .getUserData(APIUtils.getOAuthHeader(accessToken), username)
        return if (response.isSuccessful) {
            withContext(Dispatchers.Default) {
                AppResult.Success(processFetchUserDataResponse(
                    response,
                    redditDataRoomDatabase
                ))
            }
        } else {
            AppResult.Error(APIError.Message(response.errorBody()?.string() ?: ""))
        }
    } catch (e: Exception) {
        if (e is CancellationException) {
            throw e
        }

        return AppResult.Error(APIError.Message(e.message ?: ""))
    }
}

@WorkerThread
@Throws(JSONException::class)
private fun processFetchUserDataResponse(
    response: Response<String>,
    redditDataRoomDatabase: RedditDataRoomDatabase?
): UserData {
    val jsonResponse = JSONObject(response.body())
    val userData = FetchUserData.parseUserDataBase(jsonResponse, true)
    redditDataRoomDatabase?.accountDao()?.updateAccountInfo(
        userData.name,
        userData.iconUrl,
        userData.banner,
        userData.totalKarma,
        userData.isMod
    )

    return userData
}