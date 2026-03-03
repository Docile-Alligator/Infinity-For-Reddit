package ml.docilealligator.infinityforreddit.apis

import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query

interface RedditAPIKt {
    @GET("/api/multi/multipath?expand_srs=true&raw_json=1")
    suspend fun getMultiRedditInfo(
        @HeaderMap headers: Map<String, String>,
        @Query("multipath") multipath: String
    ): String

    @FormUrlEncoded
    @POST("/api/multi/copy?expand_srs=true")
    suspend fun copyMultiReddit(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): String
}