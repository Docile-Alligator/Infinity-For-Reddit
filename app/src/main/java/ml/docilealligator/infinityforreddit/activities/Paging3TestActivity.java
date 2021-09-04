package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import im.ene.toro.exoplayer.ExoCreator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.adapters.Paging3TestAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.post.PostDataSource;
import ml.docilealligator.infinityforreddit.post.PostPaging3ViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class Paging3TestActivity extends BaseActivity {

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("paging_3_test")
    Retrofit mPaging3Retrofit;
    @Inject
    @Named("gfycat")
    Retrofit mGfycatRetrofit;
    @Inject
    @Named("redgifs")
    Retrofit mRedgifsRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("post_history")
    SharedPreferences mPostHistorySharedPreferences;
    @Inject
    @Named("post_feed_scrolled_position_cache")
    SharedPreferences mPostFeedScrolledPositionSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    ExoCreator mExoCreator;
    @Inject
    Executor mExecutor;
    public PostPaging3ViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paging3_test);

        String accessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        String accountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);
        int defaultPostLayout = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_POST_LAYOUT_KEY, "0"));
        boolean savePostFeedScrolledPosition = mSharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_FRONT_PAGE_SCROLLED_POSITION, false);
        boolean rememberMutingOptionInPostFeed = mSharedPreferences.getBoolean(SharedPreferencesUtils.REMEMBER_MUTING_OPTION_IN_POST_FEED, false);
        Locale locale = getResources().getConfiguration().locale;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowWidth = displayMetrics.widthPixels;
        int postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST, defaultPostLayout);
        int postType = PostDataSource.TYPE_FRONT_PAGE;
        String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_BEST_POST, SortType.Type.BEST.name());
        SortType sortType;
        if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
            String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_BEST_POST, SortType.Time.ALL.name());
            sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
        } else {
            sortType = new SortType(SortType.Type.valueOf(sort));
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        Paging3TestAdapter adapter = new Paging3TestAdapter(this, new PostFragment(), mExecutor,
                mOauthRetrofit, mGfycatRetrofit,
                mRedgifsRetrofit, mCustomThemeWrapper, locale,
                windowWidth, accessToken, accountName, postType, postLayout, true,
                mSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostHistorySharedPreferences,
                mExoCreator, new Paging3TestAdapter.Callback() {
            @Override
            public void retryLoadingMore() {

            }

            @Override
            public void typeChipClicked(int filter) {

            }

            @Override
            public void flairChipClicked(String flair) {

            }

            @Override
            public void nsfwChipClicked() {

            }

            @Override
            public void currentlyBindItem(int position) {

            }

            @Override
            public void delayTransition() {
                TransitionManager.beginDelayedTransition(recyclerView, new AutoTransition());
            }
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new PostPaging3ViewModel.Factory(mExecutor, mPaging3Retrofit,
                accessToken, accountName, mSharedPreferences, mPostFeedScrolledPositionSharedPreferences,
                null, null, null, postType, sortType, null, null,
                null, null, null)).get(PostPaging3ViewModel.class);

        viewModel.getPosts().observe(this, postPagingData -> adapter.submitData(getLifecycle(), postPagingData));
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {

    }
}