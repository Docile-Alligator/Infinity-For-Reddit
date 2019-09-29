package ml.docilealligator.infinityforreddit.Adapter;

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

public class FlairBottomSheetRecyclerViewAdapter extends RecyclerView.Adapter<FlairBottomSheetRecyclerViewAdapter.FlairViewHolder> {
    private Context context;
    private ArrayList<Flair> flairs;
    private ItemClickListener itemClickListener;
    public FlairBottomSheetRecyclerViewAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FlairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FlairViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flair, null, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FlairViewHolder holder, int position) {
        if (flairs.get(holder.getAdapterPosition()).isEditable()) {
            holder.editFlairImageView.setVisibility(View.VISIBLE);
            holder.editFlairImageView.setOnClickListener(view -> {
                View dialogView = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_edit_flair, null);
                EditText flairEditText = dialogView.findViewById(R.id.flair_edit_text_edit_flair_dialog);
                flairEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.edit_flair)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            Flair flair = flairs.get(holder.getAdapterPosition());
                            flair.setText(flairEditText.getText().toString());
                            itemClickListener.onClick(flair);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });
        }

        if (flairs.get(holder.getAdapterPosition()).isEditable() && flairs.get(holder.getAdapterPosition()).getText().equals("")) {
            holder.itemView.setOnClickListener(view -> holder.editFlairImageView.performClick());
        } else {
            holder.itemView.setOnClickListener(view -> itemClickListener.onClick(flairs.get(holder.getAdapterPosition())));
        }

        holder.flairTextView.setText(flairs.get(holder.getAdapterPosition()).getText());
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
        }
    }
}
