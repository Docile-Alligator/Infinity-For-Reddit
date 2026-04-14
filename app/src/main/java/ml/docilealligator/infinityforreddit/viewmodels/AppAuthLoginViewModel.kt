package ml.docilealligator.infinityforreddit.viewmodels

import android.content.SharedPreferences
import android.net.Uri
import android.text.Html
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.account.Account
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.events.NewUserLoggedInEvent
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException

class AppAuthLoginViewModel(
    private val mRetrofit: Retrofit,
    private val mOauthRetrofit: Retrofit,
    private val mRedditDataRoomDatabase: RedditDataRoomDatabase,
    private val mCurrentAccountSharedPreferences: SharedPreferences
): ViewModel() {
    private val _accountFetched = MutableStateFlow(false)
    val accountFetched = _accountFetched.asStateFlow()

    private val _errorMessageId = MutableStateFlow<Int?>(null)
    val errorMessageId = _errorMessageId.asStateFlow()

    fun setError(errorMessageId: Int) {
        _errorMessageId.value = errorMessageId
    }

    fun clearError() {
        _errorMessageId.value = null
    }

    fun setUpAccount(uri: Uri) {
        viewModelScope.launch {
            val state = uri.getQueryParameter("state")
            if (state == APIUtils.STATE) {
                val authCode = uri.getQueryParameter("code")
                authCode?.let {
                    val params: MutableMap<String, String> = HashMap()
                    params[APIUtils.GRANT_TYPE_KEY] = "authorization_code"
                    params["code"] = it
                    params[APIUtils.REDIRECT_URI_KEY] = APIUtils.REDIRECT_URI

                    val api: RedditAPIKt = mRetrofit.create(RedditAPIKt::class.java)

                    try {
                        val accessTokenResponse = api.getAccessToken(APIUtils.getHttpBasicAuthHeader(), params)
                        val responseJSON = JSONObject(accessTokenResponse)
                        val accessToken = responseJSON.getString(APIUtils.ACCESS_TOKEN_KEY)
                        val refreshToken = responseJSON.getString(APIUtils.REFRESH_TOKEN_KEY)

                        _accountFetched.value = fetchAccountInfo(accessToken, refreshToken, it)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        _errorMessageId.value = R.string.retrieve_token_error
                    } catch (e: HttpException) {
                        e.printStackTrace()
                        _errorMessageId.value = R.string.retrieve_token_error
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        _errorMessageId.value = R.string.parse_json_response_error
                    }
                } ?: run {
                    val error = uri.getQueryParameter("error")
                    error?.let {
                        _errorMessageId.value = R.string.login_failed_result_url_error
                    } ?: run {
                        _errorMessageId.value = R.string.login_failed_result_url_unknown_error
                    }
                }
            } else {
                _errorMessageId.value = R.string.something_went_wrong
            }
        }
    }

    private suspend fun fetchAccountInfo(accessToken: String, refreshToken: String, code: String): Boolean {
        try {
            val accountResponse = mOauthRetrofit.create(RedditAPIKt::class.java)
                .getMyInfo(APIUtils.getOAuthHeader(accessToken))
            val jsonResponse = JSONObject(accountResponse)
            val name = jsonResponse.getString(JSONUtils.NAME_KEY)
            val profileImageUrl =
                Html.fromHtml(jsonResponse.getString(JSONUtils.ICON_IMG_KEY)).toString()
            val bannerImageUrl = if (!jsonResponse.isNull(JSONUtils.SUBREDDIT_KEY)) Html.fromHtml(
                jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY)
                    .getString(JSONUtils.BANNER_IMG_KEY)
            ).toString() else null
            val karma = jsonResponse.getInt(JSONUtils.TOTAL_KARMA_KEY)
            val isMod = jsonResponse.getBoolean(JSONUtils.IS_MOD_KEY)

            val accountDao = mRedditDataRoomDatabase.accountDaoKt()
            accountDao
                .updateAccountInfo(name, profileImageUrl, bannerImageUrl, karma, isMod)

            mCurrentAccountSharedPreferences.edit {
                putString(
                    SharedPreferencesUtils.ACCESS_TOKEN,
                    accessToken
                )
                putString(SharedPreferencesUtils.ACCOUNT_NAME, name)
                putString(
                    SharedPreferencesUtils.ACCOUNT_IMAGE_URL,
                    profileImageUrl
                )
                remove(SharedPreferencesUtils.SUBSCRIBED_THINGS_SYNC_TIME)
            }

            val account = Account(
                name, accessToken, refreshToken, code, profileImageUrl,
                bannerImageUrl, karma, true, isMod
            )

            accountDao.markAllAccountsNonCurrent()
            accountDao.insert(account)

            return true
        } catch (e: IOException) {
            e.printStackTrace()
            _errorMessageId.value = R.string.cannot_fetch_user_info
        } catch (e: HttpException) {
            e.printStackTrace()
            _errorMessageId.value = R.string.cannot_fetch_user_info
        } catch (e: JSONException) {
            e.printStackTrace()
            _errorMessageId.value = R.string.parse_user_info_error
        }

        return false
    }

    companion object {
        fun provideFactory(
            retrofit: Retrofit,
            oauthRetrofit: Retrofit,
            redditRoomDatabase: RedditDataRoomDatabase,
            currentAccountSharedPreferences: SharedPreferences
        ) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return AppAuthLoginViewModel(
                        retrofit,
                        oauthRetrofit,
                        redditRoomDatabase,
                        currentAccountSharedPreferences
                    ) as T
                }
            }
        }
    }
}