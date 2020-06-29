package ml.docilealligator.infinityforreddit.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.r0adkll.slidr.Slidr;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Adapter.PrivateMessagesDetailRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.Message;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class ViewPrivateMessagesActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_PRIVATE_MESSAGE = "EPM";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    @BindView(R.id.coordinator_layout_view_private_messages_activity)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_private_messages_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.appbar_layout_view_private_messages_activity)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_view_private_messages_activity)
    Toolbar mToolbar;
    @BindView(R.id.recycler_view_view_private_messages)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab_view_private_messages_activity)
    FloatingActionButton mFab;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private LinearLayoutManager mLinearLayoutManager;
    private PrivateMessagesDetailRecyclerViewAdapter mAdapter;
    private Message privateMessage;
    private boolean mNullAccessToken = false;
    private String mAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_private_messages);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK_FROM_POST_DETAIL, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(mAppBarLayout);
            }

            if (isImmersiveInterface()) {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                adjustToolbar(mToolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    mRecyclerView.setPadding(0, 0, 0, navBarHeight);
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
                    params.bottomMargin = navBarHeight;
                    mFab.setLayoutParams(params);
                }
            }
        }

        Intent intent = getIntent();
        privateMessage = intent.getParcelableExtra(EXTRA_PRIVATE_MESSAGE);

        if (privateMessage != null) {
            mToolbar.setTitle(privateMessage.getTitle());
        }
        setSupportActionBar(mToolbar);
        setToolbarGoToTop(mToolbar);

        if (savedInstanceState != null) {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }
        } else {
            getCurrentAccountAndBindView();
        }
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
            }
            bindView();
        }).execute();
    }

    private void bindView() {
        mAdapter = new PrivateMessagesDetailRecyclerViewAdapter(this, privateMessage, mCustomThemeWrapper);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
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
        mCoordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(mAppBarLayout, mToolbar);
        applyFABTheme(mFab);
    }

    @Override
    public void onLongPress() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }
}