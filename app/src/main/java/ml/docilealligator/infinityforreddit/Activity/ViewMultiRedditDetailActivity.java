package ml.docilealligator.infinityforreddit.Activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.Fragment.PostFragment;
import ml.docilealligator.infinityforreddit.Fragment.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.PostDataSource;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class ViewMultiRedditDetailActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback {

    public static final String EXTRA_MULTIREDDIT_DATA = "EMD";

    private static final String FRAGMENT_OUT_STATE_KEY = "FOSK";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";

    @BindView(R.id.coordinator_layout_view_multi_reddit_detail_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_view_multi_reddit_detail)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_multi_reddit_detail_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.icon_gif_image_view_view_multi_reddit_detail_activity)
    GifImageView iconGifImageView;
    @BindView(R.id.duplicate_multi_reddit_chip_view_multi_reddit_detail_activity)
    Chip duplicateSubredditChip;
    @BindView(R.id.multi_reddit_name_text_view_view_multi_reddit_detail_activity)
    TextView multiRedditNameTextView;
    @BindView(R.id.owner_text_view_view_multi_reddit_detail_activity)
    TextView ownerTextView;
    @BindView(R.id.subreddits_count_view_multi_reddit_detail_activity)
    TextView subredditsCountTextView;
    @BindView(R.id.description_text_view_view_multi_reddit_detail_activity)
    TextView descriptionTextView;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    SharedPreferences mSharedPreferences;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String multiPath;
    private boolean isInLazyMode = false;
    private boolean showToast = false;
    private Fragment mFragment;
    private SortTypeBottomSheetFragment sortTypeBottomSheetFragment;
    private SortTimeBottomSheetFragment sortTimeBottomSheetFragment;
    private PostLayoutBottomSheetFragment postLayoutBottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_multi_reddit_detail);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Resources resources = getResources();

            if ((resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet))
                    && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
                Window window = getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

                boolean lightNavBar = false;
                if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                    lightNavBar = true;
                }
                boolean finalLightNavBar = lightNavBar;

                View decorView = window.getDecorView();
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                        if (state == State.COLLAPSED) {
                            if (finalLightNavBar) {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                        } else if (state == State.EXPANDED) {
                            if (finalLightNavBar) {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                        }
                    }
                });

                int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResourceId > 0) {
                    showToast = true;
                }
            }
        }

        MultiReddit multiReddit = getIntent().getParcelableExtra(EXTRA_MULTIREDDIT_DATA);
        if (multiReddit == null) {
            Toast.makeText(this, R.string.multi_reddit_listing_activity, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        multiPath = multiReddit.getPath();
        multiRedditNameTextView.setText(multiReddit.getDisplayName());
        ownerTextView.setText(multiReddit.getOwner());

        if (savedInstanceState != null) {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);

            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE_KEY);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_multi_reddit_detail_activity, mFragment).commit();

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndInitializeFragment();
            }
        } else {
            getCurrentAccountAndInitializeFragment();
        }

        sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
        sortTypeBottomSheetFragment.setArguments(bottomSheetBundle);

        sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();

        postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
    }

    private void getCurrentAccountAndInitializeFragment() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
            }
            initializeFragment();
        }).execute();
    }

    private void initializeFragment() {
        mFragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PostFragment.EXTRA_NAME, multiPath);
        bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_MULTI_REDDIT);
        bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
        bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
        mFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_multi_reddit_detail_activity, mFragment).commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE_KEY, mFragment);
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public void postLayoutSelected(int postLayout) {

    }
}
