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

import SubscribedUserDatabase.SubscribedUserViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class FollowedUsersListingFragment extends Fragment {

    @BindView(R.id.recycler_view_followed_users_listing_fragment) RecyclerView mRecyclerView;
    @BindView(R.id.no_subscriptions_linear_layout_followed_users_listing_fragment) LinearLayout mLinearLayout;
    @BindView(R.id.no_subscriptions_image_view_followed_users_listing_fragment) ImageView mImageView;

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

        mGlide = Glide.with(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        FollowedUsersRecyclerViewAdapter adapter = new FollowedUsersRecyclerViewAdapter(mActivity);
        mRecyclerView.setAdapter(adapter);

        mSubscribedUserViewModel = ViewModelProviders.of(this).get(SubscribedUserViewModel.class);
        mSubscribedUserViewModel.getAllSubscribedUsers().observe(this, subscribedUserData -> {
            if (subscribedUserData == null || subscribedUserData.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.load_post_error_indicator).into(mImageView);
            } else {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            adapter.setSubscribedUsers(subscribedUserData);
        });

        return rootView;
    }

}
