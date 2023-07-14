package ml.docilealligator.infinityforreddit.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Converter;

/**
 * A {@link Converter} for {@link SortType.Type sort type} and {@link SortType.Time sort time} to
 * {@link String} parameters
 */
public class SortTypeConverter<T> implements Converter<T, String> {
    /* package */ static SortTypeConverter<Object> INSTANCE = new SortTypeConverter<>();

    @Nullable
    @Override
    public String convert(@NonNull T value) throws IOException {
        if (value instanceof SortType.Type) {
            return ((SortType.Type) value).value;
        } else if (value instanceof SortType.Time) {
            return ((SortType.Time) value).value;
        } else {
            return null;
        }
    }
}
