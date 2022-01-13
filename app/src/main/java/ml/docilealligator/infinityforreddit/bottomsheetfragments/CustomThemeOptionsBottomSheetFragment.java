package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomThemeOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_THEME_NAME = "ETN";
    @BindView(R.id.theme_name_text_view_custom_theme_options_bottom_sheet_fragment)
    TextView themeNameTextView;
    @BindView(R.id.edit_theme_text_view_custom_theme_options_bottom_sheet_fragment)
    TextView editThemeTextView;
    @BindView(R.id.share_theme_text_view_custom_theme_options_bottom_sheet_fragment)
    TextView shareThemeTextView;
    @BindView(R.id.change_theme_name_text_view_custom_theme_options_bottom_sheet_fragment)
    TextView changeThemeNameTextView;
    @BindView(R.id.delete_theme_text_view_custom_theme_options_bottom_sheet_fragment)
    TextView deleteTextView;
    private String themeName;
    private BaseActivity activity;

    public CustomThemeOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    public interface CustomThemeOptionsBottomSheetFragmentListener {
        void changeName(String oldThemeName);
        void shareTheme(String themeName);
        void delete(String themeName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_custom_theme_options_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        themeName = getArguments().getString(EXTRA_THEME_NAME);
        themeNameTextView.setText(themeName);

        editThemeTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CustomizeThemeActivity.class);
            intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, themeName);
            startActivity(intent);
            dismiss();
        });

        shareThemeTextView.setOnClickListener(view -> {
            ((CustomThemeOptionsBottomSheetFragmentListener) activity).shareTheme(themeName);
            dismiss();
        });

        changeThemeNameTextView.setOnClickListener(view -> {
            ((CustomThemeOptionsBottomSheetFragmentListener) activity).changeName(themeName);
            dismiss();
        });

        deleteTextView.setOnClickListener(view -> {
            ((CustomThemeOptionsBottomSheetFragmentListener) activity).delete(themeName);
            dismiss();
        });

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}
