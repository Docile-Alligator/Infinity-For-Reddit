package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class Paging3LoadingStateAdapter extends LoadStateAdapter<Paging3LoadingStateAdapter.LoadStateViewHolder> {
    private BaseActivity activity;
    private CustomThemeWrapper mCustomThemeWrapper;
    private int mErrorStringId;
    private View.OnClickListener mRetryCallback;

    public Paging3LoadingStateAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper, int errorStringId, View.OnClickListener retryCallback) {
        this.activity = activity;
        this.mCustomThemeWrapper = customThemeWrapper;
        this.mErrorStringId = errorStringId;
        this.mRetryCallback = retryCallback;
    }

    @Override
    public void onBindViewHolder(@NonNull LoadStateViewHolder loadStateViewHolder, @NonNull LoadState loadState) {
        loadStateViewHolder.bind(loadState);
    }

    @NonNull
    @Override
    public LoadStateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, @NonNull LoadState loadState) {
        return new LoadStateViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_paging_3_load_state, viewGroup, false), mRetryCallback);
    }

    class LoadStateViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;
        private RelativeLayout mErrorView;
        private TextView mErrorMsg;
        private MaterialButton mRetry;

        LoadStateViewHolder(@NonNull View itemView, @NonNull View.OnClickListener retryCallback) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.progress_bar_item_paging_3_load_state);
            mErrorView = itemView.findViewById(R.id.error_view_relative_layout_item_paging_3_load_state);
            mErrorMsg = itemView.findViewById(R.id.error_text_view_item_paging_3_load_state);
            mRetry = itemView.findViewById(R.id.retry_button_item_paging_3_load_state);

            mErrorMsg.setText(mErrorStringId);
            mErrorMsg.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
            mRetry.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
            mRetry.setTextColor(mCustomThemeWrapper.getButtonTextColor());
            mRetry.setOnClickListener(retryCallback);
            mErrorView.setOnClickListener(retryCallback);

            if (activity.typeface != null) {
                mErrorMsg.setTypeface(activity.typeface);
                mRetry.setTypeface(activity.typeface);
            }
        }

        public void bind(LoadState loadState) {
            mProgressBar.setVisibility(loadState instanceof LoadState.Loading
                    ? View.VISIBLE : View.GONE);
            mErrorView.setVisibility(loadState instanceof LoadState.Error
                    ? View.VISIBLE : View.GONE);
        }
    }
}
