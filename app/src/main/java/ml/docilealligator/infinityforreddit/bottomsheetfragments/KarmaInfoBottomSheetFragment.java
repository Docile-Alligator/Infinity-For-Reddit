package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentKarmaInfoBottomSheetBinding;

public class KarmaInfoBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_POST_KARMA = "EPK";
    public static final String EXTRA_COMMENT_KARMA = "ECK";
    public static final String EXTRA_AWARDER_KARMA = "EARK";
    public static final String EXTRA_AWARDEE_KARMA = "EAEK";

    public static KarmaInfoBottomSheetFragment newInstance(int postKarma, int commentKarma, int awarderKarma, int awardeeKarma) {
        KarmaInfoBottomSheetFragment fragment = new KarmaInfoBottomSheetFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_POST_KARMA, postKarma);
        args.putInt(EXTRA_COMMENT_KARMA, commentKarma);
        args.putInt(EXTRA_AWARDER_KARMA, awarderKarma);
        args.putInt(EXTRA_AWARDEE_KARMA, awardeeKarma);
        fragment.setArguments(args);
        return fragment;
    }

    public KarmaInfoBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentKarmaInfoBottomSheetBinding binding = FragmentKarmaInfoBottomSheetBinding.inflate(inflater, container, false);

        int postKarma = getArguments().getInt(EXTRA_POST_KARMA, 0);
        int commentKarma = getArguments().getInt(EXTRA_COMMENT_KARMA, 0);
        int awarderKarma = getArguments().getInt(EXTRA_AWARDER_KARMA, 0);
        int awardeeKarma = getArguments().getInt(EXTRA_AWARDEE_KARMA, 0);

        binding.postKarmaKarmaInfoBottomSheetFragment.setText(Integer.toString(postKarma));
        binding.commentKarmaKarmaInfoBottomSheetFragment.setText(Integer.toString(commentKarma));
        binding.awarderKarmaKarmaInfoBottomSheetFragment.setText(Integer.toString(awarderKarma));
        binding.awardeeKarmaKarmaInfoBottomSheetFragment.setText(Integer.toString(awardeeKarma));

        return binding.getRoot();
    }
}