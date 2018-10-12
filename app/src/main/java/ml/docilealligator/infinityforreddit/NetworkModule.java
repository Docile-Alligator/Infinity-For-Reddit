package ml.docilealligator.infinityforreddit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
class NetworkModule {
    @Provides @Named("oauth")
    @Singleton
    Retrofit provideOauthRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(RedditUtils.OAUTH_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides @Named("no_oauth")
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(RedditUtils.API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.interceptors().add(new OkHttpInterceptor());
        return okHttpClient;
    }
}
