package ml.docilealligator.infinityforreddit.customviews;

import android.view.View;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class LandscapeExpandedRoundedBottomSheetDialogFragment extends RoundedBottomSheetDialogFragment {
    @Override
    public void onStart() {
        super.onStart();
        View parentView = (View) requireView().getParent();
        BottomSheetBehavior.from(parentView).setState(BottomSheetBehavior.STATE_EXPANDED);
        BottomSheetBehavior.from(parentView).setSkipCollapsed(true);
    }
}
