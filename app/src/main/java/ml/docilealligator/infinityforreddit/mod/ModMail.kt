package ml.docilealligator.infinityforreddit.mod

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.io.IOException

data class ModMail(
    @SerializedName("viewerId") var viewerId: String? = null
) {
    lateinit var conversations: MutableList<Conversation>;
    lateinit var messages: MutableList<ModMessage>;
    val conversationIds: MutableList<String> = arrayListOf()

    fun parseConversations(conversationsJSONObject: JSONObject, gson: Gson) {
        for (conversationId in conversationIds) {
            try {
                conversations.add(gson.fromJson(conversationsJSONObject.getString(conversationId), Conversation::class.java))
            } catch (ignore: IOException) {
                ignore.printStackTrace()
            }
        }
    }

    fun parseModMessages(messagesJSONObject: JSONObject, gson: Gson) {
        for (conversation in conversations) {
            for (objId in conversation.objIds) {
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
        }
    }
}
