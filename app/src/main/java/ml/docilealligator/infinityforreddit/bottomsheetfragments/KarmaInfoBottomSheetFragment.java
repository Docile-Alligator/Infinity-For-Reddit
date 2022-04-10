package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import ml.docilealligator.infinityforreddit.R;

public class KarmaInfoBottomSheetFragment extends RoundedBottomSheetDialogFragment {
    public static final String EXTRA_POST_KARMA = "EPK";
    public static final String EXTRA_COMMENT_KARMA = "ECK";
    public static final String EXTRA_AWARDER_KARMA = "EARK";
    public static final String EXTRA_AWARDEE_KARMA = "EAEK";

    private int postKarma;
    private int commentKarma;
    private int awarderKarma;
    private int awardeeKarma;

    public KarmaInfoBottomSheetFragment() {
        // Required empty public constructor
    }


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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_karma_info_bottom_sheet, container, false);

        postKarma = getArguments().getInt(EXTRA_POST_KARMA, 0);
        commentKarma = getArguments().getInt(EXTRA_COMMENT_KARMA, 0);
        awarderKarma = getArguments().getInt(EXTRA_AWARDER_KARMA, 0);
        awardeeKarma = getArguments().getInt(EXTRA_AWARDEE_KARMA, 0);

        TextView postKarmaTextView = rootView.findViewById(R.id.post_karma_karma_info_bottom_sheet_fragment);
        TextView commentKarmaTextView = rootView.findViewById(R.id.comment_karma_karma_info_bottom_sheet_fragment);
        TextView awarderKarmaTextView = rootView.findViewById(R.id.awarder_karma_karma_info_bottom_sheet_fragment);
        TextView awardeeKarmaTextView = rootView.findViewById(R.id.awardee_karma_karma_info_bottom_sheet_fragment);

        postKarmaTextView.setText(Integer.toString(postKarma));
        commentKarmaTextView.setText(Integer.toString(commentKarma));
        awarderKarmaTextView.setText(Integer.toString(awarderKarma));
        awardeeKarmaTextView.setText(Integer.toString(awardeeKarma));

        return rootView;
    }
}