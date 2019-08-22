package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class ViewMessageActivity extends AppCompatActivity {

    static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";

    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";

    @BindView(R.id.collapsing_toolbar_layout_view_message_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.appbar_layout_view_message_activity) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_view_message_activity) Toolbar toolbar;
    @BindView(R.id.progress_bar_view_message_activity) CircleProgressBar mProgressBar;
    @BindView(R.id.recycler_view_view_message_activity) RecyclerView recyclerView;
    @BindView(R.id.fetch_messages_info_linear_layout_view_message_activity) LinearLayout mFetchMessageInfoLinearLayout;
    @BindView(R.id.fetch_messages_info_image_view_view_message_activity) ImageView mFetchMessageInfoImageView;
    @BindView(R.id.fetch_messages_info_text_view_view_message_activity) TextView mFetchMessageInfoTextView;

    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mNewAccountName;

    private MessageRecyclerViewAdapter mAdapter;

    private RequestManager mGlide;

    MessageViewModel mMessageViewModel;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Inject
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        EventBus.getDefault().register(this);

        mGlide = Glide.with(this);

        Resources resources = getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
                && (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                || resources.getBoolean(R.bool.isTablet))) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            boolean lightNavBar = false;
            if((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                lightNavBar = true;
            }
            boolean finalLightNavBar = lightNavBar;

            View decorView = window.getDecorView();
            if(finalLightNavBar) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        if(finalLightNavBar) {
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        }
                    } else if (state == State.EXPANDED) {
                        if(finalLightNavBar) {
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        }
                    }
                }
            });

            int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarResourceId > 0) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
                toolbar.setLayoutParams(params);
            }

            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                recyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int themeType = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        switch (themeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                break;
            case 2:
                if(systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                }

        }

        toolbar.setTitle(R.string.inbox);
        setSupportActionBar(toolbar);

        if(savedInstanceState != null) {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);

            if(!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndFetchMessage();
            } else {
                bindView();
            }
        } else {
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
            getCurrentAccountAndFetchMessage();
        }
    }

    private void getCurrentAccountAndFetchMessage() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(mNewAccountName != null) {
                if(account == null || !account.getUsername().equals(mNewAccountName)) {
                    new SwitchAccountAsyncTask(mRedditDataRoomDatabase, mNewAccountName, newAccount -> {
                        EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                        Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                        mNewAccountName = null;
                        if(newAccount == null) {
                            mNullAccessToken = true;
                        } else {
                            mAccessToken = newAccount.getAccessToken();
                        }

                        bindView();
                    }).execute();
                } else {
                    mAccessToken = account.getAccessToken();
                    bindView();
                }
            } else {
                if(account == null) {
                    mNullAccessToken = true;
                } else {
                    mAccessToken = account.getAccessToken();
                }

                bindView();
            }
        }).execute();
    }

    private void bindView() {
        mAdapter = new MessageRecyclerViewAdapter(this, mOauthRetrofit, mAccessToken);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        MessageViewModel.Factory factory = new MessageViewModel.Factory(mOauthRetrofit,
                getResources().getConfiguration().locale, mAccessToken, FetchMessages.WHERE_INBOX);
        mMessageViewModel = new ViewModelProvider(this, factory).get(MessageViewModel.class);
        mMessageViewModel.getMessages().observe(this, messages -> mAdapter.submitList(messages));

        mMessageViewModel.getInitialLoadingState().observe(this, networkState -> {
            if(networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if(networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mFetchMessageInfoLinearLayout.setOnClickListener(view -> mMessageViewModel.retry());
                showErrorView(R.string.load_messages_failed);
            } else {
                mFetchMessageInfoLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mMessageViewModel.hasMessage().observe(this, hasMessage -> {
            if(hasMessage) {
                mFetchMessageInfoLinearLayout.setVisibility(View.GONE);
            } else {
                mFetchMessageInfoLinearLayout.setOnClickListener(view -> {
                    //Do nothing
                });
                showErrorView(R.string.no_messages);
            }
        });

        mMessageViewModel.getPaginationNetworkState().observe(this, networkState -> {
            mAdapter.setNetworkState(networkState);
        });
    }

    private void showErrorView(int stringResId) {
        mProgressBar.setVisibility(View.GONE);
        mFetchMessageInfoLinearLayout.setVisibility(View.VISIBLE);
        mFetchMessageInfoTextView.setText(stringResId);
        mGlide.load(R.drawable.load_post_error_indicator).into(mFetchMessageInfoImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_message_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_refresh_view_message_activity) {
            mMessageViewModel.refresh();
            return true;
        } else if(item.getItemId() == android.R.id.home) {
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
        outState.putString(NEW_ACCOUNT_NAME_STATE, mNewAccountName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if(!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }
}
