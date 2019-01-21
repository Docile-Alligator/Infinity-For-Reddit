package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.felipecsl.gifimageview.library.GifImageView;

import javax.inject.Inject;
import javax.inject.Named;

import SubredditDatabase.SubredditDao;
import SubredditDatabase.SubredditData;
import SubredditDatabase.SubredditRoomDatabase;
import SubredditDatabase.SubredditViewModel;
import SubscribedSubredditDatabase.SubscribedSubredditDao;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditRoomDatabase;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import retrofit2.Retrofit;

public class ViewSubredditDetailActivity extends AppCompatActivity {

    static final String EXTRA_SUBREDDIT_NAME_KEY = "ESN";

    private static final String FRAGMENT_OUT_STATE_KEY = "FOSK";

    @BindView(R.id.coordinator_layout_view_subreddit_detail_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.banner_image_view_view_subreddit_detail_activity) ImageView bannerImageView;
    @BindView(R.id.icon_gif_image_view_view_subreddit_detail_activity) GifImageView iconGifImageView;
    @BindView(R.id.subscribe_subreddit_chip_view_subreddit_detail_activity) Chip subscribeSubredditChip;
    @BindView(R.id.subreddit_name_text_view_view_subreddit_detail_activity) TextView subredditNameTextView;
    @BindView(R.id.subscriber_count_text_view_view_subreddit_detail_activity) TextView nSubscribersTextView;
    @BindView(R.id.online_subscriber_count_text_view_view_subreddit_detail_activity) TextView nOnlineSubscribersTextView;
    @BindView(R.id.description_text_view_view_subreddit_detail_activity) TextView descriptionTextView;

    private boolean subscriptionReady = false;

    private RequestManager glide;
    private Fragment mFragment;

