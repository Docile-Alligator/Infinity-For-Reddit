package ml.docilealligator.infinityforreddit;

import Account.Account;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;

class AccountRecyclerViewAdapter extends
    RecyclerView.Adapter<AccountRecyclerViewAdapter.AccountViewHolder> {

  private final Context mContext;
  private final RequestManager mGlide;
  private final ItemSelectedListener mItemSelectedListener;
  private List<Account> mAccounts;
  private String mCurrentAccountName;

  AccountRecyclerViewAdapter(Context context, RequestManager glide, String currentAccountName,
      ItemSelectedListener itemSelectedListener) {
    mContext = context;
    mGlide = glide;
    mCurrentAccountName = currentAccountName;
    mItemSelectedListener = itemSelectedListener;
  }

  @NonNull
  @Override
  public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new AccountViewHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
    if (mAccounts == null) {
      mGlide.load(R.drawable.subreddit_default_icon)
          .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
          .into(holder.profileImageGifImageView);
      holder.usernameTextView.setText(R.string.add_account);
      holder.itemView.setOnClickListener(view -> mItemSelectedListener.addAccountSelected());
    } else {
      if (position < mAccounts.size()) {
        mGlide.load(mAccounts.get(position).getProfileImageUrl())
            .error(mGlide.load(R.drawable.subreddit_default_icon))
            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
            .into(holder.profileImageGifImageView);
        holder.usernameTextView.setText(mAccounts.get(position).getUsername());
        holder.itemView.setOnClickListener(view -> {
          mCurrentAccountName = mAccounts.get(position).getUsername();
          mItemSelectedListener.accountSelected(mAccounts.get(position));
        });
      } else if (position == mAccounts.size()) {
        holder.profileImageGifImageView
            .setColorFilter(ContextCompat.getColor(mContext, R.color.primaryTextColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
        mGlide.load(R.drawable.ic_outline_add_circle_outline_24px)
            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
            .into(holder.profileImageGifImageView);
        holder.usernameTextView.setText(R.string.add_account);
        holder.itemView.setOnClickListener(view -> mItemSelectedListener.addAccountSelected());
      } else if (position == mAccounts.size() + 1) {
        holder.profileImageGifImageView
            .setColorFilter(ContextCompat.getColor(mContext, R.color.primaryTextColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
        mGlide.load(R.drawable.ic_outline_public_24px)
            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
            .into(holder.profileImageGifImageView);
        holder.usernameTextView.setText(R.string.anonymous_account);
        holder.itemView.setOnClickListener(view -> mItemSelectedListener.anonymousSelected());
      } else {
        holder.profileImageGifImageView
            .setColorFilter(ContextCompat.getColor(mContext, R.color.primaryTextColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
        mGlide.load(R.drawable.ic_outline_block_24px)
            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
            .into(holder.profileImageGifImageView);
        holder.usernameTextView.setText(R.string.log_out);
        holder.itemView.setOnClickListener(view -> mItemSelectedListener.logoutSelected());
      }
    }
  }

  @Override
  public int getItemCount() {
    if (mAccounts == null) {
      return 1;
    } else {
      if (mCurrentAccountName == null) {
        return mAccounts.size() + 1;
      } else {
        return mAccounts.size() + 3;
      }
    }
  }

  @Override
  public void onViewRecycled(@NonNull AccountViewHolder holder) {
    mGlide.clear(holder.profileImageGifImageView);
    holder.profileImageGifImageView.clearColorFilter();
  }

  void changeAccountsDataset(List<Account> accounts) {
    mAccounts = accounts;
    notifyDataSetChanged();
  }

  interface ItemSelectedListener {

    void accountSelected(Account account);

    void addAccountSelected();

    void anonymousSelected();

    void logoutSelected();
  }

  class AccountViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.profile_image_item_account)
    GifImageView profileImageGifImageView;
    @BindView(R.id.username_text_view_item_account)
    TextView usernameTextView;

    AccountViewHolder(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
