package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class FontPreviewFragment extends Fragment {

    private SettingsActivity activity;

    public FontPreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_font_preview, container, false);

        if (activity.isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);
                    rootView.setPadding(allInsets.left, 0, allInsets.right, allInsets.bottom);
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        rootView.setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());
        LinearLayout linearLayout = rootView.findViewById(R.id.linear_layout_font_preview_fragment);
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(primaryTextColor);
            }
        }
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (SettingsActivity) context;
    }
}