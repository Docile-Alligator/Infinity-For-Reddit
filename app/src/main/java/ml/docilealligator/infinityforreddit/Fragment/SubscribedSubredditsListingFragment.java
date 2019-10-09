package ml.docilealligator.infinityforreddit.Fragment;


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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Activity.SubredditSelectionActivity;
import ml.docilealligator.infinityforreddit.Adapter.SubscribedSubredditsRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditViewModel;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscribedSubredditsListingFragment extends Fragment {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_PROFILE_IMAGE_URL = "EAPIU";
    public static final String EXTRA_IS_SUBREDDIT_SELECTION = "EISS";
    public static final String EXTRA_EXTRA_CLEAR_SELECTION = "EECS";

    @BindView(R.id.recycler_view_subscribed_subreddits_listing_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_subscriptions_linear_layout_subreddits_listing_fragment)
    LinearLayout mLinearLayout;
    @BindView(R.id.no_subscriptions_image_view_subreddits_listing_fragment)
    ImageView mImageView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    private Activity mActivity;
    private RequestManager mGlide;
    private SubscribedSubredditViewModel mSubscribedSubredditViewModel;

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
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);

        mGlide = Glide.with(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        SubscribedSubredditsRecyclerViewAdapter adapter;
        if (getArguments().getBoolean(EXTRA_IS_SUBREDDIT_SELECTION)) {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity, getArguments().getBoolean(EXTRA_EXTRA_CLEAR_SELECTION),
                    (name, iconUrl, subredditIsUser) -> ((SubredditSelectionActivity) mActivity).getSelectedSubreddit(name, iconUrl, subredditIsUser));
        } else {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity, mOauthRetrofit, mRedditDataRoomDatabase, accessToken);
        }

        mRecyclerView.setAdapter(adapter);

        mSubscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(mActivity.getApplication(), mRedditDataRoomDatabase, accountName))
                .get(SubscribedSubredditViewModel.class);
        mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, subscribedSubredditData -> {
            if (subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(mImageView);
            } else {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }

            adapter.addUser(accountName, getArguments().getString(EXTRA_ACCOUNT_PROFILE_IMAGE_URL));
            adapter.setSubscribedSubreddits(subscribedSubredditData);
        });

        mSubscribedSubredditViewModel.getAllFavoriteSubscribedSubreddits().observe(this, favoriteSubscribedSubredditData -> {
            if (favoriteSubscribedSubredditData != null && favoriteSubscribedSubredditData.size() > 0) {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }

            adapter.setFavoriteSubscribedSubreddits(favoriteSubscribedSubredditData);
        });

        return rootView;
    }
}
