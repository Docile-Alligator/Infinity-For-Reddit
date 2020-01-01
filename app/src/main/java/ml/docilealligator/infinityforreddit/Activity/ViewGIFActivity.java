package ml.docilealligator.infinityforreddit.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.DownloadManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.pwittchen.swipe.library.rx2.SimpleSwipeListener;
import com.github.pwittchen.swipe.library.rx2.Swipe;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ContentFontStyle;
import ml.docilealligator.infinityforreddit.FontStyle;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.TitleFontStyle;
import pl.droidsonroids.gif.GifImageView;

public class ViewGIFActivity extends AppCompatActivity {

    public static final String IMAGE_URL_KEY = "IUK";
    public static final String FILE_NAME_KEY = "FNK";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    @BindView(R.id.parent_relative_layout_view_gif_activity)
    RelativeLayout mRelativeLayout;
    @BindView(R.id.progress_bar_view_gif_activity)
    ProgressBar mProgressBar;
    @BindView(R.id.image_view_view_gif_activity)
    GifImageView mImageView;
    @BindView(R.id.load_image_error_linear_layout_view_gif_activity)
    LinearLayout mLoadErrorLinearLayout;
    @Inject
    SharedPreferences mSharedPreferences;
    private boolean isActionBarHidden = false;
    private boolean isDownloading = false;
    private Menu mMenu;
    private Swipe swipe;
    private String mImageUrl;
    private String mImageFileName;
    private float totalLengthY = 0.0f;
    private float touchY = -1.0f;
    private float zoom = 1.0f;
    private boolean isSwiping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Infinity) getApplication()).getAppComponent().inject(this);

        getTheme().applyStyle(R.style.Theme_Default, true);

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        setContentView(R.layout.activity_view_gif);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparentActionBarAndExoPlayerControllerColor)));
        setTitle("");

        Intent intent = getIntent();
        mImageUrl = intent.getStringExtra(IMAGE_URL_KEY);
        mImageFileName = intent.getStringExtra(FILE_NAME_KEY);

        mLoadErrorLinearLayout.setOnClickListener(view -> {
            if (!isSwiping) {
                mProgressBar.setVisibility(View.VISIBLE);
                mLoadErrorLinearLayout.setVisibility(View.GONE);
                loadImage();
            }
        });

        float pxHeight = getResources().getDisplayMetrics().heightPixels;

        int activityColorFrom = getResources().getColor(android.R.color.black);
        int actionBarColorFrom = getResources().getColor(R.color.transparentActionBarAndExoPlayerControllerColor);
        int actionBarElementColorFrom = getResources().getColor(android.R.color.white);
        int colorTo = getResources().getColor(android.R.color.transparent);

        final ValueAnimator activityColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), activityColorFrom, colorTo);
        final ValueAnimator actionBarColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), actionBarColorFrom, colorTo);
        final ValueAnimator actionBarElementColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), actionBarElementColorFrom, colorTo);

        activityColorAnimation.setDuration(300); // milliseconds
        actionBarColorAnimation.setDuration(300);
        actionBarElementColorAnimation.setDuration(300);

        activityColorAnimation.addUpdateListener(valueAnimator -> mRelativeLayout.setBackgroundColor((int) valueAnimator.getAnimatedValue()));

        actionBarColorAnimation.addUpdateListener(valueAnimator -> actionBar.setBackgroundDrawable(new ColorDrawable((int) valueAnimator.getAnimatedValue())));

        actionBarElementColorAnimation.addUpdateListener(valueAnimator -> {
            upArrow.setColorFilter((int) valueAnimator.getAnimatedValue(), PorterDuff.Mode.SRC_IN);
            if (mMenu != null) {
                Drawable drawable = mMenu.getItem(0).getIcon();
                //drawable.mutate();
                drawable.setColorFilter((int) valueAnimator.getAnimatedValue(), PorterDuff.Mode.SRC_IN);
            }
        });

        loadImage();

        swipe = new Swipe();
        swipe.setListener(new SimpleSwipeListener() {
            @Override
            public void onSwipingUp(final MotionEvent event) {
                isSwiping = true;
                float nowY = event.getY();
                float offset;
                if (touchY == -1.0f) {
                    offset = 0.0f;
                } else {
                    offset = nowY - touchY;
                }
                totalLengthY += offset;
                touchY = nowY;
                mImageView.animate()
                        .y(totalLengthY)
                        .setDuration(0)
                        .start();
                mLoadErrorLinearLayout.animate()
                        .y(totalLengthY)
                        .setDuration(0)
                        .start();
            }

            @Override
            public boolean onSwipedUp(final MotionEvent event) {
                if (totalLengthY < -pxHeight / 8) {
                    mImageView.animate()
                            .y(-pxHeight)
                            .setDuration(300)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    activityColorAnimation.start();
                                    actionBarColorAnimation.start();
                                    actionBarElementColorAnimation.start();
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    finish();
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {
                                }
                            })
                            .start();
                    mLoadErrorLinearLayout.animate()
                            .y(-pxHeight)
                            .setDuration(300)
                            .start();
                } else {
                    isSwiping = false;
                    mImageView.animate()
                            .y(0)
                            .setDuration(300)
                            .start();
                    mLoadErrorLinearLayout.animate()
                            .y(0)
                            .setDuration(300)
                            .start();
                }

                totalLengthY = 0.0f;
                touchY = -1.0f;
                return false;
            }

            @Override
            public void onSwipingDown(final MotionEvent event) {
                isSwiping = true;
                float nowY = event.getY();
                float offset;
                if (touchY == -1.0f) {
                    offset = 0.0f;
                } else {
                    offset = nowY - touchY;
                }
                totalLengthY += offset;
                touchY = nowY;
                mImageView.animate()
                        .y(totalLengthY)
                        .setDuration(0)
                        .start();
                mLoadErrorLinearLayout.animate()
                        .y(totalLengthY)
                        .setDuration(0)
                        .start();
            }

            @Override
            public boolean onSwipedDown(final MotionEvent event) {
                if (totalLengthY > pxHeight / 8) {
                    mImageView.animate()
                            .y(pxHeight)
                            .setDuration(300)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    activityColorAnimation.start();
                                    actionBarColorAnimation.start();
                                    actionBarElementColorAnimation.start();
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    finish();
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {
                                }
                            })
                            .start();
                    mLoadErrorLinearLayout.animate()
                            .y(pxHeight)
                            .setDuration(300)
                            .start();
                } else {
                    isSwiping = false;
                    mImageView.animate()
                            .y(0)
                            .setDuration(300)
                            .start();
                    mLoadErrorLinearLayout.animate()
                            .y(0)
                            .setDuration(300)
                            .start();
                }

                totalLengthY = 0.0f;
                touchY = -1.0f;

                return false;
            }
        });

        mImageView.setOnClickListener(view -> {
            if (isActionBarHidden) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                isActionBarHidden = false;
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                isActionBarHidden = true;
            }
        });
    }

    private void loadImage() {
        Glide.with(this).load(mImageUrl).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                mProgressBar.setVisibility(View.GONE);
                mLoadErrorLinearLayout.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                mProgressBar.setVisibility(View.GONE);
                return false;
            }
        }).apply(new RequestOptions().fitCenter()).into(mImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.view_gif_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_copy_link_view_image:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.reddit_media_url), mImageUrl);
                clipboard.setPrimaryClip(clip);
                Toast copyToast = Toast.makeText(this, getString(R.string.action_copy_link_success), Toast.LENGTH_SHORT);
                copyToast.show();
                return true;
            case R.id.action_download_view_gif:
                if (isDownloading) {
                    return false;
                }

                isDownloading = true;

                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Permission is not granted
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                    } else {
                        // Permission has already been granted
                        download();
                    }
                } else {
                    download();
                }

                return true;
        }

        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (zoom == 1.0) {
            swipe.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && isDownloading) {
                download();
            }
            isDownloading = false;
        }
    }

    private void download() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mImageUrl));
        request.setTitle(mImageFileName);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //Android Q support
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, mImageFileName);
        } else {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File directory = new File(path + "/Infinity/");
            boolean saveToInfinityFolder = true;
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    saveToInfinityFolder = false;
                }
            } else {
                if (directory.isFile()) {
                    if (!(directory.delete() && directory.mkdir())) {
                        saveToInfinityFolder = false;
                    }
                }
            }

            if (saveToInfinityFolder) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/Infinity/", mImageFileName);
            } else {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, mImageFileName);
            }
        }

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if (manager == null) {
            Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        manager.enqueue(request);
        Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
