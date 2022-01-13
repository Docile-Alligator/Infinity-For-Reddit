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
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.core.spans.CustomTypefaceSpan;
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
            Pattern.compile("(^|^ *|\\n *)#(?!($|\\s|#))"),
            Pattern.compile("(^|^ *|\\n *)##(?!($|\\s|#))"),
            Pattern.compile("(^|^ *|\\n *)###(?!($|\\s|#))"),
            Pattern.compile("(^|^ *|\\n *)####(?!($|\\s|#))"),
            Pattern.compile("(^|^ *|\\n *)#####(?!($|\\s|#))"),
            Pattern.compile("(^|^ *|\\n *)######(?!($|\\s|#))"),
            Pattern.compile("!\\[gif]\\(giphy\\|\\w+\\)"),
            Pattern.compile("!\\[gif]\\(giphy\\|\\w+\\|downsized\\)"),
            Pattern.compile("!\\[gif]\\(emote\\|\\w+\\|\\w+\\)"),
    };

    public static String modifyMarkdown(String markdown) {
        String regexed = REGEX_PATTERNS[0].matcher(markdown).replaceAll("[$0](https://www.reddit.com$0)");
        regexed = REGEX_PATTERNS[1].matcher(regexed).replaceAll("[$0](https://www.reddit.com/$0)");
        regexed = REGEX_PATTERNS[2].matcher(regexed).replaceAll("^");
        regexed = REGEX_PATTERNS[3].matcher(regexed).replaceAll("$0 ");
        regexed = REGEX_PATTERNS[4].matcher(regexed).replaceAll("$0 ");
        regexed = REGEX_PATTERNS[5].matcher(regexed).replaceAll("$0 ");
        regexed = REGEX_PATTERNS[6].matcher(regexed).replaceAll("$0 ");
        regexed = REGEX_PATTERNS[7].matcher(regexed).replaceAll("$0 ");
        regexed = REGEX_PATTERNS[8].matcher(regexed).replaceAll("$0 ");

        //return fixSuperScript(regexed);
        // We don't want to fix super scripts here because we need the original markdown later for editing posts
        return regexed;
    }

    public static String fixSuperScript(String regexedMarkdown) {
        StringBuilder regexed = new StringBuilder(regexedMarkdown);
        boolean hasBracket = false;
        int nCarets = 0;
        int new_lines = 0;
        for (int i = 0; i < regexed.length(); i++) {
            char currentChar = regexed.charAt(i);
            if (hasBracket && currentChar == '\n') {
                new_lines++;
                if (new_lines > 1) {
                    hasBracket = false;
                    nCarets = 0;
                    new_lines = 0;
                }
            } else if (currentChar == '^') {
                if (!(i > 0 && regexed.charAt(i - 1) == '\\')) {
                    if (nCarets == 0 && i < regexed.length() - 1 && regexed.charAt(i + 1) == '(') {
                        regexed.replace(i, i + 2, "<sup>");
                        hasBracket = true;
                    } else {
                        regexed.replace(i, i + 1, "<sup>");
                    }
                    nCarets++;
                }
            } else if (hasBracket && currentChar == ')') {
                if (i > 0 && regexed.charAt(i - 1) == '\\') {
                    hasBracket = false;
                    nCarets--;
                    continue;
                }
                hasBracket = false;
                regexed.replace(i, i + 1, "</sup>");
                nCarets--;
            } else if (!hasBracket && currentChar == '\n') {
                for (int j = 0; j < nCarets; j++) {
                    regexed.insert(i, "</sup>");
                    i += 6;
                }
                nCarets = 0;
            } else if (!hasBracket && Character.isWhitespace(currentChar)) {
                for (int j = 0; j < nCarets; j++) {
                    regexed.insert(i, "</sup>");
                    i += 6;
                }
                nCarets = 0;
            } else {
                new_lines = 0;
            }
        }
        if (!hasBracket) {
            for (int j = 0; j < nCarets; j++) {
                regexed.append("</sup>");
            }
        }

        return regexed.toString();
    }

    public static String parseInlineGifInComments(String markdown) {
        StringBuilder markdownStringBuilder = new StringBuilder(markdown);
        Pattern inlineGifPattern = REGEX_PATTERNS[9];
        Matcher matcher = inlineGifPattern.matcher(markdownStringBuilder);
        while (matcher.find()) {
            markdownStringBuilder.replace(matcher.start(), matcher.end(), "[gif](https://i.giphy.com/media/" + markdownStringBuilder.substring(matcher.start() + "![gif](giphy|".length(), matcher.end() - 1) + "/giphy.mp4)");
            matcher = inlineGifPattern.matcher(markdownStringBuilder);
        }

        Pattern inlineGifPattern2 = REGEX_PATTERNS[10];
        Matcher matcher2 = inlineGifPattern2.matcher(markdownStringBuilder);
        while (matcher2.find()) {
            markdownStringBuilder.replace(matcher2.start(), matcher2.end(), "[gif](https://i.giphy.com/media/" + markdownStringBuilder.substring(matcher2.start() + "![gif](giphy|".length(), matcher2.end() - "|downsized\\)".length() + 1) + "/giphy.mp4)");
            matcher2 = inlineGifPattern2.matcher(markdownStringBuilder);
        }

        Pattern inlineGifPattern3 = REGEX_PATTERNS[11];
        Matcher matcher3 = inlineGifPattern3.matcher(markdownStringBuilder);
        while (matcher3.find()) {
            markdownStringBuilder.replace(matcher3.start(), matcher3.end(),
                    "[gif](https://reddit-meta-production.s3.amazonaws.com/public/fortnitebr/emotes/snoomoji_emotes/"
                            + markdownStringBuilder.substring(
                            matcher3.start() + "![gif](emote|".length(), matcher3.end() - 1).replace('|', '/') + ".gif)");
            matcher3 = inlineGifPattern3.matcher(markdownStringBuilder);
        }

        return markdownStringBuilder.toString();
    }

    public static String parseInlineEmotes(String markdown, JSONObject mediaMetadataObject) throws JSONException {
        JSONArray mediaMetadataNames = mediaMetadataObject.names();
        for (int i = 0; i < mediaMetadataNames.length(); i++) {
            if (!mediaMetadataNames.isNull(i)) {
                String mediaMetadataKey = mediaMetadataNames.getString(i);
                if (mediaMetadataObject.isNull(mediaMetadataKey)) {
                    continue;
                }
                JSONObject item = mediaMetadataObject.getJSONObject(mediaMetadataKey);
                if (item.isNull(JSONUtils.STATUS_KEY)
                        || !item.getString(JSONUtils.STATUS_KEY).equals("valid")
                        || item.isNull(JSONUtils.ID_KEY)
                        || item.isNull(JSONUtils.T_KEY)
                        || item.isNull(JSONUtils.S_KEY)) {
                    continue;
                }
                String emote_type = item.getString(JSONUtils.T_KEY);
                String emote_id = item.getString(JSONUtils.ID_KEY);

                JSONObject s_key = item.getJSONObject(JSONUtils.S_KEY);
                if (s_key.isNull(JSONUtils.U_KEY)) {
                    continue;
                }
                String emote_url = s_key.getString(JSONUtils.U_KEY);

                markdown = markdown.replace("![img](" + emote_id + ")", "[" + emote_type + "](" + emote_url + ") ");
            }
        }
        return markdown;
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
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                return NETWORK_TYPE_OTHER;
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

                return NETWORK_TYPE_OTHER;
            }
        }

        return NETWORK_TYPE_OTHER;
    }

    public static boolean isConnectedToWifi(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    public static void hideKeyboard(Activity activity) {
        /*if (activity.getCurrentFocus() == null || !(activity.getCurrentFocus() instanceof EditText)) {
            editText.requestFocus();
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);*/

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
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable != null) {
            Drawable wrappedDrawable = DrawableCompat.wrap(drawable).mutate();
            DrawableCompat.setTint(wrappedDrawable, color);
            return wrappedDrawable;
        }

        return null;
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
                String imageUrlOrError = UploadImageUtils.uploadImage(oauthRetrofit, uploadMediaRetrofit, accessToken, bitmap);
                handler.post(() -> {
                    if (imageUrlOrError != null && !imageUrlOrError.startsWith("Error: ")) {
                        String fileName = Utils.getFileName(context, imageUri);
                        if (fileName == null) {
                            fileName = imageUrlOrError;
                        }
                        uploadedImages.add(new UploadedImage(fileName, imageUrlOrError));

                        int start = Math.max(editText.getSelectionStart(), 0);
                        int end = Math.max(editText.getSelectionEnd(), 0);
                        editText.getText().replace(Math.min(start, end), Math.max(start, end),
                                "[" + fileName + "](" + imageUrlOrError + ")",
                                0, "[]()".length() + fileName.length() + imageUrlOrError.length());
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
}
