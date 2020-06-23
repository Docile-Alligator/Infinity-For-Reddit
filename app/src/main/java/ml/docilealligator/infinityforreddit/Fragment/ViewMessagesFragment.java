package ml.docilealligator.infinityforreddit.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.RequestManager;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import ml.docilealligator.infinityforreddit.Adapter.MessageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.FetchMessages;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.MessageViewModel;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import retrofit2.Retrofit;

public class ViewMessagesFragment extends Fragment implements FragmentCommunicator {

    @BindView(R.id.swipe_refresh_layout_view_messages_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view_view_messages_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.fetch_messages_info_linear_layout_view_messages_fragment)
    LinearLayout mFetchMessageInfoLinearLayout;
    @BindView(R.id.fetch_messages_info_image_view_view_messages_fragment)
    ImageView mFetchMessageInfoImageView;
    @BindView(R.id.fetch_messages_info_text_view_view_messages_fragment)
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
    private MessageRecyclerViewAdapter mAdapter;
    private RequestManager mGlide;
    private LinearLayoutManager mLinearLayoutManager;

    public ViewMessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_messages, container, false);

        MessageViewModel.Factory factory = new MessageViewModel.Factory(mOauthRetrofit,
                getResources().getConfiguration().locale, mAccessToken, FetchMessages.WHERE_INBOX);
        mMessageViewModel = new ViewModelProvider(this, factory).get(MessageViewModel.class);
        mMessageViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> mAdapter.submitList(messages));

        mMessageViewModel.hasMessage().observe(getViewLifecycleOwner(), hasMessage -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (hasMessage) {
                mFetchMessageInfoLinearLayout.setVisibility(View.GONE);
            } else {
                mFetchMessageInfoLinearLayout.setOnClickListener(view -> {
                    //Do nothing
                });
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

    }

    private void onRefresh() {
        mMessageViewModel.refresh();
    }
}