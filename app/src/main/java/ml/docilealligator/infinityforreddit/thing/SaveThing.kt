package ml.docilealligator.infinityforreddit.thing

import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.utils.APIUtils
import retrofit2.Retrofit

suspend fun saveThing(
    oauthRetrofit: Retrofit,
    accessToken: String,
    fullname: String
): Boolean {
    val params: MutableMap<String, String> = mutableMapOf<String, String>().apply {
        put(APIUtils.ID_KEY, fullname)
    }
    try {
        val response = oauthRetrofit.create(RedditAPIKt::class.java)
            .save(APIUtils.getOAuthHeader(accessToken), params)

        return response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

suspend fun unsaveThing(
    oauthRetrofit: Retrofit,
    accessToken: String,
    fullname: String
): Boolean {
    val params: MutableMap<String, String> = mutableMapOf<String, String>().apply {
        put(APIUtils.ID_KEY, fullname)
    }
    try {
        val response = oauthRetrofit.create(RedditAPIKt::class.java)
            .unsave(APIUtils.getOAuthHeader(accessToken), params)

        return response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}