package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityCommentFullMarkdownBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.CustomMarkwonAdapter;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class FullMarkdownActivity extends BaseActivity {

    public static final String EXTRA_MARKDOWN = "EM";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final String EXTRA_SUBMIT_POST = "ESP";

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private ActivityCommentFullMarkdownBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        binding = ActivityCommentFullMarkdownBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        setSupportActionBar(binding.toolbarCommentFullMarkdownActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(" ");

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutCommentFullMarkdownActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }

                ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                        Insets allInsets = Utils.getInsets(insets, false);

                        setMargins(binding.toolbarCommentFullMarkdownActivity,
                                allInsets.left,
                                allInsets.top,
                                allInsets.right,
                                BaseActivity.IGNORE_MARGIN);

                        binding.contentRecyclerViewCommentFullMarkdownActivity.setPadding(
                                (int) Utils.convertDpToPixel(16, FullMarkdownActivity.this) + allInsets.left,
                                0,
                                (int) Utils.convertDpToPixel(16, FullMarkdownActivity.this) + allInsets.right,
                                allInsets.bottom);

                        return WindowInsetsCompat.CONSUMED;
                    }
                });
                /*adjustToolbar(binding.toolbarCommentFullMarkdownActivity);
                binding.contentRecyclerViewCommentFullMarkdownActivity.setPadding(binding.contentRecyclerViewCommentFullMarkdownActivity.getPaddingLeft(), 0, binding.contentRecyclerViewCommentFullMarkdownActivity.getPaddingRight(), getNavBarHeight());*/
            }
        }

        String markdown = getIntent().getStringExtra(EXTRA_MARKDOWN);
        boolean isNsfw = getIntent().getBooleanExtra(EXTRA_IS_NSFW, false);
        int markdownColor = mCustomThemeWrapper.getCommentColor();
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
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
                builder.linkColor(mCustomThemeWrapper.getLinkColor());
            }
        };
        Markwon markwon = MarkdownUtils.createContentPreviewRedditMarkwon(this, miscPlugin, markdownColor,
                markdownColor | 0xFF000000);

        CustomMarkwonAdapter markwonAdapter = MarkdownUtils.createCustomTablesAdapter(this);
        LinearLayoutManagerBugFixed linearLayoutManager = new SwipeLockLinearLayoutManager(this, new SwipeLockInterface() {
            @Override
            public void lockSwipe() {
                if (mSliderPanel != null) {
                    mSliderPanel.lock();
                }
            }

            @Override
            public void unlockSwipe() {
                if (mSliderPanel != null) {
                    mSliderPanel.unlock();
                }
            }
        });
        binding.contentRecyclerViewCommentFullMarkdownActivity.setLayoutManager(linearLayoutManager);
        binding.contentRecyclerViewCommentFullMarkdownActivity.setAdapter(markwonAdapter);
        markwonAdapter.setMarkdown(markwon, markdown);
        // noinspection NotifyDataSetChanged
        markwonAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getBooleanExtra(EXTRA_SUBMIT_POST, false)) {
            getMenuInflater().inflate(R.menu.full_markdown_activity, menu);
            applyMenuItemTheme(menu);
        }
        return true;
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
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutCommentFullMarkdownActivity, binding.collapsingToolbarLayoutCommentFullMarkdownActivity, binding.toolbarCommentFullMarkdownActivity);
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