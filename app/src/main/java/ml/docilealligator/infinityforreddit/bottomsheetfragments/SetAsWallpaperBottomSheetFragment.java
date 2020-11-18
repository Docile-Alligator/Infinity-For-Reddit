package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SetAsWallpaperCallback;

public class SetAsWallpaperBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    public static final String EXTRA_VIEW_PAGER_POSITION = "EVPP";

    @BindView(R.id.home_screen_text_view_set_as_wallpaper_bottom_sheet_fragment)
    TextView homeScreenTextvView;
    @BindView(R.id.lock_screen_text_view_set_as_wallpaper_bottom_sheet_fragment)
    TextView lockScreenTextView;
    @BindView(R.id.both_text_view_set_as_wallpaper_bottom_sheet_fragment)
    TextView bothTextView;
    private Activity mActivity;

    public SetAsWallpaperBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set_as_wallpaper_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        Bundle bundle = getArguments();
        int viewPagerPosition = bundle == null ? -1 : bundle.getInt(EXTRA_VIEW_PAGER_POSITION);

        bothTextView.setOnClickListener(view -> {
            if (mActivity instanceof SetAsWallpaperCallback) {
                ((SetAsWallpaperCallback) mActivity).setToBoth(viewPagerPosition);
            }
            dismiss();
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            homeScreenTextvView.setVisibility(View.VISIBLE);
            lockScreenTextView.setVisibility(View.VISIBLE);

            homeScreenTextvView.setOnClickListener(view -> {
                if (mActivity instanceof SetAsWallpaperCallback) {
                    ((SetAsWallpaperCallback) mActivity).setToHomeScreen(viewPagerPosition);
                }
                dismiss();
            });

            lockScreenTextView.setOnClickListener(view -> {
                if (mActivity instanceof SetAsWallpaperCallback) {
                    ((SetAsWallpaperCallback) mActivity).setToLockScreen(viewPagerPosition);
                }
                dismiss();
            });
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }
}