package ml.docilealligator.infinityforreddit.apis

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface StreamableAPIKt {
    @GET("videos/{shortcode}")
    suspend fun getStreamableData(@Path("shortcode") shortCode: String): Response<String>
}