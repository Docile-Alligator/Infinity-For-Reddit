package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.html.tag.SuperScriptHandler;
import io.noties.markwon.inlineparser.AutolinkInlineProcessor;
import io.noties.markwon.inlineparser.BangInlineProcessor;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.MarkwonLinearLayoutManager;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.SpoilerParserPlugin;
import ml.docilealligator.infinityforreddit.markdown.SuperscriptInlineProcessor;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WikiActivity extends BaseActivity {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_WIKI_PATH = "EWP";
    private static final String WIKI_MARKDOWN_STATE = "WMS";

    @BindView(R.id.coordinator_layout_comment_wiki_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_comment_wiki_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_wiki_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_comment_wiki_activity)
    Toolbar toolbar;
    @BindView(R.id.swipe_refresh_layout_wiki_activity)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.content_markdown_view_comment_wiki_activity)
    RecyclerView markdownRecyclerView;
    @BindView(R.id.fetch_wiki_linear_layout_wiki_activity)
    LinearLayout mFetchWikiInfoLinearLayout;
    @BindView(R.id.fetch_wiki_image_view_wiki_activity)
    ImageView mFetchWikiInfoImageView;
    @BindView(R.id.fetch_wiki_text_view_wiki_activity)
    TextView mFetchWikiInfoTextView;

    @Inject
    @Named("no_oauth")
    Retrofit retrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private SlidrInterface mSlidrInterface;
    private String wikiMarkdown;
    private Markwon markwon;
    private MarkwonAdapter markwonAdapter;
    private boolean isRefreshing = false;
    private RequestManager mGlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSlidrInterface = Slidr.attach(this);
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
                markdownRecyclerView.setPadding(markdownRecyclerView.getPaddingLeft(), 0, markdownRecyclerView.getPaddingRight(), getNavBarHeight());
            }
        }

        mGlide = Glide.with(this);

        swipeRefreshLayout.setEnabled(mSharedPreferences.getBoolean(SharedPreferencesUtils.PULL_TO_REFRESH, true));
        swipeRefreshLayout.setOnRefreshListener(this::loadWiki);

        int markdownColor = mCustomThemeWrapper.getPrimaryTextColor();
        int spoilerBackgroundColor = markdownColor | 0xFF000000;
        int linkColor = mCustomThemeWrapper.getLinkColor();
        markwon = Markwon.builder(this)
                .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                    plugin.excludeInlineProcessor(AutolinkInlineProcessor.class);
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                    plugin.addInlineProcessor(new SuperscriptInlineProcessor());
                }))
                .usePlugin(HtmlPlugin.create(plugin -> {
                    plugin.excludeDefaults(true).addHandler(new SuperScriptHandler());
                }))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @NonNull
                    @Override
                    public String processMarkdown(@NonNull String markdown) {
                        return Utils.fixSuperScript(markdown);
                    }

                    @Override
                    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                        textView.setTextColor(markdownColor);
                    }

                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(WikiActivity.this, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            intent.setData(uri);
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder.linkColor(linkColor);
                    }
                })
                .usePlugin(SpoilerParserPlugin.create(markdownColor, spoilerBackgroundColor))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(MovementMethodPlugin.create(BetterLinkMovementMethod.linkify(Linkify.WEB_URLS).setOnLinkLongClickListener((textView, url) -> {
                    UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = new UrlMenuBottomSheetFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(UrlMenuBottomSheetFragment.EXTRA_URL, url);
                    urlMenuBottomSheetFragment.setArguments(bundle);
                    urlMenuBottomSheetFragment.show(getSupportFragmentManager(), urlMenuBottomSheetFragment.getTag());
                    return true;
                })))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(TableEntryPlugin.create(this))
                .build();

        markwonAdapter = MarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
                .include(TableBlock.class, TableEntry.create(builder -> builder
                        .tableLayout(R.layout.adapter_table_block, R.id.table_layout)
                        .textLayoutIsRoot(R.layout.view_table_entry_cell)))
                .build();
        LinearLayoutManagerBugFixed linearLayoutManager = new MarkwonLinearLayoutManager(this, new MarkwonLinearLayoutManager.HorizontalScrollViewScrolledListener() {
            @Override
            public void onScrolledLeft() {
                if (mSlidrInterface != null) {
                    mSlidrInterface.lock();
                }
            }

            @Override
            public void onScrolledRight() {
                if (mSlidrInterface != null) {
                    mSlidrInterface.unlock();
                }
            }
        });
        markdownRecyclerView.setLayoutManager(linearLayoutManager);
        markdownRecyclerView.setAdapter(markwonAdapter);

        if (savedInstanceState != null) {
            wikiMarkdown = savedInstanceState.getString(WIKI_MARKDOWN_STATE);
        }

        if (wikiMarkdown == null) {
            loadWiki();
        } else {
            markwonAdapter.setMarkdown(markwon, wikiMarkdown);
            markwonAdapter.notifyDataSetChanged();
        }
    }

    private void loadWiki() {
        if (isRefreshing) {
            return;
        }
        isRefreshing = true;

        swipeRefreshLayout.setRefreshing(true);

        Glide.with(this).clear(mFetchWikiInfoImageView);
        mFetchWikiInfoLinearLayout.setVisibility(View.GONE);

        retrofit.create(RedditAPI.class).getWikiPage(getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME), getIntent().getStringExtra(EXTRA_WIKI_PATH)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        String markdown = new JSONObject(response.body())
                                .getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.CONTENT_MD_KEY);
                        markwonAdapter.setMarkdown(markwon, Utils.modifyMarkdown(markdown));
                        markwonAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showErrorView(R.string.error_loading_wiki);
                    }
                } else {
                    if (response.code() == 404 || response.code() == 403) {
                        showErrorView(R.string.no_wiki);
                    } else {
                        showErrorView(R.string.error_loading_wiki);
                    }
                }
                isRefreshing = false;
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showErrorView(R.string.error_loading_wiki);
                isRefreshing = false;
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showErrorView(int stringResId) {
        swipeRefreshLayout.setRefreshing(false);
        mFetchWikiInfoLinearLayout.setVisibility(View.VISIBLE);
        mFetchWikiInfoTextView.setText(stringResId);
        mGlide.load(R.drawable.error_image).into(mFetchWikiInfoImageView);
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
        outState.putString(WIKI_MARKDOWN_STATE, wikiMarkdown);
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
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        swipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        mFetchWikiInfoTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (typeface != null) {
            mFetchWikiInfoTextView.setTypeface(typeface);
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }
}