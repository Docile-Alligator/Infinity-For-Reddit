package ml.docilealligator.infinityforreddit.mod

import com.google.gson.annotations.SerializedName

data class ModMessage(
    @SerializedName("body") var body: String? = null,
    @SerializedName("author") var author: Author? = Author(),
    @SerializedName("isInternal") var isInternal: Boolean? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("bodyMarkdown") var bodyMarkdown: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("participatingAs") var participatingAs: String? = null
)
