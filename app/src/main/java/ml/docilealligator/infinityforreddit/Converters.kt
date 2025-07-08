package ml.docilealligator.infinityforreddit

import androidx.room.TypeConverter
import ml.docilealligator.infinityforreddit.comment.DraftType

class Converters {
    @TypeConverter
    fun fromDraftType(value: DraftType): String {
        return value.name
    }

    @TypeConverter
    fun toDraftType(value: String): DraftType {
        return DraftType.valueOf(value)
    }
}