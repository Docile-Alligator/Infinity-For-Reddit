package ml.docilealligator.infinityforreddit.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.core.spans.CustomTypefaceSpan;
import ml.docilealligator.infinityforreddit.MediaMetadata;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.UploadedImage;
import retrofit2.Retrofit;

public final class Utils {
    public static final int NETWORK_TYPE_OTHER = -1;
    public static final int NETWORK_TYPE_WIFI = 0;
    public static final int NETWORK_TYPE_CELLULAR = 1;
    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    private static final long YEAR_MILLIS = 12 * MONTH_MILLIS;
    private static final Pattern[] REGEX_PATTERNS = {
            Pattern.compile("((?<=[\\s])|^)/[rRuU]/[\\w-]+/{0,1}"),
            Pattern.compile("((?<=[\\s])|^)[rRuU]/[\\w-]+/{0,1}"),
            Pattern.compile("\\^{2,}"),
            //Sometimes the reddit preview images and gifs have a caption and the markdown will become [caption](image_link)
            //Matches preview.redd.it and i.redd.it media
            //For i.redd.it media, it only matches [caption](image-link. Notice there is no ) at the end.
            //i.redd.it: (\\[(?:(?!((?<!\\\\)\\[)).)*?]\\()?https://i.redd.it/\\w+.(jpg|png|jpeg|gif)"
            Pattern.compile("((?:\\[(?:(?!(?:(?<!\\\\)\\[)).)*?]\\()?https://preview.redd.it/\\w+.(?:jpg|png|jpeg)(?:(?:\\?+[-a-zA-Z0-9()@:%_+.~#?&/=]*)|))|((?:\\[(?:(?!(?:(?<!\\\\)\\[)).)*?]\\()?https://i.redd.it/\\w+.(?:jpg|png|jpeg|gif))"),
    };

    public static String modifyMarkdown(String markdown) {
        String regexed = REGEX_PATTERNS[0].matcher(markdown).replaceAll("[$0](https://www.reddit.com$0)");
        regexed = REGEX_PATTERNS[1].matcher(regexed).replaceAll("[$0](https://www.reddit.com/$0)");
        regexed = REGEX_PATTERNS[2].matcher(regexed).replaceAll("^");

        return regexed;
    }

