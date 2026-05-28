package ml.docilealligator.infinityforreddit.apis

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface VReddItKt {
    @GET
    suspend fun getRedirectUrl(@Url vReddItUrl: String): Response<String>
}