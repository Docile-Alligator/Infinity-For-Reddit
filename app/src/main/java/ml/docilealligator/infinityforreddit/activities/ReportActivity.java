package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.FetchRules;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.ReportReason;
import ml.docilealligator.infinityforreddit.ReportThing;
import ml.docilealligator.infinityforreddit.Rule;
import ml.docilealligator.infinityforreddit.adapters.ReportReasonRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class ReportActivity extends BaseActivity {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_THING_FULLNAME = "ETF";
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
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
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

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFullname = getIntent().getStringExtra(EXTRA_THING_FULLNAME);
        mSubredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        if (savedInstanceState != null) {
            generalReasons = savedInstanceState.getParcelableArrayList(GENERAL_REASONS_STATE);
            rulesReasons = savedInstanceState.getParcelableArrayList(RULES_REASON_STATE);
        }

        if (generalReasons != null) {
            mAdapter = new ReportReasonRecyclerViewAdapter(this, mCustomThemeWrapper, generalReasons);
        } else {
            mAdapter = new ReportReasonRecyclerViewAdapter(this, mCustomThemeWrapper, ReportReason.getGeneralReasons(this));
        }
        recyclerView.setAdapter(mAdapter);

        if (rulesReasons == null) {
            FetchRules.fetchRules(mExecutor, new Handler(), mRetrofit, mSubredditName, new FetchRules.FetchRulesListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_send_report_activity) {
            ReportReason reportReason = mAdapter.getSelectedReason();
            if (reportReason != null) {
                Toast.makeText(ReportActivity.this, R.string.reporting, Toast.LENGTH_SHORT).show();
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
        if (mAdapter != null) {
            outState.putParcelableArrayList(GENERAL_REASONS_STATE, mAdapter.getGeneralReasons());
            outState.putParcelableArrayList(RULES_REASON_STATE, mAdapter.getRules());
        }
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
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, null, toolbar);
    }
}