    public static String parseRedditImagesBlock(String markdown, @Nullable Map<String, MediaMetadata> mediaMetadataMap) {
        if (mediaMetadataMap == null) {
            return markdown;
        }

        StringBuilder markdownStringBuilder = new StringBuilder(markdown);
        Pattern previewReddItAndIReddItImagePattern = REGEX_PATTERNS[3];
        Matcher matcher = previewReddItAndIReddItImagePattern.matcher(markdownStringBuilder);
        int start = 0;
        int previewReddItLength = "https://preview.redd.it/".length();
        int iReddItLength = "https://i.redd.it/".length();
        while (matcher.find(start)) {
            if (matcher.group(1) != null) {
                String id;
                String caption = null;
                if (markdownStringBuilder.charAt(matcher.start()) == '[') {
                    //Has caption
                    int urlStartIndex = markdownStringBuilder.lastIndexOf("https://preview.redd.it/", matcher.end());
                    id = markdownStringBuilder.substring(previewReddItLength + urlStartIndex,
                            markdownStringBuilder.indexOf(".", previewReddItLength + urlStartIndex));
                    //Minus "](".length()
                    caption = markdownStringBuilder.substring(matcher.start() + 1, urlStartIndex - 2);
                } else {
                    id = markdownStringBuilder.substring(matcher.start() + previewReddItLength,
                            markdownStringBuilder.indexOf(".", matcher.start() + previewReddItLength));
                }

                MediaMetadata mediaMetadata = mediaMetadataMap.get(id);
                if (mediaMetadata == null) {
                    start = matcher.end();
                    continue;
                }

                mediaMetadata.caption = caption;

                if (markdownStringBuilder.charAt(matcher.start()) == '[') {
                    //Has caption
                    markdownStringBuilder.insert(matcher.start(), '!');
                    start = matcher.end() + 1;
                } else {
                    String replacingText = "![](" + markdownStringBuilder.substring(matcher.start(), matcher.end()) + ")";
                    markdownStringBuilder.replace(matcher.start(), matcher.end(), replacingText);
                    start = replacingText.length() + matcher.start();
                }

                matcher = previewReddItAndIReddItImagePattern.matcher(markdownStringBuilder);
            } else if (matcher.group(2) != null) {
                String id;
                String caption = null;
                if (markdownStringBuilder.charAt(matcher.start()) == '[') {
                    //Has caption
                    int urlStartIndex = markdownStringBuilder.lastIndexOf("https://i.redd.it/", matcher.end());
                    id = markdownStringBuilder.substring(iReddItLength + urlStartIndex,
                            markdownStringBuilder.indexOf(".", iReddItLength + urlStartIndex));
                    //Minus "](".length()
                    caption = markdownStringBuilder.substring(matcher.start() + 1, urlStartIndex - 2);
                } else {
                    id = markdownStringBuilder.substring(matcher.start() + iReddItLength, markdownStringBuilder.indexOf(".", matcher.start() + iReddItLength));
                }

                MediaMetadata mediaMetadata = mediaMetadataMap.get(id);
                if (mediaMetadata == null) {
                    start = matcher.end();
                    continue;
                }

                mediaMetadata.caption = caption;

                if (markdownStringBuilder.charAt(matcher.start()) == '[') {
                    //Has caption
                    markdownStringBuilder.insert(matcher.start(), '!');
                    start = matcher.end() + 1;
                } else {
                    String replacingText = "![](" + markdownStringBuilder.substring(matcher.start(), matcher.end()) + ")";
                    markdownStringBuilder.replace(matcher.start(), matcher.end(), replacingText);
                    start = replacingText.length() + matcher.start();
                }

                matcher = previewReddItAndIReddItImagePattern.matcher(markdownStringBuilder);
            } else {
                start = matcher.end();
            }
        }

        return markdownStringBuilder.toString();
    }

    public static String trimTrailingWhitespace(String source) {

        if (source == null) {
            return "";
        }

        int i = source.length();

        // loop back to the first non-whitespace character
        do {
            i--;
        } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        return source.substring(0, i + 1);
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null) {
            return "";
        }

        int i = source.length();

