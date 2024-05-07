package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerAccountBinding;

public class AccountChooserRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final BaseActivity baseActivity;
    private ArrayList<Account> accounts;
    private final RequestManager glide;
    private final int primaryTextColor;
    private final ItemClickListener itemClickListener;

    public AccountChooserRecyclerViewAdapter(BaseActivity baseActivity, CustomThemeWrapper customThemeWrapper,
                                             RequestManager glide, ItemClickListener itemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountViewHolder(ItemNavDrawerAccountBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AccountViewHolder) {
            glide.load(accounts.get(position).getProfileImageUrl())
                    .error(glide.load(R.drawable.subreddit_default_icon))
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                    .into(((AccountViewHolder) holder).binding.profileImageItemAccount);
            ((AccountViewHolder) holder).binding.usernameTextViewItemAccount.setText(accounts.get(position).getAccountName());
            holder.itemView.setOnClickListener(view ->
                    itemClickListener.onClick(accounts.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return accounts == null ? 0 : accounts.size();
    }

    public void changeAccountsDataset(List<Account> accounts) {
        this.accounts = (ArrayList<Account>) accounts;
        notifyDataSetChanged();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        ItemNavDrawerAccountBinding binding;

        AccountViewHolder(@NonNull ItemNavDrawerAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (baseActivity.typeface != null) {
                binding.usernameTextViewItemAccount.setTypeface(baseActivity.typeface);
            }
            binding.usernameTextViewItemAccount.setTextColor(primaryTextColor);
        }
    }

    public interface ItemClickListener {
        void onClick(Account account);
    }
}
