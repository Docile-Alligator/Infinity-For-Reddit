package ml.docilealligator.infinityforreddit.activities;

import android.content.Context;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.SwitchAccount;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.InboxFragment;
import ml.docilealligator.infinityforreddit.message.FetchMessage;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class InboxActivity extends BaseActivity implements ActivityToolbarInterface, RecyclerViewContentScrollingInterface {

    public static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";
    public static final String EXTRA_VIEW_MESSAGE = "EVM";

    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";
    private static final int SEARCH_USER_REQUEST_CODE = 1;

    @BindView(R.id.coordinator_layout_inbox_activity)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.appbar_layout_inbox_activity)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_inbox_activity)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.toolbar_inbox_activity)
    Toolbar mToolbar;
    @BindView(R.id.tab_layout_inbox_activity)
    TabLayout tabLayout;
    @BindView(R.id.view_pager_inbox_activity)
    ViewPager2 viewPager2;
    @BindView(R.id.fab_inbox_activity)
    FloatingActionButton fab;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
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
    private SlidrInterface mSlidrInterface;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private FragmentManager fragmentManager;
    private String mAccessToken;
    private String mAccountName;
    private String mNewAccountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inbox);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSlidrInterface = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(mAppBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    mCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(mToolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    fab.setLayoutParams(params);
                }
            }
        }

        mToolbar.setTitle(R.string.inbox);
        setSupportActionBar(mToolbar);
        setToolbarGoToTop(mToolbar);

        fragmentManager = getSupportFragmentManager();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        if (savedInstanceState != null) {
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);
        } else {
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
        }
        getCurrentAccountAndFetchMessage(savedInstanceState);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                fab.show();
            }
        });

        fab.setOnClickListener(view -> {
            View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, mCoordinatorLayout, false);
            TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
            thingEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                    Intent pmIntent = new Intent(this, SendPrivateMessageActivity.class);
                    pmIntent.putExtra(SendPrivateMessageActivity.EXTRA_RECIPIENT_USERNAME, thingEditText.getText().toString());
                    startActivity(pmIntent);
                    return true;
                }
                return false;
            });
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.choose_a_user)
                    .setView(rootView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                        }
                        Intent pmIntent = new Intent(this, SendPrivateMessageActivity.class);
                        pmIntent.putExtra(SendPrivateMessageActivity.EXTRA_RECIPIENT_USERNAME, thingEditText.getText().toString());
                        startActivity(pmIntent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.search, (dialogInterface, i) -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                        }

                        Intent intent = new Intent(this, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
                        startActivityForResult(intent, SEARCH_USER_REQUEST_CODE);
                    })
                    .setOnDismissListener(dialogInterface -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                        }
                    })
                    .show();
        });
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
        mCoordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(mAppBarLayout, mCollapsingToolbarLayout, mToolbar);
        applyTabLayoutTheme(tabLayout);
        applyFABTheme(fab);
    }

    private void getCurrentAccountAndFetchMessage(Bundle savedInstanceState) {
        if (mNewAccountName != null) {
            if (mAccountName == null || !mAccountName.equals(mNewAccountName)) {
                SwitchAccount.switchAccount(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                        mExecutor, new Handler(), mNewAccountName, newAccount -> {
                            EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                            Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                            mNewAccountName = null;
                            if (newAccount != null) {
                                mAccessToken = newAccount.getAccessToken();
                            }

                            bindView(savedInstanceState);
                        });
            } else {
                bindView(savedInstanceState);
            }
        } else {
            bindView(savedInstanceState);
        }
    }

    private void bindView(Bundle savedInstanceState) {
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }
            }
        });
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(2);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.notifications));
                    break;
                case 1:
                    Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.messages));
                    break;
            }
        }).attach();
        if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_VIEW_MESSAGE, false)) {
            viewPager2.setCurrentItem(1, false);
        }

        fixViewPager2Sensitivity(viewPager2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inbox_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh_inbox_activity) {
            if (sectionsPagerAdapter != null) {
                sectionsPagerAdapter.refresh();
            }
            return true;
        } else if (item.getItemId() == R.id.action_read_all_messages_inbox_activity) {
            if (mAccessToken != null) {
                Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
                mOauthRetrofit.create(RedditAPI.class).readAllMessages(APIUtils.getOAuthHeader(mAccessToken))
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(InboxActivity.this, R.string.read_all_messages_success, Toast.LENGTH_SHORT).show();
                                    if (sectionsPagerAdapter != null) {
                                        sectionsPagerAdapter.readAllMessages();
                                    }
                                } else {
                                    if (response.code() == 429) {
                                        Toast.makeText(InboxActivity.this, R.string.read_all_messages_time_limit, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(InboxActivity.this, R.string.read_all_messages_failed, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                Toast.makeText(InboxActivity.this, R.string.read_all_messages_failed, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SEARCH_USER_REQUEST_CODE && data != null) {
            String username = data.getStringExtra(SearchActivity.EXTRA_RETURN_USER_NAME);
            Intent intent = new Intent(this, SendPrivateMessageActivity.class);
            intent.putExtra(SendPrivateMessageActivity.EXTRA_RECIPIENT_USERNAME, username);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NEW_ACCOUNT_NAME_STATE, mNewAccountName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Override
    public void onLongPress() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.goBackToTop();
        }
    }

    private void lockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.lock();
        }
    }

    private void unlockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.unlock();
        }
    }

    @Override
    public void contentScrollUp() {
        fab.show();
    }

    @Override
    public void contentScrollDown() {
        fab.hide();
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {

        SectionsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Nullable
        private Fragment getCurrentFragment() {
            if (viewPager2 == null || fragmentManager == null) {
                return null;
            }
            return fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
        }

        void refresh() {
            InboxFragment fragment = (InboxFragment) getCurrentFragment();
            if (fragment != null) {
                fragment.refresh();
            }
        }

        void goBackToTop() {
            InboxFragment fragment = (InboxFragment) getCurrentFragment();
            if (fragment != null) {
                fragment.goBackToTop();
            }
        }

        void readAllMessages() {
            InboxFragment fragment = (InboxFragment) getCurrentFragment();
            if (fragment != null) {
                fragment.markAllMessagesRead();
            }
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                InboxFragment fragment = new InboxFragment();
                Bundle bundle = new Bundle();
                bundle.putString(InboxFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(InboxFragment.EXTRA_MESSAGE_WHERE, FetchMessage.WHERE_INBOX);
                fragment.setArguments(bundle);
                return fragment;
            } else {
                InboxFragment fragment = new InboxFragment();
                Bundle bundle = new Bundle();
                bundle.putString(InboxFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(InboxFragment.EXTRA_MESSAGE_WHERE, FetchMessage.WHERE_MESSAGES);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
