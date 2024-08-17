package ml.docilealligator.infinityforreddit.customtheme;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.concurrent.Executor;

import kotlinx.coroutines.CoroutineScope;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import retrofit2.Retrofit;

public class OnlineCustomThemeRepository {
    private final LiveData<PagingData<OnlineCustomThemeMetadata>> customThemes;
    private MutableLiveData<OnlineCustomThemeFilter> onlineCustomThemeFilterMutableLiveData;

    public OnlineCustomThemeRepository(Executor executor, Retrofit retrofit,
                                       RedditDataRoomDatabase redditDataRoomDatabase, CoroutineScope viewModelScope) {
        onlineCustomThemeFilterMutableLiveData = new MutableLiveData<>(new OnlineCustomThemeFilter());

        Pager<String, OnlineCustomThemeMetadata> pager = new Pager<>(new PagingConfig(25, 4, false, 10),
                () -> new OnlineCustomThemePagingSource(executor, retrofit, redditDataRoomDatabase));

        customThemes = PagingLiveData.cachedIn(Transformations.switchMap(onlineCustomThemeFilterMutableLiveData,
                customThemeFilter -> PagingLiveData.getLiveData(pager)), viewModelScope);
    }

    public LiveData<PagingData<OnlineCustomThemeMetadata>> getOnlineCustomThemeMetadata() {
        return customThemes;
    }

    public void changeOnlineCustomThemeFilter(OnlineCustomThemeFilter onlineCustomThemeFilter) {
        onlineCustomThemeFilterMutableLiveData.postValue(onlineCustomThemeFilter);
    }

}
