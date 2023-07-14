package ml.docilealligator.infinityforreddit.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A {@link Converter.Factory} for {@link SortType.Type sort type} and {@link SortType.Time sort time} to
 * {@link String} parameters
 */
public class SortTypeConverterFactory extends Converter.Factory {
    public static SortTypeConverterFactory create() {
        return new SortTypeConverterFactory();
    }

    @Nullable
    @Override
    public Converter<?, String> stringConverter(@NonNull Type type, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        if (type == SortType.Type.class || type == SortType.Time.class) {
            return SortTypeConverter.INSTANCE;
        }
        return null;
    }
}