        // loop back to the first non-whitespace character
        do {
            i--;
        } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        return source.subSequence(0, i + 1);
    }

    public static String getFormattedTime(Locale locale, long time, String pattern) {
        Calendar postTimeCalendar = Calendar.getInstance();
        postTimeCalendar.setTimeInMillis(time);
        return new SimpleDateFormat(pattern, locale).format(postTimeCalendar.getTime());
    }

    public static String getElapsedTime(Context context, long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < MINUTE_MILLIS) {
            return context.getString(R.string.elapsed_time_just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return context.getString(R.string.elapsed_time_a_minute_ago);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return context.getString(R.string.elapsed_time_minutes_ago, diff / MINUTE_MILLIS);
        } else if (diff < 120 * MINUTE_MILLIS) {
            return context.getString(R.string.elapsed_time_an_hour_ago);
        } else if (diff < 24 * HOUR_MILLIS) {
            return context.getString(R.string.elapsed_time_hours_ago, diff / HOUR_MILLIS);
        } else if (diff < 48 * HOUR_MILLIS) {
            return context.getString(R.string.elapsed_time_yesterday);
        } else if (diff < MONTH_MILLIS) {
            return context.getString(R.string.elapsed_time_days_ago, diff / DAY_MILLIS);
        } else if (diff < 2 * MONTH_MILLIS) {
            return context.getString(R.string.elapsed_time_a_month_ago);
        } else if (diff < YEAR_MILLIS) {
            return context.getString(R.string.elapsed_time_months_ago, diff / MONTH_MILLIS);
        } else if (diff < 2 * YEAR_MILLIS) {
            return context.getString(R.string.elapsed_time_a_year_ago);
        } else {
            return context.getString(R.string.elapsed_time_years_ago, diff / YEAR_MILLIS);
        }
    }

    public static String getNVotes(boolean showAbsoluteNumberOfVotes, int votes) {
        if (showAbsoluteNumberOfVotes) {
            return Integer.toString(votes);
        } else {
            if (Math.abs(votes) < 1000) {
                return Integer.toString(votes);
            }
            return String.format(Locale.US, "%.1f", (float) votes / 1000) + "K";
        }
    }

    public static void setHTMLWithImageToTextView(TextView textView, String content, boolean enlargeImage) {
        GlideImageGetter glideImageGetter = new GlideImageGetter(textView, enlargeImage);
        Spannable html = (Spannable) HtmlCompat.fromHtml(
                content, HtmlCompat.FROM_HTML_MODE_LEGACY, glideImageGetter, null);

        textView.setText(html);
    }

    public static int getConnectedNetwork(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network nw = connMgr.getActiveNetwork();
                if (nw == null) return NETWORK_TYPE_OTHER;
                try {
                    NetworkCapabilities actNw = connMgr.getNetworkCapabilities(nw);
                    if (actNw != null) {
                        if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return NETWORK_TYPE_WIFI;
                        }
                        if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return NETWORK_TYPE_CELLULAR;
                        }
                    }
                } catch (SecurityException ignore) {
                }
            } else {
                boolean isWifi = false;
                boolean isCellular = false;
                for (Network network : connMgr.getAllNetworks()) {
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        isWifi = true;
                    }
                    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        isCellular = true;
                    }
                }

                if (isWifi) {
                    return NETWORK_TYPE_WIFI;
                }

                if (isCellular) {
                    return NETWORK_TYPE_CELLULAR;
                }

            }
            return NETWORK_TYPE_OTHER;
        }

        return NETWORK_TYPE_OTHER;
    }

    public static boolean isConnectedToWifi(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network nw = connMgr.getActiveNetwork();
                if (nw == null) return false;
                NetworkCapabilities actNw = connMgr.getNetworkCapabilities(nw);
                return actNw != null && actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            } else {
                for (Network network : connMgr.getAllNetworks()) {
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        return networkInfo.isConnected();
                    }
                }
            }
        }

        return false;
    }

    public static boolean isConnectedToCellularData(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network nw = connMgr.getActiveNetwork();
                if (nw == null) return false;
                NetworkCapabilities actNw = connMgr.getNetworkCapabilities(nw);
                return actNw != null && actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            } else {
                for (Network network : connMgr.getAllNetworks()) {
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        return networkInfo.isConnected();
                    }
                }
            }
        }

        return false;
    }

    public static void displaySortTypeInToolbar(SortType sortType, Toolbar toolbar) {
        if (sortType != null) {
            if (sortType.getTime() != null) {
                toolbar.setSubtitle(sortType.getType().fullName + ": " + sortType.getTime().fullName);
            } else {
                toolbar.setSubtitle(sortType.getType().fullName);
            }
        }
    }

    public static void showKeyboard(Context context, Handler handler, View view) {
        handler.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Nullable
    public static Drawable getTintedDrawable(Context context, int drawableId, int color) {
        final Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
        if (drawable != null) {
            drawable.setTint(color);
        }
        return drawable;
    }

    public static void uploadImageToReddit(Context context, Executor executor, Retrofit oauthRetrofit,
                                           Retrofit uploadMediaRetrofit, String accessToken, EditText editText,
                                           CoordinatorLayout coordinatorLayout, Uri imageUri,
                                           ArrayList<UploadedImage> uploadedImages) {
        Toast.makeText(context, R.string.uploading_image, Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        executor.execute(() -> {
            try {
                Bitmap bitmap = Glide.with(context).asBitmap().load(imageUri).submit().get();
                String imageKeyOrError = UploadImageUtils.uploadImage(oauthRetrofit, uploadMediaRetrofit, accessToken, bitmap, true);
                handler.post(() -> {
                    if (imageKeyOrError != null && !imageKeyOrError.startsWith("Error: ")) {
                        String fileName = Utils.getFileName(context, imageUri);
                        if (fileName == null) {
                            fileName = imageKeyOrError;
                        }
                        uploadedImages.add(new UploadedImage(fileName, imageKeyOrError));

                        int start = Math.max(editText.getSelectionStart(), 0);
                        int end = Math.max(editText.getSelectionEnd(), 0);
                        int realStart = Math.min(start, end);
                        if (realStart > 0 && editText.getText().toString().charAt(realStart - 1) != '\n') {
                            editText.getText().replace(realStart, Math.max(start, end),
                                    "\n![](" + imageKeyOrError + ")\n",
                                    0, "\n![]()\n".length() + imageKeyOrError.length());
                        } else {
                            editText.getText().replace(realStart, Math.max(start, end),
                                    "![](" + imageKeyOrError + ")\n",
                                    0, "![]()\n".length() + imageKeyOrError.length());
                        }
                        Snackbar.make(coordinatorLayout, R.string.upload_image_success, Snackbar.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, R.string.upload_image_failed, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(context, R.string.get_image_bitmap_failed, Toast.LENGTH_LONG).show());
            } catch (XmlPullParserException | JSONException | IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(context, R.string.error_processing_image, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Nullable
    public static String getFileName(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver != null) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                String fileName = cursor.getString(nameIndex);
                if (fileName != null && fileName.contains(".")) {
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                }
                return fileName;
            }
        }

        return null;
    }

    public static void setTitleWithCustomFontToMenuItem(Typeface typeface, MenuItem item, String desiredTitle) {
        if (typeface != null) {
            CharSequence title = desiredTitle == null ? item.getTitle() : desiredTitle;
            if (title != null) {
                SpannableStringBuilder spannableTitle = new SpannableStringBuilder(title);
                spannableTitle.setSpan(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? new TypefaceSpan(typeface) : new CustomTypefaceSpan(typeface), 0, spannableTitle.length(), 0);
                item.setTitle(spannableTitle);
            }
        } else if (desiredTitle != null) {
            item.setTitle(desiredTitle);
        }
    }

    public static void setTitleWithCustomFontToTab(Typeface typeface, TabLayout.Tab tab, String title) {
        if (typeface != null) {
            if (title != null) {
                SpannableStringBuilder spannableTitle = new SpannableStringBuilder(title);
                spannableTitle.setSpan(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? new TypefaceSpan(typeface) : new CustomTypefaceSpan(typeface), 0, spannableTitle.length(), 0);
                tab.setText(spannableTitle);
            }
        } else {
            tab.setText(title);
        }
    }

    public static CharSequence getTabTextWithCustomFont(Typeface typeface, CharSequence title) {
        if (typeface != null && title != null) {
            SpannableStringBuilder spannableTitle = new SpannableStringBuilder(title);
            spannableTitle.setSpan(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? new TypefaceSpan(typeface) : new CustomTypefaceSpan(typeface), 0, spannableTitle.length(), 0);
            return spannableTitle;
        } else {
            return title;
        }
    }

    public static void setFontToAllTextViews(View rootView, Typeface typeface) {
        if (rootView instanceof TextInputLayout) {
            ((TextInputLayout) rootView).setTypeface(typeface);
        } else if (rootView instanceof ViewGroup) {
            ViewGroup rootViewGroup = ((ViewGroup) rootView);
            int childViewCount = rootViewGroup.getChildCount();
            for (int i = 0; i < childViewCount; i++) {
                setFontToAllTextViews(rootViewGroup.getChildAt(i), typeface);
            }
        } else if (rootView instanceof TextView) {
            ((TextView) rootView).setTypeface(typeface);
        }
    }

    public static <T> int fixIndexOutOfBounds(T[] array, int index) {
        return index >= array.length ? array.length - 1 : index;
    }

    public static <T> int fixIndexOutOfBoundsUsingPredetermined(T[] array, int index, int predeterminedIndex) {
        return index >= array.length ? predeterminedIndex : index;
    }
}
