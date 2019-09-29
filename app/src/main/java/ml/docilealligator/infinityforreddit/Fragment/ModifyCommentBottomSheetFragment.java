package ml.docilealligator.infinityforreddit.Fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Activity.EditCommentActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ModifyCommentBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    public static final String EXTRA_COMMENT_FULLNAME = "ECF";
    public static final String EXTRA_COMMENT_CONTENT = "ECC";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_POSITION = "EP";

    public ModifyCommentBottomSheetFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.edit_text_view_modify_comment_bottom_sheet_fragment) TextView editTextView;
    @BindView(R.id.delete_text_view_modify_comment_bottom_sheet_fragment) TextView deleteTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_modify_comment_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        Activity activity = getActivity();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        Bundle bundle = getArguments();
        String fullName = bundle.getString(EXTRA_COMMENT_FULLNAME);
        String content = bundle.getString(EXTRA_COMMENT_CONTENT);
        String accessToken = bundle.getString(EXTRA_ACCESS_TOKEN);

        editTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, EditCommentActivity.class);
            intent.putExtra(EditCommentActivity.EXTRA_ACCESS_TOKEN, accessToken);
            intent.putExtra(EditCommentActivity.EXTRA_FULLNAME, fullName);
            intent.putExtra(EditCommentActivity.EXTRA_CONTENT, content);
            intent.putExtra(EditCommentActivity.EXTRA_POSITION, bundle.getInt(EXTRA_POSITION));
            if(activity instanceof ViewPostDetailActivity) {
                activity.startActivityForResult(intent, ViewPostDetailActivity.EDIT_COMMENT_REQUEST_CODE);
            } else {
                startActivity(intent);
            }

            dismiss();
        });

        deleteTextView.setOnClickListener(view -> {
            dismiss();
            if(activity instanceof ViewPostDetailActivity) {
                ((ViewPostDetailActivity) activity).deleteComment(fullName, bundle.getInt(EXTRA_POSITION));
            } else if(activity instanceof ViewUserDetailActivity) {
                ((ViewUserDetailActivity) activity).deleteComment(fullName);
            }
        });

        return rootView;
    }

}
