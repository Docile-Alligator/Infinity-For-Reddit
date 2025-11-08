package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.recycler.MarkwonAdapter;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityWikiBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.EmoteCloseBracketInlineProcessor;
import ml.docilealligator.infinityforreddit.markdown.EmotePlugin;
import ml.docilealligator.infinityforreddit.markdown.EvenBetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifEntry;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifPlugin;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
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
    CustomThemeWrapper mCustomThemeWrapper;
    private String wikiMarkdown;
    private String mSubredditName;
    private EmoteCloseBracketInlineProcessor emoteCloseBracketInlineProcessor;
    private EmotePlugin emotePlugin;
    private ImageAndGifPlugin imageAndGifPlugin;
    private ImageAndGifEntry imageAndGifEntry;
    private Markwon markwon;
    private MarkwonAdapter markwonAdapter;
    private boolean isRefreshing = false;
    private RequestManager mGlide;
    private ActivityWikiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivityWikiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        setSupportActionBar(binding.toolbarCommentWikiActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutCommentWikiActivity);
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

                        setMargins(binding.toolbarCommentWikiActivity,
                                allInsets.left,
                                allInsets.top,
                                allInsets.right,
                                BaseActivity.IGNORE_MARGIN);

                        int padding16 = (int) Utils.convertDpToPixel(16, WikiActivity.this);

                        binding.contentMarkdownViewCommentWikiActivity.setPadding(
                                padding16 + allInsets.left,
                                0,
                                padding16 + allInsets.right,
                                allInsets.bottom);

                        return WindowInsetsCompat.CONSUMED;
                    }
                });
                /*adjustToolbar(binding.toolbarCommentWikiActivity);
                binding.contentMarkdownViewCommentWikiActivity.setPadding(binding.contentMarkdownViewCommentWikiActivity.getPaddingLeft(), 0, binding.contentMarkdownViewCommentWikiActivity.getPaddingRight(), getNavBarHeight());*/
            }
        }

        mGlide = Glide.with(this);

        mSubredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);

        binding.swipeRefreshLayoutWikiActivity.setEnabled(mSharedPreferences.getBoolean(SharedPreferencesUtils.PULL_TO_REFRESH, true));
        binding.swipeRefreshLayoutWikiActivity.setOnRefreshListener(this::loadWiki);

        int markdownColor = mCustomThemeWrapper.getPrimaryTextColor();
        int spoilerBackgroundColor = markdownColor | 0xFF000000;
        int linkColor = mCustomThemeWrapper.getLinkColor();
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (contentTypeface != null) {
                    textView.setTypeface(contentTypeface);
                }
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
        };
        EvenBetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
            urlMenuBottomSheetFragment.show(getSupportFragmentManager(), null);
            return true;
        };
        emoteCloseBracketInlineProcessor = new EmoteCloseBracketInlineProcessor();
        emotePlugin = EmotePlugin.create(this, SharedPreferencesUtils.EMBEDDED_MEDIA_ALL,
                mediaMetadata -> {
                    Intent intent = new Intent(this, ViewImageOrGifActivity.class);
                    if (mediaMetadata.isGIF) {
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                    } else {
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                    }
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mSubredditName);
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
                });
        imageAndGifPlugin = new ImageAndGifPlugin();
        imageAndGifEntry = new ImageAndGifEntry(this,
                mGlide, SharedPreferencesUtils.EMBEDDED_MEDIA_ALL, mediaMetadata -> {
            Intent intent = new Intent(this, ViewImageOrGifActivity.class);
            if (mediaMetadata.isGIF) {
                intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
            } else {
                intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
            }
            intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mSubredditName);
            intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
        });
        markwon = MarkdownUtils.createFullRedditMarkwon(this,
                miscPlugin, emoteCloseBracketInlineProcessor, emotePlugin, imageAndGifPlugin, markdownColor, spoilerBackgroundColor, onLinkLongClickListener);

        markwonAdapter = MarkdownUtils.createCustomTablesAndImagesAdapter(this, imageAndGifEntry);
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
        binding.contentMarkdownViewCommentWikiActivity.setLayoutManager(linearLayoutManager);
        binding.contentMarkdownViewCommentWikiActivity.setAdapter(markwonAdapter);

        if (savedInstanceState != null) {
            wikiMarkdown = savedInstanceState.getString(WIKI_MARKDOWN_STATE);
        }

        if (wikiMarkdown == null) {
            loadWiki();
        } else {
            markwonAdapter.setMarkdown(markwon, wikiMarkdown);
            // noinspection NotifyDataSetChanged
            markwonAdapter.notifyDataSetChanged();
        }
    }

    private void loadWiki() {
        if (isRefreshing) {
            return;
        }
        isRefreshing = true;

        binding.swipeRefreshLayoutWikiActivity.setRefreshing(true);

        Glide.with(this).clear(binding.fetchWikiImageViewWikiActivity);
        binding.fetchWikiLinearLayoutWikiActivity.setVisibility(View.GONE);

        mRetrofit.create(RedditAPI.class).getWikiPage(mSubredditName, getIntent().getStringExtra(EXTRA_WIKI_PATH)).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        String markdown = new JSONObject(response.body())
                                .getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.CONTENT_MD_KEY);
                        markwonAdapter.setMarkdown(markwon, Utils.modifyMarkdown(markdown));
                        // noinspection NotifyDataSetChanged
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
                binding.swipeRefreshLayoutWikiActivity.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showErrorView(R.string.error_loading_wiki);
                isRefreshing = false;
                binding.swipeRefreshLayoutWikiActivity.setRefreshing(false);
            }
        });
    }

    private void showErrorView(int stringResId) {
        binding.swipeRefreshLayoutWikiActivity.setRefreshing(false);
        binding.fetchWikiLinearLayoutWikiActivity.setVisibility(View.VISIBLE);
        binding.fetchWikiTextViewWikiActivity.setText(stringResId);
        mGlide.load(R.drawable.error_image).into(binding.fetchWikiImageViewWikiActivity);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutCommentWikiActivity,
                binding.collapsingToolbarLayoutWikiActivity, binding.toolbarCommentWikiActivity);
        binding.swipeRefreshLayoutWikiActivity.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayoutWikiActivity.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        binding.fetchWikiTextViewWikiActivity.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (typeface != null) {
            binding.fetchWikiTextViewWikiActivity.setTypeface(typeface);
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Subscribe
    public void onChangeNetworkStatusEvent(ChangeNetworkStatusEvent changeNetworkStatusEvent) {
        String dataSavingMode = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        if (dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            if (emotePlugin != null) {
                emotePlugin.setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
            }

            if (imageAndGifEntry != null) {
                imageAndGifEntry.setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
            }
        }
    }
}