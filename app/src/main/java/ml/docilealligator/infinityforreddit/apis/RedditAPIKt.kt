package ml.docilealligator.infinityforreddit.apis

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query

interface RedditAPIKt {
    @GET("/api/mod/conversations")
    suspend fun getModMailConversations(
        @HeaderMap headers: Map<String, String>,
        @Query("after") after: String?
    ): Response<String>

    @GET("/api/mod/conversations/{id}")
    suspend fun getModMailConversation(
        @Path("id") id: String,
        @HeaderMap headers: Map<String, String>
    ): Response<String>
}