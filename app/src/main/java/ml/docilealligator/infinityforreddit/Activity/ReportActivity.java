package ml.docilealligator.infinityforreddit.Activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Adapter.ReportReasonRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.FetchRules;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.ReportReason;
import ml.docilealligator.infinityforreddit.ReportThing;
import ml.docilealligator.infinityforreddit.Rule;
import retrofit2.Retrofit;

public class ReportActivity extends BaseActivity {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_THING_FULLNAME = "ETF";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String GENERAL_REASONS_STATE = "GRS";
    private static final String RULES_REASON_STATE = "RRS";

    @BindView(R.id.coordinator_layout_report_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_report_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_report_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_report_activity)
    RecyclerView recyclerView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mFullname;
    private String mSubredditName;
    private ArrayList<ReportReason> generalReasons;
    private ArrayList<ReportReason> rulesReasons;
    private ReportReasonRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFullname = getIntent().getStringExtra(EXTRA_THING_FULLNAME);
        mSubredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);

        if (savedInstanceState != null) {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccount();
            }

            generalReasons = savedInstanceState.getParcelableArrayList(GENERAL_REASONS_STATE);
            rulesReasons = savedInstanceState.getParcelableArrayList(RULES_REASON_STATE);
        } else {
            getCurrentAccount();
        }

        if (generalReasons != null) {
            mAdapter = new ReportReasonRecyclerViewAdapter(mCustomThemeWrapper, generalReasons);
        } else {
            mAdapter = new ReportReasonRecyclerViewAdapter(mCustomThemeWrapper, ReportReason.getGeneralReasons(this));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        if (rulesReasons == null) {
            FetchRules.fetchRules(mRetrofit, mSubredditName, new FetchRules.FetchRulesListener() {
                @Override
                public void success(ArrayList<Rule> rules) {
                    mAdapter.setRules(ReportReason.convertRulesToReasons(rules));
                }

                @Override
                public void failed() {
                    Snackbar.make(coordinatorLayout, R.string.error_loading_rules_without_retry, Snackbar.LENGTH_SHORT).show();
                }
            });
        } else {
            mAdapter.setRules(rulesReasons);
        }
    }

    private void getCurrentAccount() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
            }
        }).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_report_activity:
                ReportReason reportReason = mAdapter.getSelectedReason();
                if (reportReason != null) {
                    ReportThing.reportThing(mOauthRetrofit, mAccessToken, mFullname, mSubredditName,
                            reportReason.getReasonType(), reportReason.getReportReason(), new ReportThing.ReportThingListener() {
                                @Override
                                public void success() {
                                    Toast.makeText(ReportActivity.this, R.string.report_successful, Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(ReportActivity.this, R.string.report_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(ReportActivity.this, R.string.report_reason_not_selected, Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        if (mAdapter != null) {
            outState.putParcelableArrayList(GENERAL_REASONS_STATE, mAdapter.getGeneralReasons());
            outState.putParcelableArrayList(RULES_REASON_STATE, mAdapter.getRules());
        }
    }

    @Override
    protected SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
    }
}
