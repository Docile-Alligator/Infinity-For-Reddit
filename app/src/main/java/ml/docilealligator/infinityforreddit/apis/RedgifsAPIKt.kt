package ml.docilealligator.infinityforreddit.apis

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RedgifsAPIKt {
    @GET("/v2/gifs/{id}")
    suspend fun getRedgifsData(
        @HeaderMap headers: Map<String, String>,
        @Path("id") id: String,
        @Query("user-agent") userAgent: String
    ): Response<String>

    @FormUrlEncoded
    @POST("/v2/oauth/client")
    suspend fun getRedgifsAccessToken(@FieldMap params: Map<String, String>): Response<String>

    @GET("/v2/auth/temporary")
    suspend fun getRedgifsTemporaryToken(): Response<String>
}