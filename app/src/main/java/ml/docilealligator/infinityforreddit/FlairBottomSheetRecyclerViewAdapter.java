package ml.docilealligator.infinityforreddit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

class FlairBottomSheetRecyclerViewAdapter extends RecyclerView.Adapter<FlairBottomSheetRecyclerViewAdapter.FlairViewHolder> {
    interface ItemClickListener {
        void onClick(String flair);
    }

    private ArrayList<String> flairs;
    private ItemClickListener itemClickListener;

    FlairBottomSheetRecyclerViewAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FlairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FlairViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flair, null, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FlairViewHolder holder, int position) {
        holder.flairTextView.setText(flairs.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return flairs == null ? 0 : flairs.size();
    }

    void changeDataset(ArrayList<String> flairs) {
        this.flairs = flairs;
        notifyDataSetChanged();
    }

    class FlairViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.flair_text_view_item_flair) TextView flairTextView;

        FlairViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            flairTextView.setOnClickListener(view -> itemClickListener.onClick(flairs.get(getAdapterPosition())));
        }
    }
}
