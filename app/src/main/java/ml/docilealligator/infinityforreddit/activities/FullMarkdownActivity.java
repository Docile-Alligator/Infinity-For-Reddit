package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.MarkwonLinearLayoutManager;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.SpoilerParserPlugin;
import ml.docilealligator.infinityforreddit.markdown.SuperscriptInlineProcessor;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class FullMarkdownActivity extends BaseActivity {

    public static final String EXTRA_COMMENT_MARKDOWN = "ECM";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final String EXTRA_SUBMIT_POST = "ESP";

    @BindView(R.id.coordinator_layout_comment_full_markdown_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_comment_full_markdown_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_comment_full_markdown_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_comment_full_markdown_activity)
    Toolbar toolbar;
    @BindView(R.id.content_markdown_view_comment_full_markdown_activity)
    RecyclerView markdownRecyclerView;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private SlidrInterface mSlidrInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_full_markdown);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(" ");

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

        String commentMarkdown = getIntent().getStringExtra(EXTRA_COMMENT_MARKDOWN);
        boolean isNsfw = getIntent().getBooleanExtra(EXTRA_IS_NSFW, false);
        int markdownColor = mCustomThemeWrapper.getCommentColor();
        int spoilerBackgroundColor = markdownColor | 0xFF000000;
        int linkColor = mCustomThemeWrapper.getLinkColor();
        Markwon markwon = Markwon.builder(this)
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
                        if (typeface != null) {
                            textView.setTypeface(typeface);
                        }
                        textView.setTextColor(markdownColor);
                    }

                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(FullMarkdownActivity.this, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            intent.setData(uri);
                            intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, isNsfw);
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
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(TableEntryPlugin.create(this))
                .build();

        MarkwonAdapter markwonAdapter = MarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
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
        markwonAdapter.setMarkdown(markwon, commentMarkdown);
        markwonAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getBooleanExtra(EXTRA_SUBMIT_POST, false)) {
            getMenuInflater().inflate(R.menu.full_markdown_activity, menu);
            applyMenuItemTheme(menu);
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_send_full_markdown_activity) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
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
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
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
}