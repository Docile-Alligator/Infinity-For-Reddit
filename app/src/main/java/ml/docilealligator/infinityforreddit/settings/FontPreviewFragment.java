package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;

public class FontPreviewFragment extends Fragment {

    private SettingsActivity activity;

    public FontPreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_font_preview, container, false);

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