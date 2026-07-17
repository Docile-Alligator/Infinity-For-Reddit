package ml.docilealligator.infinityforreddit.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentTiktokPostBinding;
import ml.docilealligator.infinityforreddit.post.Post;

public class TikTokPostFragment extends Fragment {

    public static final String EXTRA_POST = "ETP";

    private FragmentTiktokPostBinding binding;
    private Post post;
    private RequestManager mGlide;

    public static TikTokPostFragment newInstance(Post post) {
        TikTokPostFragment fragment = new TikTokPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_POST, post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post = getArguments().getParcelable(EXTRA_POST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((Infinity) requireActivity().getApplication()).getAppComponent().inject(this);

        binding = FragmentTiktokPostBinding.inflate(inflater, container, false);
        mGlide = Glide.with(this);

        if (post != null) {
            binding.subredditTextViewFragmentTiktokPost.setText("r/" + post.getSubredditName());
            binding.titleTextViewFragmentTiktokPost.setText(post.getTitle());
            binding.authorTextViewFragmentTiktokPost.setText("u/" + post.getAuthor());
            binding.commentsCountTextViewFragmentTiktokPost.setText(String.valueOf(post.getNumComments()));

            String imageUrl = null;
            if (post.getThumbnail() != null && !post.getThumbnail().isEmpty() && post.getThumbnail().startsWith("http")) {
                imageUrl = post.getThumbnail();
            } else if (post.getUrl() != null && !post.getUrl().isEmpty() && post.getUrl().startsWith("http")) {
                imageUrl = post.getUrl();
            }

            if (imageUrl != null) {
                mGlide.load(imageUrl).into(binding.mediaImageViewFragmentTiktokPost);
            } else {
                binding.mediaImageViewFragmentTiktokPost.setVisibility(View.GONE);
            }

            View.OnClickListener openDetailListener = v -> {
                Intent intent = new Intent(requireContext(), ViewPostDetailActivity.class);
                intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                startActivity(intent);
            };

            binding.commentsImageViewFragmentTiktokPost.setOnClickListener(openDetailListener);
            binding.commentsCountTextViewFragmentTiktokPost.setOnClickListener(openDetailListener);
            binding.titleTextViewFragmentTiktokPost.setOnClickListener(openDetailListener);

            binding.rootLayoutFragmentTiktokPost.setOnClickListener(v -> {
                if (binding.overlayLayoutFragmentTiktokPost.getVisibility() == View.VISIBLE) {
                    binding.overlayLayoutFragmentTiktokPost.setVisibility(View.GONE);
                } else {
                    binding.overlayLayoutFragmentTiktokPost.setVisibility(View.VISIBLE);
                }
            });
        }

        return binding.getRoot();
    }
}
