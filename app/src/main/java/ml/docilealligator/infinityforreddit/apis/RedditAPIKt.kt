package ml.docilealligator.infinityforreddit.apis

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface RedditAPIKt {
    @GET("/api/multi/multipath?expand_srs=true")
    suspend fun getMultiRedditInfo(
        @HeaderMap headers: MutableMap<String, String>,
        @Query("multipath") multipath: String
    ): String
}