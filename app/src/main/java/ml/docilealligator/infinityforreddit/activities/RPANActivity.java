package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RPANBroadcast;
import ml.docilealligator.infinityforreddit.apis.Strapi;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.fragments.ViewRPANBroadcastFragment;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RPANActivity extends BaseActivity {

    @BindView(R.id.coordinator_layout_rpan_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_rpan_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_rpan_activity)
    Toolbar toolbar;
    @BindView(R.id.view_pager_2_rpan_activity)
    ViewPager2 viewPager2;
    @Inject
    @Named("strapi")
    Retrofit strapiRetrofit;
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
    private String mAccessToken;
    private String mAccountName;
    private ArrayList<RPANBroadcast> rpanBroadcasts;
    private String nextCursor;
    private SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpanactivity);

        ButterKnife.bind(this);

        applyCustomTheme();

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

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        loadRPANVideos();
    }

    private void loadRPANVideos() {
        strapiRetrofit.create(Strapi.class).getAllBroadcasts(APIUtils.getOAuthHeader(mAccessToken)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    parseRPANBroadcasts(response.body());
                } else {
                    Toast.makeText(RPANActivity.this,
                            R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(RPANActivity.this,
                        R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseRPANBroadcasts(String response) {
        Handler handler = new Handler();
        mExecutor.execute(() -> {
            try {
                ArrayList<RPANBroadcast> rpanBroadcasts = new ArrayList<>();
                JSONObject responseObject = new JSONObject(response);
                String nextCursor = responseObject.getString(JSONUtils.NEXT_CURSOR_KEY);

                JSONArray dataArray = responseObject.getJSONArray(JSONUtils.DATA_KEY);
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject singleData = dataArray.getJSONObject(i);
                    JSONObject rpanPostObject = singleData.getJSONObject(JSONUtils.POST_KEY);
                    RPANBroadcast.RPANPost rpanPost = new RPANBroadcast.RPANPost(
                            rpanPostObject.getString(JSONUtils.TITLE_KEY),
                            rpanPostObject.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.NAME_KEY),
                            rpanPostObject.getJSONObject(JSONUtils.SUBREDDIT_KEY).getJSONObject(JSONUtils.STYLES_KEY).getString(JSONUtils.ICON_KEY),
                            rpanPostObject.getJSONObject(JSONUtils.AUTHOR_INFO_KEY).getString(JSONUtils.NAME_KEY),
                            rpanPostObject.getInt(JSONUtils.SCORE_KEY),
                            rpanPostObject.getString(JSONUtils.VOTE_STATE_KEY),
                            rpanPostObject.getDouble(JSONUtils.UPVOTE_RATIO_CAMEL_CASE_KEY),
                            rpanPostObject.getString(JSONUtils.PERMALINK_KEY),
                            rpanPostObject.getJSONObject(JSONUtils.OUTBOUND_LINK_KEY).getString(JSONUtils.URL_KEY),
                            rpanPostObject.getBoolean(JSONUtils.IS_NSFW_KEY),
                            rpanPostObject.getBoolean(JSONUtils.IS_LOCKED_KEY),
                            rpanPostObject.getBoolean(JSONUtils.IS_ARCHIVED_KEY),
                            rpanPostObject.getBoolean(JSONUtils.IS_SPOILER),
                            rpanPostObject.getString(JSONUtils.SUGGESTED_COMMENT_SORT_CAMEL_CASE_KEY),
                            rpanPostObject.getString(JSONUtils.LIVE_COMMENTS_WEBSOCKET_KEY)
                    );

                    JSONObject rpanStreamObject = singleData.getJSONObject(JSONUtils.STREAM_KEY);
                    RPANBroadcast.RPANStream rpanStream = new RPANBroadcast.RPANStream(
                            rpanStreamObject.getString(JSONUtils.STREAM_ID_KEY),
                            rpanStreamObject.getString(JSONUtils.HLS_URL_KEY),
                            rpanStreamObject.getString(JSONUtils.THUMBNAIL_KEY),
                            rpanStreamObject.getInt(JSONUtils.WIDTH_KEY),
                            rpanStreamObject.getInt(JSONUtils.HEIGHT_KEY),
                            rpanStreamObject.getLong(JSONUtils.PUBLISH_AT_KEY),
                            rpanStreamObject.getString(JSONUtils.STATE_KEY)
                    );

                    rpanBroadcasts.add(new RPANBroadcast(
                            singleData.getInt(JSONUtils.UPVOTES_KEY),
                            singleData.getInt(JSONUtils.DOWNVOTES_KEY),
                            singleData.getInt(JSONUtils.UNIQUE_WATCHERS_KEY),
                            singleData.getInt(JSONUtils.CONTINUOUS_WATCHERS_KEY),
                            singleData.getInt(JSONUtils.TOTAL_CONTINUOUS_WATCHERS_KEY),
                            singleData.getBoolean(JSONUtils.CHAT_DISABLED_KEY),
                            singleData.getDouble(JSONUtils.BROADCAST_TIME_KEY),
                            singleData.getDouble(JSONUtils.ESTIMATED_REMAINING_TIME_KEY),
                            rpanPost,
                            rpanStream
                    ));
                }

                handler.post(() -> {
                    RPANActivity.this.rpanBroadcasts = rpanBroadcasts;
                    RPANActivity.this.nextCursor = nextCursor;

                    initializeViewPager();
                });
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(RPANActivity.this,
                        R.string.parse_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void initializeViewPager() {
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setUserInputEnabled(!mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false));
        fixViewPager2Sensitivity(viewPager2);
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

    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {

        public SectionsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            ViewRPANBroadcastFragment fragment = new ViewRPANBroadcastFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(ViewRPANBroadcastFragment.EXTRA_RPAN_BROADCAST, rpanBroadcasts.get(position));
            fragment.setArguments(bundle);
            return fragment;
        }

        @Nullable
        private Fragment getCurrentFragment() {
            if (viewPager2 == null || getSupportFragmentManager() == null) {
                return null;
            }
            return getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
        }

        @Override
        public int getItemCount() {
            return rpanBroadcasts == null ? 0 : rpanBroadcasts.size();
        }
    }
}