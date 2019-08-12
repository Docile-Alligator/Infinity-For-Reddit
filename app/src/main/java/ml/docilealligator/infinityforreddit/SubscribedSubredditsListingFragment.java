package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import javax.inject.Inject;

import SubscribedSubredditDatabase.SubscribedSubredditViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscribedSubredditsListingFragment extends Fragment {

    static final String EXTRA_ACCOUNT_NAME = "EAT";
    static final String EXTRA_ACCOUNT_PROFILE_IMAGE_URL = "EAPIU";
    static final String EXTRA_IS_SUBREDDIT_SELECTION = "EISS";
    static final String EXTRA_EXTRA_CLEAR_SELECTION = "EECS";

    @BindView(R.id.recycler_view_subscribed_subreddits_listing_fragment) RecyclerView mRecyclerView;
    @BindView(R.id.no_subscriptions_linear_layout_subreddits_listing_fragment) LinearLayout mLinearLayout;
    @BindView(R.id.no_subscriptions_image_view_subreddits_listing_fragment) ImageView mImageView;

    private Activity mActivity;

    private RequestManager mGlide;

    private SubscribedSubredditViewModel mSubscribedSubredditViewModel;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    public SubscribedSubredditsListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subscribed_subreddits_listing, container, false);

        ButterKnife.bind(this, rootView);

        mActivity = getActivity();

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        Resources resources = getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet)) {
                int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResourceId > 0) {
                    mRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
                }
            }
        }

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        mGlide = Glide.with(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        SubscribedSubredditsRecyclerViewAdapter adapter;
        if(getArguments().getBoolean(EXTRA_IS_SUBREDDIT_SELECTION)) {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity, getArguments().getBoolean(EXTRA_EXTRA_CLEAR_SELECTION),
                    (name, iconUrl, subredditIsUser) -> ((SubredditSelectionActivity) mActivity).getSelectedSubreddit(name, iconUrl, subredditIsUser));
        } else {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity);
        }

        mRecyclerView.setAdapter(adapter);

        mSubscribedSubredditViewModel = ViewModelProviders.of(this,
                new SubscribedSubredditViewModel.Factory(mActivity.getApplication(), mRedditDataRoomDatabase, accountName))
                .get(SubscribedSubredditViewModel.class);
        mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, subscribedSubredditData -> {
            if (subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.load_post_error_indicator).into(mImageView);
            } else {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }

            adapter.addUser(accountName, getArguments().getString(EXTRA_ACCOUNT_PROFILE_IMAGE_URL));
            adapter.setSubscribedSubreddits(subscribedSubredditData);
        });

        return rootView;
    }
}
