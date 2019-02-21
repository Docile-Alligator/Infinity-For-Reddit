package ml.docilealligator.infinityforreddit;

import android.Manifest;
import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.pwittchen.swipe.library.rx2.SimpleSwipeListener;
import com.github.pwittchen.swipe.library.rx2.Swipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewImageActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    static final String TITLE_KEY = "TK";
    static final String IMAGE_URL_KEY = "IUK";
    static final String FILE_NAME_KEY = "FNK";

    @BindView(R.id.parent_relative_layout_view_image_activity) RelativeLayout mRelativeLayout;
    @BindView(R.id.progress_bar_view_image_activity) ProgressBar mProgressBar;
    @BindView(R.id.image_view_view_image_activity) GestureImageView mImageView;
    @BindView(R.id.load_image_error_linear_layout_view_image_activity) LinearLayout mLoadErrorLinearLayout;

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
        setContentView(R.layout.activity_view_image);
        ButterKnife.bind(this);

        final ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);

        Intent intent = getIntent();
        mImageUrl = intent.getExtras().getString(IMAGE_URL_KEY);
        mImageFileName = intent.getExtras().getString(FILE_NAME_KEY);
        String title = intent.getExtras().getString(TITLE_KEY);
        final Spannable text = new SpannableString(title);
        setTitle(text);

        mLoadErrorLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSwiping) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mLoadErrorLinearLayout.setVisibility(View.GONE);
                    loadImage();
                }
            }
        });

        final float pxHeight = getResources().getDisplayMetrics().heightPixels;

        int activityColorFrom = getResources().getColor(android.R.color.black);
        int actionBarColorFrom = getResources().getColor(R.color.transparentActionBarColor);
        int actionBarElementColorFrom = getResources().getColor(android.R.color.white);
        int colorTo = getResources().getColor(android.R.color.transparent);

        final ValueAnimator activityColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), activityColorFrom, colorTo);
        final ValueAnimator actionBarColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), actionBarColorFrom, colorTo);
        final ValueAnimator actionBarElementColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), actionBarElementColorFrom, colorTo);

        activityColorAnimation.setDuration(300); // milliseconds
        actionBarColorAnimation.setDuration(300);
        actionBarElementColorAnimation.setDuration(300);

        activityColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mRelativeLayout.setBackgroundColor((int) valueAnimator.getAnimatedValue());
            }
        });

        actionBarColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                actionBar.setBackgroundDrawable(new ColorDrawable((int) valueAnimator.getAnimatedValue()));
            }
        });

        actionBarElementColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                text.setSpan(new ForegroundColorSpan((int) valueAnimator.getAnimatedValue()), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                actionBar.setTitle(text);
                upArrow.setColorFilter((int) valueAnimator.getAnimatedValue(), PorterDuff.Mode.SRC_IN);
                if(mMenu != null) {
                    Drawable drawable = mMenu.getItem(0).getIcon();
                    //drawable.mutate();
                    drawable.setColorFilter((int) valueAnimator.getAnimatedValue(), PorterDuff.Mode.SRC_IN);
                }
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

        mImageView.getController().addOnStateChangeListener(new GestureController.OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                zoom = state.getZoom();
            }

            @Override
            public void onStateReset(State oldState, State newState) {

            }
        });

        mImageView.getController().getSettings().setPanEnabled(true);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        getMenuInflater().inflate(R.menu.view_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_download_view_image:
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
                        saveImage();
                    }
                } else {
                    saveImage();
                }

                return true;
        }

        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(zoom == 1.0) {
            swipe.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "No storage permission to save this file", Toast.LENGTH_SHORT).show();
            } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED && isDownloading) {
                saveImage();
            }
            isDownloading = false;
        }
    }

    private void saveImage() {
        Glide.with(this)
                .asBitmap()
                .load(mImageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onResourceReady(@NonNull final Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        new AsyncTask<Void, Void, Void>() {
                            private boolean saveSuccess = true;

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                isDownloading = false;
                                if(saveSuccess) {
                                    Toast.makeText(ViewImageActivity.this, "Download completed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ViewImageActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                                    File directory = new File(path + "/Infinity/");
                                    if(!directory.exists()) {
                                        if(!directory.mkdir()) {
                                            saveSuccess = false;
                                            return null;
                                        }
                                    } else {
                                        if(directory.isFile()) {
                                            if(!directory.delete() && !directory.mkdir()) {
                                                saveSuccess = false;
                                                return null;
                                            }
                                        }
                                    }

                                    File file = new File(path + "/Infinity/", mImageFileName + ".jpg");
                                    int postfix = 1;
                                    while(file.exists()) {
                                        file = new File(path + "/Infinity/", mImageFileName + "-" + postfix + ".jpg");
                                    }
                                    OutputStream outputStream = new FileOutputStream(file);

                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                                    outputStream.flush();
                                    outputStream.close();
                                } catch (IOException e) {
                                    saveSuccess = false;
                                    e.printStackTrace();
                                }

                                return null;
                            }
                        }.execute();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
