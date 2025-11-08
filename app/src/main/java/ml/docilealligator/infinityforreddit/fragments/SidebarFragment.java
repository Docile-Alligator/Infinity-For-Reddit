package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubredditData;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CopyTextBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentSidebarBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.markdown.CustomMarkwonAdapter;
import ml.docilealligator.infinityforreddit.markdown.EmoteCloseBracketInlineProcessor;
import ml.docilealligator.infinityforreddit.markdown.EmotePlugin;
import ml.docilealligator.infinityforreddit.markdown.EvenBetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifEntry;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifPlugin;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.subreddit.FetchSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class SidebarFragment extends Fragment {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public SubredditViewModel mSubredditViewModel;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private ViewSubredditDetailActivity activity;
    private String subredditName;
    private LinearLayoutManagerBugFixed linearLayoutManager;
    private int markdownColor;
    private String sidebarDescription;
    private EmotePlugin emotePlugin;
    private ImageAndGifEntry imageAndGifEntry;
    private FragmentSidebarBinding binding;

    public SidebarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSidebarBinding.inflate(inflater, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        EventBus.getDefault().register(this);

        if (activity.isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);
                    binding.markdownRecyclerViewSidebarFragment.setPadding(
                            0, 0, 0, allInsets.bottom
                    );
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        subredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME);
        if (subredditName == null) {
            Toast.makeText(activity, R.string.error_getting_subreddit_name, Toast.LENGTH_SHORT).show();
            return binding.getRoot();
        }

        binding.swipeRefreshLayoutSidebarFragment.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayoutSidebarFragment.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        markdownColor = mCustomThemeWrapper.getPrimaryTextColor();
        int spoilerBackgroundColor = markdownColor | 0xFF000000;

        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (activity.contentTypeface != null) {
                    textView.setTypeface(activity.contentTypeface);
                }
                textView.setTextColor(markdownColor);
                textView.setOnLongClickListener(view -> {
                    if (sidebarDescription != null && !sidebarDescription.equals("") && textView.getSelectionStart() == -1 && textView.getSelectionEnd() == -1) {
                        Bundle bundle = new Bundle();
                        bundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, sidebarDescription);
                        CopyTextBottomSheetFragment copyTextBottomSheetFragment = new CopyTextBottomSheetFragment();
                        copyTextBottomSheetFragment.setArguments(bundle);
                        copyTextBottomSheetFragment.show(getChildFragmentManager(), copyTextBottomSheetFragment.getTag());
                    }
                    return true;
                });
            }

            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                builder.linkColor(mCustomThemeWrapper.getLinkColor());
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(activity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    startActivity(intent);
                });
            }
        };
        EvenBetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
            urlMenuBottomSheetFragment.show(getChildFragmentManager(), null);
            return true;
        };
        EmoteCloseBracketInlineProcessor emoteCloseBracketInlineProcessor = new EmoteCloseBracketInlineProcessor();
        emotePlugin = EmotePlugin.create(activity, SharedPreferencesUtils.EMBEDDED_MEDIA_ALL,
                mediaMetadata -> {
                    Intent imageIntent = new Intent(activity, ViewImageOrGifActivity.class);
                    if (mediaMetadata.isGIF) {
                        imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                    } else {
                        imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                    }
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, subredditName);
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
                });
        ImageAndGifPlugin imageAndGifPlugin = new ImageAndGifPlugin();
        imageAndGifEntry = new ImageAndGifEntry(activity,
                Glide.with(this), SharedPreferencesUtils.EMBEDDED_MEDIA_ALL,
                mediaMetadata -> {
                    Intent imageIntent = new Intent(activity, ViewImageOrGifActivity.class);
                    if (mediaMetadata.isGIF) {
                        imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                    } else {
                        imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                    }
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, subredditName);
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
                });
        Markwon markwon = MarkdownUtils.createFullRedditMarkwon(activity,
                miscPlugin, emoteCloseBracketInlineProcessor, emotePlugin, imageAndGifPlugin, markdownColor,
                spoilerBackgroundColor, onLinkLongClickListener);
        CustomMarkwonAdapter markwonAdapter = MarkdownUtils.createCustomTablesAndImagesAdapter(activity, imageAndGifEntry);
        markwonAdapter.setOnLongClickListener(view -> {
            if (sidebarDescription != null && !sidebarDescription.equals("")) {
                Bundle bundle = new Bundle();
                bundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, sidebarDescription);
                CopyTextBottomSheetFragment copyTextBottomSheetFragment = new CopyTextBottomSheetFragment();
                copyTextBottomSheetFragment.setArguments(bundle);
                copyTextBottomSheetFragment.show(getChildFragmentManager(), copyTextBottomSheetFragment.getTag());
            }
            return true;
        });
        linearLayoutManager = new LinearLayoutManagerBugFixed(activity);
        binding.markdownRecyclerViewSidebarFragment.setLayoutManager(linearLayoutManager);
        binding.markdownRecyclerViewSidebarFragment.setAdapter(markwonAdapter);
        binding.markdownRecyclerViewSidebarFragment.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    activity.contentScrollDown();
                } else if (dy < 0) {
                    activity.contentScrollUp();
                }

            }
        });

        mSubredditViewModel = new ViewModelProvider(activity,
                new SubredditViewModel.Factory(mRedditDataRoomDatabase, subredditName))
                .get(SubredditViewModel.class);
        mSubredditViewModel.getSubredditLiveData().observe(getViewLifecycleOwner(), subredditData -> {
            if (subredditData != null) {
                sidebarDescription = subredditData.getSidebarDescription();
                if (sidebarDescription != null && !sidebarDescription.equals("")) {
                    markwonAdapter.setMarkdown(markwon, sidebarDescription);
                    // noinspection NotifyDataSetChanged
                    markwonAdapter.notifyDataSetChanged();
                }
            } else {
                fetchSubredditData();
            }
        });

        binding.swipeRefreshLayoutSidebarFragment.setOnRefreshListener(this::fetchSubredditData);

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ViewSubredditDetailActivity) context;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void fetchSubredditData() {
        binding.swipeRefreshLayoutSidebarFragment.setRefreshing(true);
        Handler handler = new Handler();
        FetchSubredditData.fetchSubredditData(mExecutor, handler,
                activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? null : mOauthRetrofit, mRetrofit,
                subredditName, activity.accessToken, new FetchSubredditData.FetchSubredditDataListener() {
                    @Override
                    public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                        binding.swipeRefreshLayoutSidebarFragment.setRefreshing(false);
                        InsertSubredditData.insertSubredditData(mExecutor, handler, mRedditDataRoomDatabase,
                                subredditData, () -> binding.swipeRefreshLayoutSidebarFragment.setRefreshing(false));
                    }

                    @Override
                    public void onFetchSubredditDataFail(boolean isQuarantined) {
                        binding.swipeRefreshLayoutSidebarFragment.setRefreshing(false);
                        Toast.makeText(activity, R.string.cannot_fetch_sidebar, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void goBackToTop() {
        if (linearLayoutManager != null) {
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void setDataSavingMode(boolean dataSavingMode) {
        emotePlugin.setDataSavingMode(dataSavingMode);
        imageAndGifEntry.setDataSavingMode(dataSavingMode);
    }

    @Subscribe
    public void onChangeNetworkStatusEvent(ChangeNetworkStatusEvent changeNetworkStatusEvent) {
        String dataSavingMode = activity.getDefaultSharedPreferences().getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
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
