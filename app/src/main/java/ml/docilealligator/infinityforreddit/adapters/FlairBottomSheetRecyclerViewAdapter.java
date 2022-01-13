package ml.docilealligator.infinityforreddit.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class FlairBottomSheetRecyclerViewAdapter extends RecyclerView.Adapter<FlairBottomSheetRecyclerViewAdapter.FlairViewHolder> {
    private BaseActivity activity;
    private ArrayList<Flair> flairs;
    private int flairTextColor;
    private ItemClickListener itemClickListener;

    public FlairBottomSheetRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                               ItemClickListener itemClickListener) {
        this.activity = activity;
        flairTextColor = customThemeWrapper.getPrimaryTextColor();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FlairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FlairViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flair, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FlairViewHolder holder, int position) {
        if (flairs.get(holder.getBindingAdapterPosition()).isEditable()) {
            holder.editFlairImageView.setVisibility(View.VISIBLE);
            holder.editFlairImageView.setOnClickListener(view -> {
                View dialogView = ((Activity) activity).getLayoutInflater().inflate(R.layout.dialog_edit_flair, null);
                EditText flairEditText = dialogView.findViewById(R.id.flair_edit_text_edit_flair_dialog);
                flairEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.edit_flair)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            Flair flair = flairs.get(holder.getBindingAdapterPosition());
                            flair.setText(flairEditText.getText().toString());
                            itemClickListener.onClick(flair);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });
        }

        if (flairs.get(holder.getBindingAdapterPosition()).isEditable() && flairs.get(holder.getBindingAdapterPosition()).getText().equals("")) {
            holder.itemView.setOnClickListener(view -> holder.editFlairImageView.performClick());
        } else {
            holder.itemView.setOnClickListener(view -> itemClickListener.onClick(flairs.get(holder.getBindingAdapterPosition())));
        }

        holder.flairTextView.setText(flairs.get(holder.getBindingAdapterPosition()).getText());
    }

    @Override
    public int getItemCount() {
        return flairs == null ? 0 : flairs.size();
    }

    @Override
    public void onViewRecycled(@NonNull FlairViewHolder holder) {
        super.onViewRecycled(holder);
        holder.editFlairImageView.setVisibility(View.GONE);
    }

    public void changeDataset(ArrayList<Flair> flairs) {
        this.flairs = flairs;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onClick(Flair flair);
    }

    class FlairViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        @BindView(R.id.flair_text_view_item_flair)
        TextView flairTextView;
        @BindView(R.id.edit_flair_image_view_item_flair)
        ImageView editFlairImageView;

        FlairViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            flairTextView.setTextColor(flairTextColor);

            if (activity.typeface != null) {
                flairTextView.setTypeface(activity.typeface);
            }
        }
    }
}
