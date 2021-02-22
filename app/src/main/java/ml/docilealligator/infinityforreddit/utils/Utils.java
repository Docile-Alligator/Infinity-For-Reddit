package ml.docilealligator.infinityforreddit.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.HtmlCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SortType;

public class Utils {
    public static final int NETWORK_TYPE_OTHER = -1;
    public static final int NETWORK_TYPE_WIFI = 0;
    public static final int NETWORK_TYPE_CELLULAR = 1;
    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    private static final long YEAR_MILLIS = 12 * MONTH_MILLIS;

    public static String modifyMarkdown(String markdown) {
        StringBuilder regexed = new StringBuilder(markdown
                .replaceAll("((?<=[\\s])|^)/[rRuU]/[\\w-]+/{0,1}", "[$0](https://www.reddit.com$0)")
                .replaceAll("((?<=[\\s])|^)[rRuU]/[\\w-]+/{0,1}", "[$0](https://www.reddit.com/$0)")
                .replaceAll("\\^{2,}", "^")
                .replaceAll("(^|^ *|\\n *)#(?!($|\\s|#))", "$0 ")
                .replaceAll("(^|^ *|\\n *)##(?!($|\\s|#))", "$0 ")
                .replaceAll("(^|^ *|\\n *)###(?!($|\\s|#))", "$0 ")
                .replaceAll("(^|^ *|\\n *)####(?!($|\\s|#))", "$0 ")
                .replaceAll("(^|^ *|\\n *)#####(?!($|\\s|#))", "$0 ")
                .replaceAll("(^|^ *|\\n *)######(?!($|\\s|#))", "$0 "));

        return fixSuperScript(regexed);
        //Fix superscript
        /*int startIndex = regexed.indexOf("^");
        while (startIndex >= 0 && startIndex + 1 < regexed.length()) {
            char currentChar = regexed.charAt(startIndex + 1);
            if (currentChar == '^') {
                regexed.insert(startIndex, '^');
                startIndex = regexed.indexOf("^", startIndex + 1);
            } else if (currentChar == ' ' || currentChar == '\n') {
                regexed.insert(startIndex + 1, '^');
                startIndex = regexed.indexOf("^", startIndex + 2);
            } else if (currentChar == '(') {
                int closeBracketIndex = regexed.indexOf(")", startIndex + 2);
                if (closeBracketIndex > 0) {

                }
            } else {
                if (startIndex + 1 == regexed.length() - 1) {
                    regexed.append('^');
                    startIndex++;
                }
                startIndex++;
            }
        }
        return regexed.toString();*/
    }

    public static String fixSuperScript(StringBuilder regexed) {
        int newestCaretIndex = regexed.indexOf("^");
        if (newestCaretIndex >= 0) {
            boolean hasBracket = false;
            int caretWithLeftBracketIndex = -1;
            for (int i = newestCaretIndex + 1; i < regexed.length(); i++) {
                char currentChar = regexed.charAt(i);
                if (currentChar == '^') {
                    if (!(i > 0 && regexed.charAt(i - 1) == '\\')) {
                        if (newestCaretIndex < 0) {
                            newestCaretIndex = i;
                        } else {
                            regexed.insert(i, '^');
                            newestCaretIndex = i + 1;
                            i++;
                            if (newestCaretIndex == regexed.length() - 1) {
                                regexed.deleteCharAt(regexed.length() - 1);
                                newestCaretIndex = -1;
                                break;
                            }
                        }
                    }
                } else if (currentChar == ' ' || currentChar == '\n') {
                    if (newestCaretIndex >= 0) {
                        if (i != newestCaretIndex + 1) {
                            regexed.insert(i, '^');
                            newestCaretIndex = -1;
                            i++;
                        } else {
                            newestCaretIndex = -1;
                        }
                    }
                } else if (currentChar == '(') {
                    if (newestCaretIndex >= 0 && i == newestCaretIndex + 1) {
                        hasBracket = true;
                        newestCaretIndex = -1;
                        caretWithLeftBracketIndex = i - 1;
                    }
                } else if (currentChar == ')') {
                    if (hasBracket) {
                        hasBracket = false;
                        regexed.setCharAt(i, '^');
                        regexed.replace(caretWithLeftBracketIndex, caretWithLeftBracketIndex + 2, "^");
                        caretWithLeftBracketIndex = -1;
                        i--;
                    }
                }
            }
            if (newestCaretIndex >=0 || caretWithLeftBracketIndex >= 0) {
                regexed.insert(regexed.length(), '^');
            }
        }

        return regexed.toString();
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        do {
            i--;
        } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        return source.subSequence(0, i+1);
    }

    public static String getFormattedTime(Locale locale, long time, String pattern) {
        Calendar postTimeCalendar = Calendar.getInstance();
        postTimeCalendar.setTimeInMillis(time);
        return new SimpleDateFormat(pattern, locale).format(postTimeCalendar.getTime());
    }

    public static String getElapsedTime(Context context,  long time) {
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
                NetworkCapabilities actNw = connMgr.getNetworkCapabilities(nw);
                if (actNw != null) {
                    if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return NETWORK_TYPE_WIFI;
                    }
                    if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return NETWORK_TYPE_CELLULAR;
                    }
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
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Nullable
    public static Drawable getTintedDrawable(Context context, int drawableId, int color) {
        Drawable drawable = context.getDrawable(drawableId);
        if (drawable != null) {
            Drawable wrappedDrawable = DrawableCompat.wrap(drawable).mutate();
            DrawableCompat.setTint(wrappedDrawable, color);
            return wrappedDrawable;
        }

        return null;
    }
}
