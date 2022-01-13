package ml.docilealligator.infinityforreddit.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import app.futured.hauler.DragDirection;
import app.futured.hauler.HaulerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SetAsWallpaperCallback;
import ml.docilealligator.infinityforreddit.WallpaperSetter;
import ml.docilealligator.infinityforreddit.customviews.ViewPagerBugFixed;
import ml.docilealligator.infinityforreddit.font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontFamily;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.fragments.ViewRedditGalleryImageOrGifFragment;
import ml.docilealligator.infinityforreddit.fragments.ViewRedditGalleryVideoFragment;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ViewRedditGalleryActivity extends AppCompatActivity implements SetAsWallpaperCallback, CustomFontReceiver {

    public static final String EXTRA_REDDIT_GALLERY = "ERG";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_IS_NSFW = "EIN";

    @BindView(R.id.hauler_view_view_reddit_gallery_activity)
    HaulerView haulerView;
    @BindView(R.id.view_pager_view_reddit_gallery_activity)
    ViewPagerBugFixed viewPager;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    Executor executor;
    public Typeface typeface;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ArrayList<Post.Gallery> gallery;
    private String subredditName;
    private boolean isNsfw;
    private boolean useBottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int systemThemeType = Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        switch (systemThemeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                getTheme().applyStyle(R.style.Theme_Normal, true);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                if (sharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                    getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                } else {
                    getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                }
                break;
            case 2:
                if (systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                }
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    getTheme().applyStyle(R.style.Theme_Normal, true);
                } else {
                    if (sharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                        getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                    } else {
                        getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                    }
                }
        }

        getTheme().applyStyle(FontStyle.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(FontFamily.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(TitleFontFamily.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(ContentFontFamily.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name())).getResId(), true);

        setContentView(R.layout.activity_view_reddit_gallery);

        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        useBottomAppBar = sharedPreferences.getBoolean(SharedPreferencesUtils.USE_BOTTOM_TOOLBAR_IN_MEDIA_VIEWER, false);

        if (!useBottomAppBar) {
            ActionBar actionBar = getSupportActionBar();
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparentActionBarAndExoPlayerControllerColor)));
            setTitle(" ");
        } else {
            getSupportActionBar().hide();
        }

        gallery = getIntent().getParcelableArrayListExtra(EXTRA_REDDIT_GALLERY);
        if (gallery == null || gallery.isEmpty()) {
            finish();
            return;
        }
        subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
        isNsfw = getIntent().getBooleanExtra(EXTRA_IS_NSFW, false);

        if (sharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_VERTICALLY_TO_GO_BACK_FROM_MEDIA, true)) {
            haulerView.setOnDragDismissedListener(dragDirection -> {
                int slide = dragDirection == DragDirection.UP ? R.anim.slide_out_up : R.anim.slide_out_down;
                finish();
                overridePendingTransition(0, slide);
            });
        } else {
            haulerView.setDragEnabled(false);
        }

        setupViewPager();
    }

    public boolean isUseBottomAppBar() {
        return useBottomAppBar;
    }

    private void setupViewPager() {
        if (!useBottomAppBar) {
            setToolbarTitle(0);
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    setToolbarTitle(position);
                }
            });
        }
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
    }

    private void setToolbarTitle(int position) {
        if (gallery != null && position >= 0 && position < gallery.size()) {
            if (gallery.get(position).mediaType == Post.Gallery.TYPE_IMAGE) {
                setTitle(Utils.getTabTextWithCustomFont(typeface, Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.view_reddit_gallery_activity_image_label, position + 1, gallery.size()) + "</font>")));
            } else if (gallery.get(position).mediaType == Post.Gallery.TYPE_GIF) {
                setTitle(Utils.getTabTextWithCustomFont(typeface, Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.view_reddit_gallery_activity_gif_label, position + 1, gallery.size()) + "</font>")));
            } else {
                setTitle(Utils.getTabTextWithCustomFont(typeface, Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.view_reddit_gallery_activity_video_label, position + 1, gallery.size()) + "</font>")));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void setToHomeScreen(int viewPagerPosition) {
        if (gallery != null && viewPagerPosition >= 0 && viewPagerPosition < gallery.size()) {
            WallpaperSetter.set(executor, new Handler(), gallery.get(viewPagerPosition).url, WallpaperSetter.HOME_SCREEN, this,
                    new WallpaperSetter.SetWallpaperListener() {
                        @Override
                        public void success() {
                            Toast.makeText(ViewRedditGalleryActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(ViewRedditGalleryActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void setToLockScreen(int viewPagerPosition) {
        if (gallery != null && viewPagerPosition >= 0 && viewPagerPosition < gallery.size()) {
            WallpaperSetter.set(executor, new Handler(), gallery.get(viewPagerPosition).url, WallpaperSetter.LOCK_SCREEN, this,
                    new WallpaperSetter.SetWallpaperListener() {
                        @Override
                        public void success() {
                            Toast.makeText(ViewRedditGalleryActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(ViewRedditGalleryActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void setToBoth(int viewPagerPosition) {
        if (gallery != null && viewPagerPosition >= 0 && viewPagerPosition < gallery.size()) {
            WallpaperSetter.set(executor, new Handler(), gallery.get(viewPagerPosition).url, WallpaperSetter.BOTH_SCREENS, this,
                    new WallpaperSetter.SetWallpaperListener() {
                        @Override
                        public void success() {
                            Toast.makeText(ViewRedditGalleryActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(ViewRedditGalleryActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public int getCurrentPagePosition() {
        return viewPager.getCurrentItem();
    }

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Post.Gallery media = gallery.get(position);
            if (media.mediaType == Post.Gallery.TYPE_VIDEO) {
                ViewRedditGalleryVideoFragment fragment = new ViewRedditGalleryVideoFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(ViewRedditGalleryVideoFragment.EXTRA_REDDIT_GALLERY_VIDEO, media);
                bundle.putString(ViewRedditGalleryVideoFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                bundle.putInt(ViewRedditGalleryVideoFragment.EXTRA_INDEX, position);
                bundle.putInt(ViewRedditGalleryVideoFragment.EXTRA_MEDIA_COUNT, gallery.size());
                bundle.putBoolean(ViewRedditGalleryVideoFragment.EXTRA_IS_NSFW, isNsfw);
                fragment.setArguments(bundle);
                return fragment;
            } else {
                ViewRedditGalleryImageOrGifFragment fragment = new ViewRedditGalleryImageOrGifFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(ViewRedditGalleryImageOrGifFragment.EXTRA_REDDIT_GALLERY_MEDIA, media);
                bundle.putString(ViewRedditGalleryImageOrGifFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                bundle.putInt(ViewRedditGalleryImageOrGifFragment.EXTRA_INDEX, position);
                bundle.putInt(ViewRedditGalleryImageOrGifFragment.EXTRA_MEDIA_COUNT, gallery.size());
                bundle.putBoolean(ViewRedditGalleryImageOrGifFragment.EXTRA_IS_NSFW, false);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            return gallery.size();
        }
    }
}
