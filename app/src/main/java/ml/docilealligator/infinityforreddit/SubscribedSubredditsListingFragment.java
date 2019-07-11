package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import SubscribedSubredditDatabase.SubscribedSubredditViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscribedSubredditsListingFragment extends Fragment {

    static final String EXTRA_IS_SUBREDDIT_SELECTION = "EISSK";

    @BindView(R.id.recycler_view_subscribed_subreddits_listing_fragment) RecyclerView mRecyclerView;
    @BindView(R.id.no_subscriptions_linear_layout_subreddits_listing_fragment) LinearLayout mLinearLayout;
    @BindView(R.id.no_subscriptions_image_view_subreddits_listing_fragment) ImageView mImageView;

    private Activity mActivity;

    private RequestManager mGlide;

    private SubscribedSubredditViewModel mSubscribedSubredditViewModel;

    private boolean mInsertSuccess = false;

    public SubscribedSubredditsListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subscribed_subreddits_listing, container, false);

        ButterKnife.bind(this, rootView);

        mActivity = getActivity();

        mGlide = Glide.with(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        SubscribedSubredditsRecyclerViewAdapter adapter;
        if(getArguments().getBoolean(EXTRA_IS_SUBREDDIT_SELECTION)) {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity, (name, iconUrl) -> ((SubredditSelectionActivity) mActivity).getSelectedSubreddit(name, iconUrl));
        } else {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity);
        }


        mRecyclerView.setAdapter(adapter);

        mSubscribedSubredditViewModel = ViewModelProviders.of(this).get(SubscribedSubredditViewModel.class);
        mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, subscribedSubredditData -> {
            if (subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.load_post_error_indicator).into(mImageView);
            } else {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            adapter.setSubscribedSubreddits(subscribedSubredditData);
        });

        return rootView;
    }
}
