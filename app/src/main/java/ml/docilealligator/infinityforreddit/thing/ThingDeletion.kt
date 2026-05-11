package ml.docilealligator.infinityforreddit.thing

import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.utils.APIUtils
import retrofit2.Retrofit

suspend fun deleteThing(
    oauthRetrofit: Retrofit,
    fullname: String,
    accessToken: String
): Boolean {
    val params: MutableMap<String, String> = mutableMapOf<String, String>().apply {
        put(APIUtils.ID_KEY, fullname)
    }
    try {
        val response = oauthRetrofit.create(RedditAPIKt::class.java)
            .delete(APIUtils.getOAuthHeader(accessToken), params)

        return response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}