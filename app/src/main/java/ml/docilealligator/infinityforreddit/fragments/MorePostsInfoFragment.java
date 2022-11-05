package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.FragmentMorePostsInfoBinding;

public class MorePostsInfoFragment extends Fragment {

    public static final String EXTRA_STATUS = "ES";

    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private FragmentMorePostsInfoBinding binding;
    private BaseActivity mActivity;
    @Status
    int status;

    public MorePostsInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        binding = FragmentMorePostsInfoBinding.inflate(inflater, container, false);

        applyTheme();

        setStatus(getArguments().getInt(EXTRA_STATUS, Status.LOADING));

        return binding.getRoot();
    }

    public void setStatus(@Status int status) {
        this.status = status;
        switch (status) {
            case Status.LOADING:
                binding.infoTextViewMorePostsInfoFragment.setText(R.string.loading);
                break;
            case Status.FAILED:
                binding.infoTextViewMorePostsInfoFragment.setText(R.string.load_more_posts_failed);
                break;
            case Status.NO_MORE_POSTS:
                binding.infoTextViewMorePostsInfoFragment.setText(R.string.no_more_posts);
        }
    }

    private void applyTheme() {
        binding.infoTextViewMorePostsInfoFragment.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @IntDef({Status.LOADING, Status.FAILED, Status.NO_MORE_POSTS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
        int LOADING = 0;
        int FAILED = 1;
        int NO_MORE_POSTS = 2;
    }
}