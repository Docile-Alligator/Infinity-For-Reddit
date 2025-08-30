package ml.docilealligator.infinityforreddit.apis

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OhMyDlAPI {
    @FormUrlEncoded
    @POST("/api/download")
    fun getRedgifsData(
        @FieldMap params: Map<String, String>
    ): Call<String>
}