package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.post.Post;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostOptionsBottomSheetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private static final String EXTRA_POST = "EP";

    private Post mPost;
    private FragmentPostOptionsBottomSheetBinding binding;

    public PostOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param post Post
     * @return A new instance of fragment PostOptionsBottomSheetFragment.
     */
    public static PostOptionsBottomSheetFragment newInstance(Post post) {
        PostOptionsBottomSheetFragment fragment = new PostOptionsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_POST, post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPost = getArguments().getParcelable(EXTRA_POST);
        } else {
            dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostOptionsBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}