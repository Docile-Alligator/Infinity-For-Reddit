package ml.docilealligator.infinityforreddit.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.ImgurMedia;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SetAsWallpaperCallback;
import ml.docilealligator.infinityforreddit.activities.ViewImgurMediaActivity;
import ml.docilealligator.infinityforreddit.asynctasks.SaveBitmapImageToFile;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SetAsWallpaperBottomSheetFragment;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;

public class ViewImgurImageFragment extends Fragment {

    public static final String EXTRA_IMGUR_IMAGES = "EII";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @BindView(R.id.progress_bar_view_imgur_image_fragment)
    ProgressBar progressBar;
    @BindView(R.id.image_view_view_imgur_image_fragment)
    SubsamplingScaleImageView imageView;
    @BindView(R.id.load_image_error_linear_layout_view_imgur_image_fragment)
    LinearLayout errorLinearLayout;
    @Inject
    Executor mExecutor;

    private ViewImgurMediaActivity activity;
    private RequestManager glide;
    private ImgurMedia imgurMedia;
    private boolean isDownloading = false;
    private boolean isActionBarHidden = false;

    public ViewImgurImageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_imgur_images, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        setHasOptionsMenu(true);

        imgurMedia = getArguments().getParcelable(EXTRA_IMGUR_IMAGES);
        glide = Glide.with(activity);
        loadImage();

        imageView.setOnClickListener(view -> {
            if (isActionBarHidden) {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                isActionBarHidden = false;
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                isActionBarHidden = true;
            }
        });
        imageView.setMinimumDpi(80);
        imageView.setDoubleTapZoomDpi(240);
        imageView.resetScaleAndCenter();

        errorLinearLayout.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            errorLinearLayout.setVisibility(View.GONE);
            loadImage();
        });

        return rootView;
    }

    private void loadImage() {
        glide.asBitmap().load(imgurMedia.getLink()).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                errorLinearLayout.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                imageView.setImage(ImageSource.bitmap(resource));
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.view_imgur_image_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_download_view_imgur_image_fragment) {
            if (isDownloading) {
                return false;
            }

            isDownloading = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // No explanation needed; request the permission
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    // Permission has already been granted
                    download();
                }
            } else {
                download();
            }

            return true;
        } else if (itemId == R.id.action_share_view_imgur_image_fragment) {
            glide.asBitmap().load(imgurMedia.getLink()).into(new CustomTarget<Bitmap>() {

                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    if (activity.getExternalCacheDir() != null) {
                        Toast.makeText(activity, R.string.save_image_first, Toast.LENGTH_SHORT).show();
                        SaveBitmapImageToFile.SaveBitmapImageToFile(mExecutor, new Handler(), resource, activity.getExternalCacheDir().getPath(),
                                imgurMedia.getFileName(),
                                new SaveBitmapImageToFile.SaveBitmapImageToFileListener() {
                                    @Override
                                    public void saveSuccess(File imageFile) {
                                        Uri uri = FileProvider.getUriForFile(activity,
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
                                        Toast.makeText(activity,
                                                R.string.cannot_save_image, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(activity,
                                R.string.cannot_get_storage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
            return true;
        } else if (itemId == R.id.action_set_wallpaper_view_imgur_image_fragment) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SetAsWallpaperBottomSheetFragment setAsWallpaperBottomSheetFragment = new SetAsWallpaperBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(SetAsWallpaperBottomSheetFragment.EXTRA_VIEW_PAGER_POSITION, activity.getCurrentPagePosition());
                setAsWallpaperBottomSheetFragment.setArguments(bundle);
                setAsWallpaperBottomSheetFragment.show(activity.getSupportFragmentManager(), setAsWallpaperBottomSheetFragment.getTag());
            } else {
                ((SetAsWallpaperCallback) activity).setToBoth(activity.getCurrentPagePosition());
            }
            return true;
        }

        return false;
    }

    private void download() {
        isDownloading = false;

        Intent intent = new Intent(activity, DownloadMediaService.class);
        intent.putExtra(DownloadMediaService.EXTRA_URL, imgurMedia.getLink());
        intent.putExtra(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_IMAGE);
        intent.putExtra(DownloadMediaService.EXTRA_FILE_NAME, imgurMedia.getFileName());
        ContextCompat.startForegroundService(activity, intent);
        Toast.makeText(activity, R.string.download_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(activity, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
                isDownloading = false;
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && isDownloading) {
                download();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ViewImgurMediaActivity) context;
    }
}
