package ml.docilealligator.infinityforreddit.activities;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.GlideImageViewFactory;

import java.io.File;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SetAsWallpaperCallback;
import ml.docilealligator.infinityforreddit.WallpaperSetter;
import ml.docilealligator.infinityforreddit.asynctasks.SaveBitmapImageToFile;
import ml.docilealligator.infinityforreddit.asynctasks.SaveGIFToFile;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SetAsWallpaperBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.customviews.slidr.model.SlidrConfig;
import ml.docilealligator.infinityforreddit.customviews.slidr.model.SlidrPosition;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewImageOrGifBinding;
import ml.docilealligator.infinityforreddit.font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontFamily;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ViewImageOrGifActivity extends AppCompatActivity implements SetAsWallpaperCallback, CustomFontReceiver {

    public static final String EXTRA_IMAGE_URL_KEY = "EIUK";
    public static final String EXTRA_GIF_URL_KEY = "EGUK";
    public static final String EXTRA_FILE_NAME_KEY = "EFNK";
    public static final String EXTRA_SUBREDDIT_OR_USERNAME_KEY = "ESOUK";
    public static final String EXTRA_POST_TITLE_KEY = "EPTK";
    public static final String EXTRA_IS_NSFW = "EIN";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    Executor mExecutor;
    private boolean isActionBarHidden = false;
    private boolean isDownloading = false;
    private RequestManager glide;
    private String mImageUrl;
    private String mImageFileName;
    private String mSubredditName;
    private boolean isGif = true;
    private boolean isNsfw;
    private Typeface typeface;
    private Handler handler;
    private ActivityViewImageOrGifBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        getTheme().applyStyle(R.style.Theme_Normal, true);

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(FontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(TitleFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(ContentFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name())).getResId(), true);

        BigImageViewer.initialize(GlideImageLoader.with(this.getApplicationContext()));

        binding = ActivityViewImageOrGifBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_VERTICALLY_TO_GO_BACK_FROM_MEDIA, true)) {
            Slidr.attach(this, new SlidrConfig.Builder().position(SlidrPosition.VERTICAL).distanceThreshold(0.125f).build());
        }

        glide = Glide.with(this);

        handler = new Handler();

        Intent intent = getIntent();
        mImageUrl = intent.getStringExtra(EXTRA_GIF_URL_KEY);
        if (mImageUrl == null) {
            isGif = false;
            mImageUrl = intent.getStringExtra(EXTRA_IMAGE_URL_KEY);
        }
        mImageFileName = intent.getStringExtra(EXTRA_FILE_NAME_KEY);
        String postTitle = intent.getStringExtra(EXTRA_POST_TITLE_KEY);
        mSubredditName = intent.getStringExtra(EXTRA_SUBREDDIT_OR_USERNAME_KEY);
        isNsfw = intent.getBooleanExtra(EXTRA_IS_NSFW, false);

        boolean useBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.USE_BOTTOM_TOOLBAR_IN_MEDIA_VIEWER, false);
        if (postTitle != null) {
            Spanned title = Html.fromHtml(String.format("<font color=\"#FFFFFF\"><small>%s</small></font>", postTitle));
            if (useBottomAppBar) {
                binding.titleTextViewViewImageOrGifActivity.setText(title);
            } else {
                setTitle(Utils.getTabTextWithCustomFont(typeface, title));
            }
        } else {
            if (!useBottomAppBar) {
                setTitle("");
            }
        }

        if (useBottomAppBar) {
            getSupportActionBar().hide();
            binding.bottomNavigationViewImageOrGifActivity.setVisibility(View.VISIBLE);
            binding.downloadImageViewViewImageOrGifActivity.setOnClickListener(view -> {
                if (isDownloading) {
                    return;
                }
                isDownloading = true;
                requestPermissionAndDownload();
            });
            binding.shareImageViewViewImageOrGifActivity.setOnClickListener(view -> {
                if (isGif)
                    shareGif();
                else
                    shareImage();
            });
            binding.wallpaperImageViewViewImageOrGifActivity.setOnClickListener(view -> {
                setWallpaper();
            });
        } else {
            ActionBar actionBar = getSupportActionBar();
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparentActionBarAndExoPlayerControllerColor)));
        }

        binding.loadImageErrorLinearLayoutViewImageOrGifActivity.setOnClickListener(view -> {
            binding.progressBarViewImageOrGifActivity.setVisibility(View.VISIBLE);
            binding.loadImageErrorLinearLayoutViewImageOrGifActivity.setVisibility(View.GONE);
            loadImage();
        });

        binding.imageViewViewImageOrGifActivity.setOnClickListener(view -> {
            if (isActionBarHidden) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                isActionBarHidden = false;
                if (useBottomAppBar) {
                    binding.bottomNavigationViewImageOrGifActivity.setVisibility(View.VISIBLE);
                }
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                isActionBarHidden = true;
                if (useBottomAppBar) {
                    binding.bottomNavigationViewImageOrGifActivity.setVisibility(View.GONE);
                }
            }
        });

        binding.imageViewViewImageOrGifActivity.setImageViewFactory(new GlideImageViewFactory());

        binding.imageViewViewImageOrGifActivity.setImageLoaderCallback(new ImageLoader.Callback() {
            @Override
            public void onCacheHit(int imageType, File image) {

            }

            @Override
            public void onCacheMiss(int imageType, File image) {

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(File image) {
                binding.progressBarViewImageOrGifActivity.setVisibility(View.GONE);

                final SubsamplingScaleImageView view = binding.imageViewViewImageOrGifActivity.getSSIV();

                if (view != null) {
                    view.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
                        @Override
                        public void onImageLoaded() {
                            view.setMinimumDpi(80);
                            view.setDoubleTapZoomDpi(240);
                            view.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_FIXED);
                            view.setQuickScaleEnabled(true);
                            view.resetScaleAndCenter();
                        }
                    });
                }
            }

            @Override
            public void onFail(Exception error) {
                binding.progressBarViewImageOrGifActivity.setVisibility(View.GONE);
                binding.loadImageErrorLinearLayoutViewImageOrGifActivity.setVisibility(View.VISIBLE);
            }
        });

        loadImage();

        // Fixes #383
        // Not having a background will cause visual glitches on some devices.
        FrameLayout slidablePanel = findViewById(R.id.slidable_panel);
        if (slidablePanel != null) {
            slidablePanel.setBackgroundColor(getResources().getColor(android.R.color.black));
        }
    }

    private void loadImage() {
        binding.imageViewViewImageOrGifActivity.showImage(Uri.parse(mImageUrl));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_image_or_gif_activity, menu);
        for (int i = 0; i < menu.size(); i++) {
            Utils.setTitleWithCustomFontToMenuItem(typeface, menu.getItem(i), null);
        }
        if (!isGif) {
            menu.findItem(R.id.action_set_wallpaper_view_image_or_gif_activity).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_download_view_image_or_gif_activity) {
            if (isDownloading) {
                return false;
            }
            isDownloading = true;
            requestPermissionAndDownload();
            return true;
        } else if (itemId == R.id.action_share_view_image_or_gif_activity) {
            if (isGif)
                shareGif();
            else
                shareImage();
            return true;
        } else if (itemId == R.id.action_set_wallpaper_view_image_or_gif_activity) {
            setWallpaper();
            return true;
        }

        return false;
    }

    private void requestPermissionAndDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                // Permission has already been granted
                download();
            }
        } else {
            download();
        }
    }

    private void download() {
        isDownloading = false;

        PersistableBundle extras = new PersistableBundle();
        extras.putString(DownloadMediaService.EXTRA_URL, mImageUrl);
        extras.putInt(DownloadMediaService.EXTRA_MEDIA_TYPE, isGif ? DownloadMediaService.EXTRA_MEDIA_TYPE_GIF : DownloadMediaService.EXTRA_MEDIA_TYPE_IMAGE);
        extras.putString(DownloadMediaService.EXTRA_FILE_NAME, mImageFileName);
        extras.putString(DownloadMediaService.EXTRA_SUBREDDIT_NAME, mSubredditName);
        extras.putInt(DownloadMediaService.EXTRA_IS_NSFW, isNsfw ? 1 : 0);

        //TODO: contentEstimatedBytes
        JobInfo jobInfo = DownloadMediaService.constructJobInfo(this, 5000000, extras);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);

        Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
    }

    private void shareImage() {
        glide.asBitmap().load(mImageUrl).into(new CustomTarget<Bitmap>() {

            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (getExternalCacheDir() != null) {
                    Toast.makeText(ViewImageOrGifActivity.this, R.string.save_image_first, Toast.LENGTH_SHORT).show();
                    SaveBitmapImageToFile.SaveBitmapImageToFile(mExecutor, handler, resource,
                            getExternalCacheDir().getPath(), mImageFileName,
                            new SaveBitmapImageToFile.SaveBitmapImageToFileListener() {
                                @Override
                                public void saveSuccess(File imageFile) {
                                    Uri uri = FileProvider.getUriForFile(ViewImageOrGifActivity.this,
                                            BuildConfig.APPLICATION_ID + ".provider", imageFile);
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    shareIntent.setType("image/*");
                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                                }

                                @Override
                                public void saveFailed() {
                                    Toast.makeText(ViewImageOrGifActivity.this,
                                            R.string.cannot_save_image, Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(ViewImageOrGifActivity.this,
                            R.string.cannot_get_storage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    private void shareGif() {
        Toast.makeText(ViewImageOrGifActivity.this, R.string.save_gif_first, Toast.LENGTH_SHORT).show();
        glide.asGif().load(mImageUrl).listener(new RequestListener<>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                if (getExternalCacheDir() != null) {
                    SaveGIFToFile.saveGifToFile(mExecutor, handler, resource, getExternalCacheDir().getPath(), mImageFileName,
                            new SaveGIFToFile.SaveGIFToFileListener() {
                                @Override
                                public void saveSuccess(File imageFile) {
                                    Uri uri = FileProvider.getUriForFile(ViewImageOrGifActivity.this,
                                            BuildConfig.APPLICATION_ID + ".provider", imageFile);
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    shareIntent.setType("image/gif");
                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                                }

                                @Override
                                public void saveFailed() {
                                    Toast.makeText(ViewImageOrGifActivity.this,
                                            R.string.cannot_save_gif, Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(ViewImageOrGifActivity.this,
                            R.string.cannot_get_storage, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        }).submit();
    }

    private void setWallpaper() {
        if (!isGif) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SetAsWallpaperBottomSheetFragment setAsWallpaperBottomSheetFragment = new SetAsWallpaperBottomSheetFragment();
                setAsWallpaperBottomSheetFragment.show(getSupportFragmentManager(), setAsWallpaperBottomSheetFragment.getTag());
            } else {
                WallpaperSetter.set(mExecutor, handler, mImageUrl, WallpaperSetter.BOTH_SCREENS, this,
                        new WallpaperSetter.SetWallpaperListener() {
                            @Override
                            public void success() {
                                Toast.makeText(ViewImageOrGifActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                Toast.makeText(ViewImageOrGifActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
                isDownloading = false;
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && isDownloading) {
                download();
            }
        }
    }

    @Override
    public void setToHomeScreen(int viewPagerPosition) {
        WallpaperSetter.set(mExecutor, handler, mImageUrl, WallpaperSetter.HOME_SCREEN, this,
                new WallpaperSetter.SetWallpaperListener() {
                    @Override
                    public void success() {
                        Toast.makeText(ViewImageOrGifActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failed() {
                        Toast.makeText(ViewImageOrGifActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void setToLockScreen(int viewPagerPosition) {
        WallpaperSetter.set(mExecutor, handler, mImageUrl, WallpaperSetter.LOCK_SCREEN, this,
                new WallpaperSetter.SetWallpaperListener() {
                    @Override
                    public void success() {
                        Toast.makeText(ViewImageOrGifActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failed() {
                        Toast.makeText(ViewImageOrGifActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void setToBoth(int viewPagerPosition) {
        WallpaperSetter.set(mExecutor, handler, mImageUrl, WallpaperSetter.BOTH_SCREENS, this,
                new WallpaperSetter.SetWallpaperListener() {
                    @Override
                    public void success() {
                        Toast.makeText(ViewImageOrGifActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failed() {
                        Toast.makeText(ViewImageOrGifActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BigImageViewer.imageLoader().cancelAll();
    }

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
    }
}
