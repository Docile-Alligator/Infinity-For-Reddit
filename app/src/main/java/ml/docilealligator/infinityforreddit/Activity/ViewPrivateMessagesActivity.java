package ml.docilealligator.infinityforreddit.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Adapter.PrivateMessagesDetailRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.LoadUserDataAsyncTask;
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
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String USER_AVATAR_STATE = "UAS";
    @BindView(R.id.linear_layout_view_private_messages_activity)
    LinearLayout mLinearLayout;
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
    @BindView(R.id.edit_text_divider_view_private_messages_activity)
    View mDivider;
    @BindView(R.id.edit_text_view_private_messages_activity)
    EditText mEditText;
    @BindView(R.id.send_image_view_view_private_messages_activity)
    ImageView mSendImageView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
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
    private String mAccountName;
    private String mUserAvatar;
    private ArrayList<ProvideUserAvatarCallback> provideUserAvatarCallbacks;
    private LoadUserDataAsyncTask loadUserDataAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_private_messages);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK_FROM_POST_DETAIL, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(mAppBarLayout);
        }

        Intent intent = getIntent();
        privateMessage = intent.getParcelableExtra(EXTRA_PRIVATE_MESSAGE);

        if (privateMessage != null) {
            mToolbar.setTitle(privateMessage.getSubject());
        }
        setSupportActionBar(mToolbar);
        setToolbarGoToTop(mToolbar);

        provideUserAvatarCallbacks = new ArrayList<>();

        if (savedInstanceState != null) {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            mUserAvatar = savedInstanceState.getString(USER_AVATAR_STATE);

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
                mAccountName = account.getUsername();
            }
            bindView();
        }).execute();
    }

    private void bindView() {
        mAdapter = new PrivateMessagesDetailRecyclerViewAdapter(this, privateMessage, mAccountName, mCustomThemeWrapper);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void fetchUserAvatar(String username, ProvideUserAvatarCallback provideUserAvatarCallback) {
        if (mUserAvatar == null) {
            provideUserAvatarCallbacks.add(provideUserAvatarCallback);
            if (loadUserDataAsyncTask == null) {
                loadUserDataAsyncTask = new LoadUserDataAsyncTask(mRedditDataRoomDatabase.userDao(), username, mRetrofit, iconImageUrl -> {
                    mUserAvatar = iconImageUrl;
                    for (ProvideUserAvatarCallback provideUserAvatarCallbackInArrayList : provideUserAvatarCallbacks) {
                        provideUserAvatarCallbackInArrayList.fetchAvatarSuccess(iconImageUrl);
                    }
                    provideUserAvatarCallbacks.clear();
                });
                loadUserDataAsyncTask.execute();
            }
        } else {
            provideUserAvatarCallback.fetchAvatarSuccess(mUserAvatar);
        }
    }

    public void delayTransition() {
        TransitionManager.beginDelayedTransition(mRecyclerView, new AutoTransition());
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putString(USER_AVATAR_STATE, mUserAvatar);
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
        mLinearLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(mAppBarLayout, mToolbar);
        mDivider.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        mSendImageView.setColorFilter(Color.parseColor("#4185F4"), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onLongPress() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public interface ProvideUserAvatarCallback {
        void fetchAvatarSuccess(String userAvatarUrl);
    }
}