    private SubscribedSubredditDao subscribedSubredditDao;
    private SubredditViewModel mSubredditViewModel;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    @Named("auth_info")
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_subreddit_detail);
        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmNetworkComponent().inject(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get status bar height
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        params.topMargin = statusBarHeight;

        String subredditName = getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME_KEY);
        String title = "r/" + subredditName;
        subredditNameTextView.setText(title);

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout_view_subreddit_detail_activity);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout_view_subreddit_detail_activity);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int previousVerticalOffset = 0;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                } else {
                    if(verticalOffset < previousVerticalOffset) {
                        //Scroll down
                        if(scrollRange - Math.abs(verticalOffset) <= toolbar.getHeight()) {
                            collapsingToolbarLayout.setTitle(title);
                        }
                    } else {
                        //Scroll up
                        if(scrollRange - Math.abs(verticalOffset) > toolbar.getHeight()) {
                            collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                        }
                    }
                    previousVerticalOffset = verticalOffset;
                }
            }
        });

        subscribedSubredditDao = SubscribedSubredditRoomDatabase.getDatabase(this).subscribedSubredditDao();
        glide = Glide.with(this);

        mSubredditViewModel = ViewModelProviders.of(this, new SubredditViewModel.Factory(getApplication(), subredditName))
                .get(SubredditViewModel.class);
        mSubredditViewModel.getSubredditLiveData().observe(this, subredditData -> {
            if(subredditData != null) {
                if(subredditData.getBannerUrl().equals("")) {
                    iconGifImageView.setOnClickListener(view -> {
                        //Do nothing as it has no image
                    });
                } else {
                    glide.load(subredditData.getBannerUrl()).into(bannerImageView);
                    bannerImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, subredditData.getBannerUrl());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName + "-banner");
                        startActivity(intent);
                    });
                }

                if(subredditData.getIconUrl().equals("")) {
                    glide.load(getDrawable(R.drawable.subreddit_default_icon))
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .into(iconGifImageView);
                    iconGifImageView.setOnClickListener(view -> {
                        //Do nothing as it is a default icon
                    });
                } else {
                    glide.load(subredditData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    if(resource instanceof Animatable) {
                                        //This is a gif
                                        ((Animatable) resource).start();
                                        iconGifImageView.startAnimation();
                                    }
                                    return false;
                                }
                            })
                            .into(iconGifImageView);
                    iconGifImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, subredditData.getIconUrl());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName + "-icon");
                        startActivity(intent);
                    });
                }

                String subredditFullName = "r/" + subredditData.getName();
                subredditNameTextView.setText(subredditFullName);
                String nSubscribers = getString(R.string.subscribers_number_detail, subredditData.getNSubscribers());
                nSubscribersTextView.setText(nSubscribers);
                if(subredditData.getDescription().equals("")) {
                    descriptionTextView.setVisibility(View.GONE);
                } else {
                    descriptionTextView.setVisibility(View.VISIBLE);
                    descriptionTextView.setText(subredditData.getDescription());
                }
            }
        });

        subscribeSubredditChip.setOnClickListener(view -> {
            if(subscriptionReady) {
                subscriptionReady = false;
                if(subscribeSubredditChip.getText().equals(getResources().getString(R.string.subscribe))) {
                    SubredditSubscription.subscribeToSubreddit(mOauthRetrofit, mRetrofit, sharedPreferences,
                            subredditName, subscribedSubredditDao, new SubredditSubscription.SubredditSubscriptionListener() {
                                @Override
                                public void onSubredditSubscriptionSuccess() {
                                    subscribeSubredditChip.setText(R.string.unsubscribe);
                                    subscribeSubredditChip.setChipBackgroundColor(getResources().getColorStateList(R.color.colorAccent));
                                    makeSnackbar(R.string.subscribed);
                                    subscriptionReady = true;
                                }

                                @Override
                                public void onSubredditSubscriptionFail() {
                                    makeSnackbar(R.string.subscribe_failed);
                                    subscriptionReady = true;
                                }
                            });
                } else {
                    SubredditSubscription.unsubscribeToSubreddit(mOauthRetrofit, sharedPreferences,
                            subredditName, subscribedSubredditDao, new SubredditSubscription.SubredditSubscriptionListener() {
                                @Override
                                public void onSubredditSubscriptionSuccess() {
                                    subscribeSubredditChip.setText(R.string.subscribe);
                                    subscribeSubredditChip.setChipBackgroundColor(getResources().getColorStateList(R.color.colorPrimaryDark));
                                    makeSnackbar(R.string.unsubscribed);
                                    subscriptionReady = true;
                                }

                                @Override
                                public void onSubredditSubscriptionFail() {
                                    makeSnackbar(R.string.unsubscribe_failed);
                                    subscriptionReady = true;
                                }
                            });
                }
            }
        });

        new CheckIsSubscribedToSubredditAsyncTask(subscribedSubredditDao, subredditName,
                new CheckIsSubscribedToSubredditAsyncTask.CheckIsSubscribedToSubredditListener() {
            @Override
            public void isSubscribed() {
                subscribeSubredditChip.setText(R.string.unsubscribe);
                subscribeSubredditChip.setChipBackgroundColor(getResources().getColorStateList(R.color.colorAccent));
                subscriptionReady = true;
            }

            @Override
            public void isNotSubscribed() {
                subscribeSubredditChip.setText(R.string.subscribe);
                subscribeSubredditChip.setChipBackgroundColor(getResources().getColorStateList(R.color.colorPrimaryDark));
                subscriptionReady = true;
            }
        }).execute();

        FetchSubredditData.fetchSubredditData(mRetrofit, subredditName, new FetchSubredditData.FetchSubredditDataListener() {
            @Override
            public void onFetchSubredditDataSuccess(String response) {
                ParseSubredditData.parseSubredditData(response, new ParseSubredditData.ParseSubredditDataListener() {
                    @Override
                    public void onParseSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                        new InsertSubredditDataAsyncTask(SubredditRoomDatabase.getDatabase(ViewSubredditDetailActivity.this), subredditData)
                                .execute();
                        String nOnlineSubscribers = getString(R.string.online_subscribers_number_detail, nCurrentOnlineSubscribers);
                        nOnlineSubscribersTextView.setText(nOnlineSubscribers);
                    }

                    @Override
                    public void onParseSubredditDataFail() {
                        makeSnackbar(R.string.cannot_fetch_subreddit_info);
                    }
                });
            }

            @Override
            public void onFetchSubredditDataFail() {

            }
        });

        if(savedInstanceState == null) {
            mFragment = new PostFragment();
            Bundle bundle = new Bundle();
            bundle.putString(PostFragment.NAME_KEY, subredditName);
            bundle.putInt(PostFragment.POST_TYPE_KEY, PostDataSource.TYPE_SUBREDDIT);
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_subreddit_detail_activity, mFragment).commit();
        } else {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE_KEY);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_subreddit_detail_activity, mFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_subreddit_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh_view_subreddit_detail_activity:
                if(mFragment instanceof FragmentCommunicator) {
                    ((FragmentCommunicator) mFragment).refresh();
                }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE_KEY, mFragment);
        }
    }

    private void makeSnackbar(int resId) {
        Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
    }

    private static class InsertSubredditDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private SubredditDao mSubredditDao;
        private SubredditData subredditData;

        InsertSubredditDataAsyncTask(SubredditRoomDatabase subredditDb, SubredditData subredditData) {
            mSubredditDao = subredditDb.subredditDao();
            this.subredditData = subredditData;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mSubredditDao.insert(subredditData);
            return null;
        }
    }

    private static class CheckIsSubscribedToSubredditAsyncTask extends AsyncTask<Void, Void, Void> {

        private SubscribedSubredditDao subscribedSubredditDao;
        private String subredditName;
        private SubscribedSubredditData subscribedSubredditData;
        private CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener;

        interface CheckIsSubscribedToSubredditListener {
            void isSubscribed();
            void isNotSubscribed();
        }

        CheckIsSubscribedToSubredditAsyncTask(SubscribedSubredditDao subscribedSubredditDao, String subredditName,
                                              CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener) {
            this.subscribedSubredditDao = subscribedSubredditDao;
            this.subredditName =subredditName;
            this.checkIsSubscribedToSubredditListener = checkIsSubscribedToSubredditListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            subscribedSubredditData = subscribedSubredditDao.getSubscribedSubreddit(subredditName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(subscribedSubredditData != null) {
                checkIsSubscribedToSubredditListener.isSubscribed();
            } else {
                checkIsSubscribedToSubredditListener.isNotSubscribed();
            }
        }
    }
}
