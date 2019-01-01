package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import retrofit2.Retrofit;

public class ViewSubredditDetailActivity extends AppCompatActivity {

    static final String EXTRA_SUBREDDIT_NAME_KEY = "ESN";
    static final String EXTRA_SUBREDDIT_VALUE_KEY = "ESV";
    static final String EXTRA_QUERY_BY_ID_KEY = "EQBI";

    private static final String FRAGMENT_OUT_STATE_KEY = "FOSK";

    @BindView(R.id.banner_image_view_view_subreddit_detail_activity) ImageView bannerImageView;
    @BindView(R.id.icon_gif_image_view_view_subreddit_detail_activity) GifImageView iconGifImageView;
    @BindView(R.id.subreddit_name_text_view_view_subreddit_detail_activity) TextView subredditNameTextView;
    @BindView(R.id.subscriber_count_text_view_view_subreddit_detail_activity) TextView nSubscribersTextView;
    @BindView(R.id.online_subscriber_count_text_view_view_subreddit_detail_activity) TextView nOnlineSubscribersTextView;
    @BindView(R.id.description_text_view_view_subreddit_detail_activity) TextView descriptionTextView;

    private RequestManager glide;
    private Fragment mFragment;

    private SubredditViewModel mSubredditViewModel;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

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

        final String subredditName = getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME_KEY);

        final String title = "r/" + subredditName;
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout_view_subreddit_detail_activity);
        final AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout_view_subreddit_detail_activity);

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

        glide = Glide.with(ViewSubredditDetailActivity.this);

        String value = getIntent().getExtras().getString(EXTRA_SUBREDDIT_VALUE_KEY);
        boolean queryById = getIntent().getExtras().getBoolean(EXTRA_QUERY_BY_ID_KEY);
        SubredditViewModel.Factory factory = new SubredditViewModel.Factory(getApplication(), value, queryById);
        mSubredditViewModel = ViewModelProviders.of(this, factory).get(SubredditViewModel.class);
        mSubredditViewModel.getSubredditLiveData().observe(this, new Observer<SubredditData>() {
            @Override
            public void onChanged(@Nullable final SubredditData subredditData) {
                if(subredditData != null) {
                    if(subredditData.getBannerUrl().equals("")) {
                        iconGifImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Do nothing as it has no image
                            }
                        });
                    } else {
                        glide.load(subredditData.getBannerUrl()).into(bannerImageView);
                        bannerImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageActivity.class);
                                intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                                intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, subredditData.getBannerUrl());
                                intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName + "-banner");
                                startActivity(intent);
                            }
                        });
                    }

                    if(subredditData.getIconUrl().equals("")) {
                        glide.load(getDrawable(R.drawable.subreddit_default_icon))
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                                .into(iconGifImageView);
                        iconGifImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Do nothing as it is a default icon
                            }
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
                        iconGifImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageActivity.class);
                                intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                                intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, subredditData.getIconUrl());
                                intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName + "-icon");
                                startActivity(intent);
                            }
                        });
                    }

                    subredditNameTextView.setText(subredditData.getName());
                    String nSubscribers = getString(R.string.subscribers_number_detail, subredditData.getNSubscribers());
                    nSubscribersTextView.setText(nSubscribers);
                    if(subredditData.getDescription().equals("")) {
                        descriptionTextView.setVisibility(View.GONE);
                    } else {
                        descriptionTextView.setVisibility(View.VISIBLE);
                        descriptionTextView.setText(subredditData.getDescription());
                    }
                }
            }
        });

        FetchSubredditData.fetchSubredditData(mRetrofit, subredditName, new FetchSubredditData.FetchSubredditDataListener() {
            @Override
            public void onFetchSubredditDataSuccess(String response) {
                ParseSubredditData.parseComment(response, new ParseSubredditData.ParseSubredditDataListener() {
                    @Override
                    public void onParseSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                        new InsertSubredditDataAsyncTask(SubredditRoomDatabase.getDatabase(ViewSubredditDetailActivity.this), subredditData)
                                .execute();
                        String nOnlineSubscribers = getString(R.string.online_subscribers_number_detail, nCurrentOnlineSubscribers);
                        nOnlineSubscribersTextView.setText(nOnlineSubscribers);
                    }

                    @Override
                    public void onParseSubredditDataFail() {
                        Toast.makeText(ViewSubredditDetailActivity.this, "Cannot fetch subreddit info", Toast.LENGTH_SHORT).show();
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
            bundle.putString(PostFragment.SUBREDDIT_NAME_KEY, subredditName);
            bundle.putBoolean(PostFragment.IS_BEST_POST_KEY, false);
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
}
