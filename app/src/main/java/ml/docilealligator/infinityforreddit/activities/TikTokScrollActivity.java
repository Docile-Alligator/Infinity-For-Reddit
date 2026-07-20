package ml.docilealligator.infinityforreddit.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityTiktokScrollBinding;
import ml.docilealligator.infinityforreddit.fragments.TikTokPostFragment;
import ml.docilealligator.infinityforreddit.post.Post;

public class TikTokScrollActivity extends BaseActivity {

    public static final String EXTRA_POSTS = "ETPS";

    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    private ActivityTiktokScrollBinding binding;
    private ArrayList<Post> posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);

        binding = ActivityTiktokScrollBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (getIntent() != null && getIntent().hasExtra(EXTRA_POSTS)) {
            posts = getIntent().getParcelableArrayListExtra(EXTRA_POSTS);
        }

        if (posts == null) {
            posts = new ArrayList<>();
        }

        binding.viewPagerTiktokScrollActivity.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        binding.viewPagerTiktokScrollActivity.setAdapter(new TikTokPagerAdapter(this, posts));
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    private static class TikTokPagerAdapter extends FragmentStateAdapter {
        private final ArrayList<Post> posts;

        public TikTokPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Post> posts) {
            super(fragmentActivity);
            this.posts = posts;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return TikTokPostFragment.newInstance(posts.get(position));
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }
    }
}
