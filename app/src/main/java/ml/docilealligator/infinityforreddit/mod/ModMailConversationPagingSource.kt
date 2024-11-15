package ml.docilealligator.infinityforreddit.mod

import android.content.SharedPreferences
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.apis.RedditAPI
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.IOException

class ModMailConversationPagingSource(val retrofit: Retrofit, val accessToken: String, val sharedPreferences: SharedPreferences): PagingSource<String, Conversation>() {
    override fun getRefreshKey(state: PagingState<String, Conversation>): String? {
        return null;
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Conversation> {
        try {
            val response = retrofit.create(RedditAPIKt::class.java)
                .getModMailConversations(APIUtils.getOAuthHeader(accessToken), params.key)

            if (response.isSuccessful) {
                response.body()?.let {
                    val json = JSONObject(it)
                    val conversationIdsArray = json.getJSONArray(JSONUtils.CONVERSATION_IDS_KEY)
                    if (conversationIdsArray.length() == 0) {
                        return LoadResult.Page(listOf(), null, null)
                    }

                    val gson = Gson()
                    val conversations: MutableList<Conversation> = mutableListOf()

                    val messagesJSONObject = json.getJSONObject(JSONUtils.MESSAGES_KEY)
                    for (i in 0 until conversationIdsArray.length()) {
                        val conversationId = conversationIdsArray.getString(i)
                        try {
                            conversations.add(gson.fromJson(json.getJSONObject(JSONUtils.CONVERSATIONS_KEY).getString(conversationId), Conversation::class.java).apply {
                                for (objId in objIds) {
                                    objId.key?.let { key ->
                                        if (key == "messages") {
                                            objId.id?.let { id ->
                                                try {
                                                    messages.add(gson.fromJson(messagesJSONObject.getString(id), ModMessage::class.java))
                                                } catch (ignore: IOException) {
                                                    ignore.printStackTrace()
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        } catch (ignore: IOException) {
                            ignore.printStackTrace()
                        }
                    }

                    return LoadResult.Page(
                        conversations, null, conversationIdsArray.getString(conversationIdsArray.length() - 1)
                    )
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return LoadResult.Error(Exception("Error getting response"))
    }
}