package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.MessageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.events.RepliedToPrivateMessageEvent;
import ml.docilealligator.infinityforreddit.message.FetchMessage;
import ml.docilealligator.infinityforreddit.message.MessageViewModel;
import retrofit2.Retrofit;

public class InboxFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_MESSAGE_WHERE = "EMT";
    @BindView(R.id.swipe_refresh_layout_inbox_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view_inbox_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.fetch_messages_info_linear_layout_inbox_fragment)
    LinearLayout mFetchMessageInfoLinearLayout;
    @BindView(R.id.fetch_messages_info_image_view_inbox_fragment)
    ImageView mFetchMessageInfoImageView;
    @BindView(R.id.fetch_messages_info_text_view_inbox_fragment)
    TextView mFetchMessageInfoTextView;
    MessageViewModel mMessageViewModel;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String mAccessToken;
    private String mWhere;
    private MessageRecyclerViewAdapter mAdapter;
    private RequestManager mGlide;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private BaseActivity mActivity;

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        EventBus.getDefault().register(this);

        applyTheme();

        Bundle arguments = getArguments();
        if (arguments == null) {
            return rootView;
        }
        mAccessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        mGlide = Glide.with(this);

        if (mActivity.isImmersiveInterface()) {
            mRecyclerView.setPadding(0, 0, 0, mActivity.getNavBarHeight());
        }

        mWhere = arguments.getString(EXTRA_MESSAGE_WHERE, FetchMessage.WHERE_INBOX);
        mAdapter = new MessageRecyclerViewAdapter(mActivity, mOauthRetrofit, mCustomThemeWrapper,
                mAccessToken, mWhere, () -> mMessageViewModel.retryLoadingMore());
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mActivity, mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        if (mActivity instanceof RecyclerViewContentScrollingInterface) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        ((RecyclerViewContentScrollingInterface) mActivity).contentScrollDown();
                    } else if (dy < 0) {
                        ((RecyclerViewContentScrollingInterface) mActivity).contentScrollUp();
                    }
                }
            });
        }

        MessageViewModel.Factory factory = new MessageViewModel.Factory(mOauthRetrofit,
                getResources().getConfiguration().locale, mAccessToken, mWhere);
        mMessageViewModel = new ViewModelProvider(this, factory).get(MessageViewModel.class);
        mMessageViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> mAdapter.submitList(messages));

        mMessageViewModel.hasMessage().observe(getViewLifecycleOwner(), hasMessage -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (hasMessage) {
                mFetchMessageInfoLinearLayout.setVisibility(View.GONE);
            } else {
                mFetchMessageInfoLinearLayout.setOnClickListener(null);
                showErrorView(R.string.no_messages);
            }
        });

        mMessageViewModel.getInitialLoadingState().observe(getViewLifecycleOwner(), networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mSwipeRefreshLayout.setRefreshing(false);
                mFetchMessageInfoLinearLayout.setOnClickListener(view -> {
                    mFetchMessageInfoLinearLayout.setVisibility(View.GONE);
                    mMessageViewModel.refresh();
                    mAdapter.setNetworkState(null);
                });
                showErrorView(R.string.load_messages_failed);
            } else {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mMessageViewModel.getPaginationNetworkState().observe(getViewLifecycleOwner(), networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        mSwipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        return rootView;
    }

    private void showErrorView(int stringResId) {
        mSwipeRefreshLayout.setRefreshing(false);
        mFetchMessageInfoLinearLayout.setVisibility(View.VISIBLE);
        mFetchMessageInfoTextView.setText(stringResId);
        mGlide.load(R.drawable.error_image).into(mFetchMessageInfoImageView);
    }

    @Override
    public void applyTheme() {
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        mSwipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        mFetchMessageInfoTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            mFetchMessageInfoTextView.setTypeface(mActivity.typeface);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void markAllMessagesRead() {
        if (mAdapter != null) {
            mAdapter.setMarkAllMessagesAsRead(true);

            int previousPosition = -1;
            if (mLinearLayoutManager != null) {
                previousPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
            }

            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            mRecyclerView.setAdapter(null);
            mRecyclerView.setLayoutManager(null);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(layoutManager);

            if (previousPosition > 0) {
                mRecyclerView.scrollToPosition(previousPosition);
            }
        }
    }

    private void onRefresh() {
        mMessageViewModel.refresh();
        mAdapter.setNetworkState(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @Subscribe
    public void onRepliedToPrivateMessageEvent(RepliedToPrivateMessageEvent repliedToPrivateMessageEvent) {
        if (mAdapter != null && mWhere.equals(FetchMessage.WHERE_MESSAGES)) {
            mAdapter.updateMessageReply(repliedToPrivateMessageEvent.newReply, repliedToPrivateMessageEvent.messagePosition);
        }
    }
}