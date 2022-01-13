package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.evernote.android.state.State;
import com.google.android.material.snackbar.Snackbar;
import com.livefront.bridge.Bridge;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RPANBroadcast;
import ml.docilealligator.infinityforreddit.apis.Strapi;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontFamily;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.fragments.ViewRPANBroadcastFragment;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RPANActivity extends AppCompatActivity implements CustomFontReceiver {

    public static final String EXTRA_RPAN_BROADCAST_FULLNAME_OR_ID = "ERBFOI";

    @BindView(R.id.coordinator_layout_rpan_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_pager_2_rpan_activity)
    ViewPager2 viewPager2;
    @BindView(R.id.progress_bar_rpan_activity)
    ProgressBar progressBar;
    @Inject
    @Named("strapi")
    Retrofit strapiRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    @State
    ArrayList<RPANBroadcast> rpanBroadcasts;
    @State
    String nextCursor;
    public Typeface typeface;
    private SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        getTheme().applyStyle(R.style.Theme_Normal, true);

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(FontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(TitleFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(ContentFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name())).getResId(), true);

        setContentView(R.layout.activity_rpanactivity);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Bridge.restoreInstanceState(this, savedInstanceState);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        actionBar.setTitle(Utils.getTabTextWithCustomFont(typeface, Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.rpan_activity_label) + "</font>")));

        if (rpanBroadcasts == null) {
            loadRPANVideos();
        } else {
            initializeViewPager();
        }
    }

    private void loadRPANVideos() {
        String rpanBroadcastFullNameOrId = getIntent().getStringExtra(EXTRA_RPAN_BROADCAST_FULLNAME_OR_ID);
        if (rpanBroadcastFullNameOrId == null) {
            strapiRetrofit.create(Strapi.class).getAllBroadcasts().enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        parseRPANBroadcasts(response.body());
                    } else {
                        try {
                            ResponseBody responseBody = response.errorBody();
                            if (responseBody != null) {
                                JSONObject errorObject = new JSONObject(responseBody.string());
                                String errorMessage = errorObject.getString(JSONUtils.DATA_KEY);
                                if (!errorMessage.isEmpty() && !errorMessage.equals("null")) {
                                    Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RPANActivity.this,
                                            R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(RPANActivity.this,
                                        R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RPANActivity.this,
                                    R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RPANActivity.this,
                            R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            strapiRetrofit.create(Strapi.class).getRPANBroadcast(rpanBroadcastFullNameOrId).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Handler handler = new Handler();
                        mExecutor.execute(() -> {
                            try {
                                rpanBroadcasts = new ArrayList<>();
                                rpanBroadcasts.add(parseSingleRPANBroadcast(new JSONObject(response.body()).getJSONObject(JSONUtils.DATA_KEY)));
                                handler.post(() -> initializeViewPager());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                handler.post(() -> Toast.makeText(RPANActivity.this,
                                        R.string.parse_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show());
                            }
                        });
                    } else {
                        try {
                            ResponseBody responseBody = response.errorBody();
                            if (responseBody != null) {
                                JSONObject errorObject = new JSONObject(responseBody.string());
                                String errorMessage = errorObject.getString(JSONUtils.DATA_KEY);
                                if (!errorMessage.isEmpty() && !errorMessage.equals("null")) {
                                    Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RPANActivity.this,
                                            R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(RPANActivity.this,
                                        R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RPANActivity.this,
                                    R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RPANActivity.this,
                            R.string.load_rpan_broadcasts_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                    try {
                        JSONObject singleData = dataArray.getJSONObject(i);
                        rpanBroadcasts.add(parseSingleRPANBroadcast(singleData));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    private RPANBroadcast parseSingleRPANBroadcast(JSONObject singleData) throws JSONException {
        JSONObject rpanPostObject = singleData.getJSONObject(JSONUtils.POST_KEY);
        RPANBroadcast.RPANPost rpanPost = new RPANBroadcast.RPANPost(
                rpanPostObject.getString(JSONUtils.ID_KEY),
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

        return new RPANBroadcast(
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
        );
    }

    private void initializeViewPager() {
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(3);
        fixViewPager2Sensitivity(viewPager2);
    }

    private void fixViewPager2Sensitivity(ViewPager2 viewPager2) {
        try {
            Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);

            RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager2);

            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);

            Object touchSlopBox = touchSlopField.get(recyclerView);
            if (touchSlopBox != null) {
                int touchSlop = (int) touchSlopBox;
                touchSlopField.set(recyclerView, touchSlop * 4);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignore) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rpan_activity, menu);
        for (int i = 0; i < menu.size(); i++) {
            Utils.setTitleWithCustomFontToMenuItem(typeface, menu.getItem(i), null);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_share_rpan_link_rpan_activity) {
            if (rpanBroadcasts != null) {
                int position = viewPager2.getCurrentItem();
                if (position >= 0 && position < rpanBroadcasts.size()) {
                    shareLink(rpanBroadcasts.get(position).rpanPost.rpanUrl);
                    return true;
                }
            }
        } else if (item.getItemId() == R.id.action_share_post_link_rpan_activity) {
            if (rpanBroadcasts != null) {
                int position = viewPager2.getCurrentItem();
                if (position >= 0 && position < rpanBroadcasts.size()) {
                    shareLink(rpanBroadcasts.get(position).rpanPost.postPermalink);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void shareLink(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, link);
            startActivity(Intent.createChooser(intent, getString(R.string.share)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
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