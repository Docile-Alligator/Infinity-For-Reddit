package ml.docilealligator.infinityforreddit.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Activity.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectBaseThemeBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    @BindView(R.id.light_theme_text_view_select_base_theme_bottom_sheet_fragment)
    TextView lightThemeTextView;
    @BindView(R.id.dark_theme_text_view_select_base_theme_bottom_sheet_fragment)
    TextView darkThemeTextView;
    @BindView(R.id.amoled_theme_text_view_select_base_theme_bottom_sheet_fragment)
    TextView amoledThemeTextView;
    private Activity activity;
    public SelectBaseThemeBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_base_theme_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        lightThemeTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CustomizeThemeActivity.class);
            intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_IS_PREDEFIINED_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, getString(R.string.theme_name_indigo));
            startActivity(intent);
            dismiss();
        });

        darkThemeTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CustomizeThemeActivity.class);
            intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_IS_PREDEFIINED_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, getString(R.string.theme_name_indigo_dark));
            startActivity(intent);
            dismiss();
        });

        amoledThemeTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CustomizeThemeActivity.class);
            intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_IS_PREDEFIINED_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, getString(R.string.theme_name_indigo_amoled));
            startActivity(intent);
            dismiss();
        });
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
