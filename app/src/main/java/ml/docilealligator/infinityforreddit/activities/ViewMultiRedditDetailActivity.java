package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.asynctasks.DeleteMultiredditInDatabase;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.RefreshMultiRedditsEvent;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.multireddit.DeleteMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.readpost.InsertReadPost;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class ViewMultiRedditDetailActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback, ActivityToolbarInterface, MarkPostAsReadInterface {

    public static final String EXTRA_MULTIREDDIT_DATA = "EMD";
    public static final String EXTRA_MULTIREDDIT_PATH = "EMP";

    private static final String FRAGMENT_OUT_STATE_KEY = "FOSK";

    @BindView(R.id.coordinator_layout_view_multi_reddit_detail_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_view_multi_reddit_detail_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_multi_reddit_detail_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_view_multi_reddit_detail_activity)
    Toolbar toolbar;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mAccessToken;
    private String mAccountName;
    private String multiPath;
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

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    coordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(toolbar);
            }
        }

        MultiReddit multiReddit = getIntent().getParcelableExtra(EXTRA_MULTIREDDIT_DATA);
        if (multiReddit == null) {
            multiPath = getIntent().getStringExtra(EXTRA_MULTIREDDIT_PATH);
            if (multiPath != null) {
                toolbar.setTitle(multiPath.substring(multiPath.lastIndexOf("/", multiPath.length() - 2) + 1));
            } else {
                Toast.makeText(this, R.string.error_getting_multi_reddit_data, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            multiPath = multiReddit.getPath();
            toolbar.setTitle(multiReddit.getDisplayName());
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(toolbar);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, "-");

        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE_KEY);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_multi_reddit_detail_activity, mFragment).commit();
        } else {
            initializeFragment();
        }

        sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
        sortTypeBottomSheetFragment.setArguments(bottomSheetBundle);

        sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();

        postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
    }

    private void initializeFragment() {
        mFragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PostFragment.EXTRA_NAME, multiPath);
        bundle.putInt(PostFragment.EXTRA_POST_TYPE, mAccessToken == null ? PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT : PostPagingSource.TYPE_MULTI_REDDIT);
        bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
        bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
        mFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_multi_reddit_detail_activity, mFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_multi_reddit_detail_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_sort_view_multi_reddit_detail_activity) {
            sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
            return true;
        } else if (itemId == R.id.action_search_view_multi_reddit_detail_activity) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_refresh_view_multi_reddit_detail_activity) {
            if (mFragment instanceof FragmentCommunicator) {
                ((FragmentCommunicator) mFragment).refresh();
            }
            return true;
        } else if (itemId == R.id.action_change_post_layout_view_multi_reddit_detail_activity) {
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        } else if (itemId == R.id.action_edit_view_multi_reddit_detail_activity) {
            Intent editIntent = new Intent(this, EditMultiRedditActivity.class);
            editIntent.putExtra(EditMultiRedditActivity.EXTRA_MULTI_PATH, multiPath);
            startActivity(editIntent);
            return true;
        } else if (itemId == R.id.action_delete_view_multi_reddit_detail_activity) {
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.delete_multi_reddit_dialog_message)
                    .setPositiveButton(R.string.delete, (dialogInterface, i)
                            -> {
                        if (mAccessToken == null) {
                            DeleteMultiredditInDatabase.deleteMultiredditInDatabase(mExecutor, new Handler(), mRedditDataRoomDatabase, mAccountName, multiPath,
                                    () -> {
                                        Toast.makeText(this, R.string.delete_multi_reddit_success, Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        } else {
                            DeleteMultiReddit.deleteMultiReddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase,
                                    mAccessToken, mAccountName, multiPath, new DeleteMultiReddit.DeleteMultiRedditListener() {
                                        @Override
                                        public void success() {
                                            Toast.makeText(ViewMultiRedditDetailActivity.this,
                                                    R.string.delete_multi_reddit_success, Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().post(new RefreshMultiRedditsEvent());
                                            finish();
                                        }

                                        @Override
                                        public void failed() {
                                            Toast.makeText(ViewMultiRedditDetailActivity.this,
                                                    R.string.delete_multi_reddit_failed, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE_KEY, mFragment);
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        ((PostFragment) mFragment).changeSortType(sortType);
        displaySortType();
    }

    @Override
    public void sortTypeSelected(String sortType) {
        Bundle bundle = new Bundle();
        bundle.putString(SortTimeBottomSheetFragment.EXTRA_SORT_TYPE, sortType);
        sortTimeBottomSheetFragment.setArguments(bundle);
        sortTimeBottomSheetFragment.show(getSupportFragmentManager(), sortTimeBottomSheetFragment.getTag());
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        if (mFragment != null) {
            mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE + multiPath, postLayout).apply();
            ((FragmentCommunicator) mFragment).changePostLayout(postLayout);
        }
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
    }

    @Override
    public void onLongPress() {
        if (mFragment != null) {
            ((PostFragment) mFragment).goBackToTop();
        }
    }

    @Override
    public void displaySortType() {
        if (mFragment != null) {
            SortType sortType = ((PostFragment) mFragment).getSortType();
            Utils.displaySortTypeInToolbar(sortType, toolbar);
        }
    }

    @Override
    public void markPostAsRead(Post post) {
        InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, mAccountName, post.getId());
    }
}
