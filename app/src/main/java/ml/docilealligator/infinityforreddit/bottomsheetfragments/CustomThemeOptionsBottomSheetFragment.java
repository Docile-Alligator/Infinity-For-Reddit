package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.customtheme.OnlineCustomThemeMetadata;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentCustomThemeOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomThemeOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_THEME_NAME = "ETN";
    public static final String EXTRA_ONLINE_CUSTOM_THEME_METADATA = "ECT";

    private String themeName;
    private OnlineCustomThemeMetadata onlineCustomThemeMetadata;
    private BaseActivity activity;

    public CustomThemeOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    public interface CustomThemeOptionsBottomSheetFragmentListener {
        void changeName(String oldThemeName);
        void shareTheme(String themeName);
        void shareTheme(OnlineCustomThemeMetadata onlineCustomThemeMetadata);
        void delete(String themeName);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentCustomThemeOptionsBottomSheetBinding binding = FragmentCustomThemeOptionsBottomSheetBinding.inflate(inflater, container, false);

        themeName = getArguments().getString(EXTRA_THEME_NAME);
        onlineCustomThemeMetadata = getArguments().getParcelable(EXTRA_ONLINE_CUSTOM_THEME_METADATA);

        binding.themeNameTextViewCustomThemeOptionsBottomSheetFragment.setText(themeName);

        binding.editThemeTextViewCustomThemeOptionsBottomSheetFragment.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CustomizeThemeActivity.class);
            intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, themeName);
            intent.putExtra(CustomizeThemeActivity.EXTRA_ONLINE_CUSTOM_THEME_METADATA, onlineCustomThemeMetadata);
            startActivity(intent);
            dismiss();
        });

        binding.shareThemeTextViewCustomThemeOptionsBottomSheetFragment.setOnClickListener(view -> {
            if (onlineCustomThemeMetadata != null) {
                ((CustomThemeOptionsBottomSheetFragmentListener) activity).shareTheme(onlineCustomThemeMetadata);
            } else {
                ((CustomThemeOptionsBottomSheetFragmentListener) activity).shareTheme(themeName);
            }
            dismiss();
        });

        binding.changeThemeNameTextViewCustomThemeOptionsBottomSheetFragment.setOnClickListener(view -> {
            ((CustomThemeOptionsBottomSheetFragmentListener) activity).changeName(themeName);
            dismiss();
        });

        binding.deleteThemeTextViewCustomThemeOptionsBottomSheetFragment.setOnClickListener(view -> {
            ((CustomThemeOptionsBottomSheetFragmentListener) activity).delete(themeName);
            dismiss();
        });

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}
