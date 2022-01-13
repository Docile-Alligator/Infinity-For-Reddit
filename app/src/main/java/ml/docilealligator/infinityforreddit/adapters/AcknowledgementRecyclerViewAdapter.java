package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.settings.Acknowledgement;

public class AcknowledgementRecyclerViewAdapter extends RecyclerView.Adapter<AcknowledgementRecyclerViewAdapter.AcknowledgementViewHolder> {
    private ArrayList<Acknowledgement> acknowledgements;
    private BaseActivity activity;

    public AcknowledgementRecyclerViewAdapter(BaseActivity activity, ArrayList<Acknowledgement> acknowledgements) {
        this.activity = activity;
        this.acknowledgements = acknowledgements;
    }

    @NonNull
    @Override
    public AcknowledgementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AcknowledgementViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_acknowledgement, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AcknowledgementViewHolder holder, int position) {
        Acknowledgement acknowledgement = acknowledgements.get(holder.getBindingAdapterPosition());
        if (acknowledgement != null) {
            holder.nameTextView.setText(acknowledgement.getName());
            holder.introductionTextView.setText(acknowledgement.getIntroduction());
            holder.itemView.setOnClickListener(view -> {
                if (activity != null) {
                    Intent intent = new Intent(activity, LinkResolverActivity.class);
                    intent.setData(acknowledgement.getLink());
                    activity.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return acknowledgements == null ? 0 : acknowledgements.size();
    }

    class AcknowledgementViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        @BindView(R.id.name_text_view_item_acknowledgement)
        TextView nameTextView;
        @BindView(R.id.introduction_text_view_item_acknowledgement)
        TextView introductionTextView;

        AcknowledgementViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;

            if (activity.typeface != null) {
                nameTextView.setTypeface(activity.typeface);
                introductionTextView.setTypeface(activity.typeface);
            }
        }
    }
}
