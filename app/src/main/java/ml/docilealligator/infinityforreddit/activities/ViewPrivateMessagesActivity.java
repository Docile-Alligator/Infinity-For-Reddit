package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.PrivateMessagesDetailRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.asynctasks.LoadUserData;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.events.PassPrivateMessageEvent;
import ml.docilealligator.infinityforreddit.events.PassPrivateMessageIndexEvent;
import ml.docilealligator.infinityforreddit.events.RepliedToPrivateMessageEvent;
import ml.docilealligator.infinityforreddit.message.Message;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import ml.docilealligator.infinityforreddit.message.ReplyMessage;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class ViewPrivateMessagesActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_PRIVATE_MESSAGE_INDEX = "EPM";
    public static final String EXTRA_MESSAGE_POSITION = "EMP";
    private static final String USER_AVATAR_STATE = "UAS";
    private static final String PRIVATE_MESSAGES_STATE = "PMS";
    @BindView(R.id.coordinator_layout_view_private_messages_activity)
    CoordinatorLayout mCoordinatorLayout;
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
    @BindView(R.id.edit_text_wrapper_linear_layout_view_private_messages_activity)
    LinearLayout mEditTextLinearLayout;
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
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private PrivateMessagesDetailRecyclerViewAdapter mAdapter;
    private Message privateMessage;
    private String mAccessToken;
    private String mAccountName;
    private String mUserAvatar;
    private ArrayList<ProvideUserAvatarCallback> mProvideUserAvatarCallbacks;
    private boolean isLoadingUserAvatar = false;
    private boolean isSendingMessage = false;
    private int mSecondaryTextColor;
    private int mSendMessageIconColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        //setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_private_messages);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(mAppBarLayout);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(mAppBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(mToolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mEditTextLinearLayout.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    mEditTextLinearLayout.setLayoutParams(params);
                }
            }
        }

        setSupportActionBar(mToolbar);
        setToolbarGoToTop(mToolbar);

        mProvideUserAvatarCallbacks = new ArrayList<>();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        if (savedInstanceState != null) {
            mUserAvatar = savedInstanceState.getString(USER_AVATAR_STATE);
            privateMessage = savedInstanceState.getParcelable(PRIVATE_MESSAGES_STATE);
            if (privateMessage == null) {
                EventBus.getDefault().post(new PassPrivateMessageIndexEvent(getIntent().getIntExtra(EXTRA_PRIVATE_MESSAGE_INDEX, -1)));
            } else {
                bindView();
            }
        } else {
            EventBus.getDefault().post(new PassPrivateMessageIndexEvent(getIntent().getIntExtra(EXTRA_PRIVATE_MESSAGE_INDEX, -1)));
        }

        ViewCompat.setWindowInsetsAnimationCallback(
                mCoordinatorLayout,
                new WindowInsetsAnimationCompat.Callback(
                        WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
                ) {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                        Insets typesInset = insets.getInsets(WindowInsetsCompat.Type.ime());
                        Insets otherInset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        Insets diff = Insets.max(Insets.NONE, Insets.subtract(typesInset, otherInset));

                        mCoordinatorLayout.setPadding(0, 0, 0, diff.bottom);
                        return insets;
                    }
                });
    }

    private void bindView() {
        if (privateMessage != null) {
            if (privateMessage.getAuthor().equals(mAccountName)) {
                setTitle(privateMessage.getDestination());
                mToolbar.setOnClickListener(view -> {
                    Intent intent = new Intent(this, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, privateMessage.getDestination());
                    startActivity(intent);
                });
            } else {
                setTitle(privateMessage.getAuthor());
                mToolbar.setOnClickListener(view -> {
                    Intent intent = new Intent(this, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, privateMessage.getAuthor());
                    startActivity(intent);
                });
            }
        }
        mAdapter = new PrivateMessagesDetailRecyclerViewAdapter(this, mSharedPreferences,
                getResources().getConfiguration().locale, privateMessage, mAccountName, mCustomThemeWrapper);
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        goToBottom();
        mSendImageView.setOnClickListener(view -> {
            if (!isSendingMessage) {
                if (!mEditText.getText().toString().equals("")) {
                    //Send Message
                    if (privateMessage != null) {
                        Message replyTo;
                        ArrayList<Message> replies = privateMessage.getReplies();
                        if (replies != null && !replies.isEmpty()) {
                            replyTo = replies.get(replies.size() - 1);
                        } else {
                            replyTo = privateMessage;
                        }
                        if (replyTo != null) {
                            isSendingMessage = true;
                            mSendImageView.setColorFilter(mSecondaryTextColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            ReplyMessage.replyMessage(mEditText.getText().toString(), replyTo.getFullname(),
                                    getResources().getConfiguration().locale, mOauthRetrofit, mAccessToken,
                                    new ReplyMessage.ReplyMessageListener() {
                                        @Override
                                        public void replyMessageSuccess(Message message) {
                                            if (mAdapter != null) {
                                                mAdapter.addReply(message);
                                            }
                                            goToBottom();
                                            mEditText.setText("");
                                            mSendImageView.setColorFilter(mSendMessageIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                            isSendingMessage = false;
                                            EventBus.getDefault().post(new RepliedToPrivateMessageEvent(message, getIntent().getIntExtra(EXTRA_MESSAGE_POSITION, -1)));
                                        }

                                        @Override
                                        public void replyMessageFailed(String errorMessage) {
                                            if (errorMessage != null && !errorMessage.equals("")) {
                                                Snackbar.make(mCoordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                                            } else {
                                                Snackbar.make(mCoordinatorLayout, R.string.reply_message_failed, Snackbar.LENGTH_LONG).show();
                                            }
                                            mSendImageView.setColorFilter(mSendMessageIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                            isSendingMessage = false;
                                        }
                                    });
                            StringBuilder fullnames = new StringBuilder();
                            if (privateMessage.isNew()) {
                                fullnames.append(privateMessage.getFullname()).append(",");
                            }
                            if (replies != null && !replies.isEmpty()) {
                                for (Message m : replies) {
                                    if (m.isNew()) {
                                        fullnames.append(m).append(",");
                                    }
                                }
                            }
                            if (fullnames.length() > 0) {
                                fullnames.deleteCharAt(fullnames.length() - 1);
                                ReadMessage.readMessage(mOauthRetrofit, mAccessToken, fullnames.toString(),
                                        new ReadMessage.ReadMessageListener() {
                                            @Override
                                            public void readSuccess() {}

                                            @Override
                                            public void readFailed() {}
                                        });
                            }
                        } else {
                            isSendingMessage = false;
                            Snackbar.make(mCoordinatorLayout, R.string.error_getting_message, Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    public void fetchUserAvatar(String username, ProvideUserAvatarCallback provideUserAvatarCallback) {
        if (mUserAvatar == null) {
            mProvideUserAvatarCallbacks.add(provideUserAvatarCallback);
            if (!isLoadingUserAvatar) {
                LoadUserData.loadUserData(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        username, mRetrofit, iconImageUrl -> {
                    isLoadingUserAvatar = false;
                    mUserAvatar = iconImageUrl == null ? "" : iconImageUrl;
                    for (ProvideUserAvatarCallback provideUserAvatarCallbackInArrayList : mProvideUserAvatarCallbacks) {
                        provideUserAvatarCallbackInArrayList.fetchAvatarSuccess(iconImageUrl);
                    }
                    mProvideUserAvatarCallbacks.clear();
                });
            }
        } else {
            provideUserAvatarCallback.fetchAvatarSuccess(mUserAvatar);
        }
    }

    public void delayTransition() {
        TransitionManager.beginDelayedTransition(mRecyclerView, new AutoTransition());
    }

    private void goToBottom() {
        if (mLinearLayoutManager != null && mAdapter != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(mAdapter.getItemCount() - 1, 0);
        }
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
        outState.putString(USER_AVATAR_STATE, mUserAvatar);
        outState.putParcelable(PRIVATE_MESSAGES_STATE, privateMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(mAppBarLayout, null, mToolbar);
        mDivider.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        mEditText.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        mSecondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        mEditText.setHintTextColor(mSecondaryTextColor);
        mEditTextLinearLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        mSendMessageIconColor = mCustomThemeWrapper.getSendMessageIconColor();
        mSendImageView.setColorFilter(mSendMessageIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        if (typeface != null) {
            mEditText.setTypeface(typeface);
        }
    }

    @Override
    public void onLongPress() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    @Subscribe
    public void onPassPrivateMessageEvent(PassPrivateMessageEvent passPrivateMessageEvent) {
        privateMessage = passPrivateMessageEvent.message;
        if (privateMessage != null) {
            bindView();
        }
    }

    public interface ProvideUserAvatarCallback {
        void fetchAvatarSuccess(String userAvatarUrl);
    }
}