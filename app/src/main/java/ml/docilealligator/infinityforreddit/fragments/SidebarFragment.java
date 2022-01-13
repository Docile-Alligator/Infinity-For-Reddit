package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.commonmark.ext.gfm.tables.TableBlock;

import java.util.concurrent.Executor;

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
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubredditData;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CopyTextBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.markdown.SuperscriptInlineProcessor;
import ml.docilealligator.infinityforreddit.subreddit.FetchSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditViewModel;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class SidebarFragment extends Fragment {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public SubredditViewModel mSubredditViewModel;
    @BindView(R.id.swipe_refresh_layout_sidebar_fragment)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.markdown_recycler_view_sidebar_fragment)
    RecyclerView recyclerView;
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
    private BaseActivity activity;
    private String mAccessToken;
    private String subredditName;
    private LinearLayoutManagerBugFixed linearLayoutManager;
    private int markdownColor;
    private String sidebarDescription;

    public SidebarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sidebar, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        mAccessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        subredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME);
        if (subredditName == null) {
            Toast.makeText(activity, R.string.error_getting_subreddit_name, Toast.LENGTH_SHORT).show();
            return rootView;
        }

        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        swipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        markdownColor = mCustomThemeWrapper.getPrimaryTextColor();

        Markwon markwon = Markwon.builder(activity)
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
                })
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(MovementMethodPlugin.create(BetterLinkMovementMethod.linkify(Linkify.WEB_URLS).setOnLinkLongClickListener((textView, url) -> {
                    UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = new UrlMenuBottomSheetFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(UrlMenuBottomSheetFragment.EXTRA_URL, url);
                    urlMenuBottomSheetFragment.setArguments(bundle);
                    urlMenuBottomSheetFragment.show(getChildFragmentManager(), urlMenuBottomSheetFragment.getTag());
                    return true;
                })))
                .usePlugin(TableEntryPlugin.create(activity))
                .build();
        MarkwonAdapter markwonAdapter = MarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
                .include(TableBlock.class, TableEntry.create(builder -> builder
                        .tableLayout(R.layout.adapter_table_block, R.id.table_layout)
                        .textLayoutIsRoot(R.layout.view_table_entry_cell)))
                .build();

        linearLayoutManager = new LinearLayoutManagerBugFixed(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(markwonAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    ((ViewSubredditDetailActivity) activity).contentScrollDown();
                } else if (dy < 0) {
                    ((ViewSubredditDetailActivity) activity).contentScrollUp();
                }

            }
        });

        mSubredditViewModel = new ViewModelProvider(this,
                new SubredditViewModel.Factory(activity.getApplication(), mRedditDataRoomDatabase, subredditName))
                .get(SubredditViewModel.class);
        mSubredditViewModel.getSubredditLiveData().observe(getViewLifecycleOwner(), subredditData -> {
            if (subredditData != null) {
                sidebarDescription = subredditData.getSidebarDescription();
                if (sidebarDescription != null && !sidebarDescription.equals("")) {
                    markwonAdapter.setMarkdown(markwon, sidebarDescription);
                    markwonAdapter.notifyDataSetChanged();
                }
            } else {
                fetchSubredditData();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this::fetchSubredditData);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }

    public void fetchSubredditData() {
        swipeRefreshLayout.setRefreshing(true);
        FetchSubredditData.fetchSubredditData(mOauthRetrofit, mRetrofit, subredditName, mAccessToken, new FetchSubredditData.FetchSubredditDataListener() {
            @Override
            public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                swipeRefreshLayout.setRefreshing(false);
                InsertSubredditData.insertSubredditData(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        subredditData, () -> swipeRefreshLayout.setRefreshing(false));
            }

            @Override
            public void onFetchSubredditDataFail(boolean isQuarantined) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(activity, R.string.cannot_fetch_sidebar, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goBackToTop() {
        if (linearLayoutManager != null) {
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }
}
