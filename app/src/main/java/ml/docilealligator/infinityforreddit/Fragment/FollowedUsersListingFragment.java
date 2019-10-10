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
import ml.docilealligator.infinityforreddit.Adapter.FollowedUsersRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserViewModel;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class FollowedUsersListingFragment extends Fragment {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";

    @BindView(R.id.recycler_view_followed_users_listing_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_subscriptions_linear_layout_followed_users_listing_fragment)
    LinearLayout mLinearLayout;
    @BindView(R.id.no_subscriptions_image_view_followed_users_listing_fragment)
    ImageView mImageView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    private Activity mActivity;
    private RequestManager mGlide;
    private SubscribedUserViewModel mSubscribedUserViewModel;

    public FollowedUsersListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_followed_users_listing, container, false);

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

        mGlide = Glide.with(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        FollowedUsersRecyclerViewAdapter adapter = new FollowedUsersRecyclerViewAdapter(mActivity,
                mOauthRetrofit, mRedditDataRoomDatabase, getArguments().getString(EXTRA_ACCESS_TOKEN));
        mRecyclerView.setAdapter(adapter);

        mSubscribedUserViewModel = new ViewModelProvider(this,
                new SubscribedUserViewModel.Factory(mActivity.getApplication(), mRedditDataRoomDatabase, getArguments().getString(EXTRA_ACCOUNT_NAME)))
                .get(SubscribedUserViewModel.class);

        mSubscribedUserViewModel.getAllSubscribedUsers().observe(this, subscribedUserData -> {
            if (subscribedUserData == null || subscribedUserData.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(mImageView);
            } else {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }
            adapter.setSubscribedUsers(subscribedUserData);
        });

        mSubscribedUserViewModel.getAllFavoriteSubscribedUsers().observe(this, favoriteSubscribedUserData -> {
            if (favoriteSubscribedUserData != null && favoriteSubscribedUserData.size() > 0) {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }
            adapter.setFavoriteSubscribedUsers(favoriteSubscribedUserData);
        });

        return rootView;
    }

}
