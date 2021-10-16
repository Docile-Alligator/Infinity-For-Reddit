package ml.docilealligator.infinityforreddit.asynctasks;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.lingala.zip4j.ZipFile;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.multireddit.AnonymousMultiredditSubreddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class RestoreSettings {
    public static void restoreSettings(Context context, Executor executor, Handler handler,
                                ContentResolver contentResolver, Uri zipFileUri,
                                RedditDataRoomDatabase redditDataRoomDatabase,
                                SharedPreferences defaultSharedPreferences,
                                SharedPreferences lightThemeSharedPreferences,
                                SharedPreferences darkThemeSharedPreferences,
                                SharedPreferences amoledThemeSharedPreferences,
                                SharedPreferences sortTypeSharedPreferences,
                                SharedPreferences postLayoutSharedPreferences,
                                SharedPreferences postFeedScrolledPositionSharedPreferences,
                                SharedPreferences mainActivityTabsSharedPreferences,
                                SharedPreferences nsfwAndSpoilerSharedPreferencs,
                                SharedPreferences bottomAppBarSharedPreferences,
                                SharedPreferences postHistorySharedPreferences,
                                RestoreSettingsListener restoreSettingsListener) {
        executor.execute(() -> {
            try {
                InputStream zipFileInputStream = contentResolver.openInputStream(zipFileUri);
                if (zipFileInputStream == null) {
                    handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_failed_cannot_get_file)));
                    return;
                }

                String cachePath = context.getExternalCacheDir() + "/Restore/";
                if (new File(cachePath).exists()) {
                    FileUtils.deleteDirectory(new File(cachePath));
                }
                new File(cachePath).mkdir();
                FileOutputStream zipCacheOutputStream = new FileOutputStream(new File(cachePath + "restore.zip"));

                byte[] fileReader = new byte[1024];

                while (true) {
                    int read = zipFileInputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    zipCacheOutputStream.write(fileReader, 0, read);
                }

                new ZipFile(cachePath + "restore.zip", "123321".toCharArray()).extractAll(cachePath);
                new File(cachePath + "restore.zip").delete();
                File[] files = new File(cachePath).listFiles();
                if (files == null || files.length <= 0) {
                    handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_failed_file_corrupted)));
                } else {
                    File restoreFilesDir = files[0];
                    File[] restoreFiles = restoreFilesDir.listFiles();
                    boolean result = true;
                    if (restoreFiles != null) {
                        for (File f : restoreFiles) {
                            if (f.isFile()) {
                                if (f.getName().startsWith(SharedPreferencesUtils.DEFAULT_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(defaultSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(CustomThemeSharedPreferencesUtils.LIGHT_THEME_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(lightThemeSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(CustomThemeSharedPreferencesUtils.DARK_THEME_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(darkThemeSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(CustomThemeSharedPreferencesUtils.AMOLED_THEME_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(amoledThemeSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(SharedPreferencesUtils.SORT_TYPE_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(sortTypeSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(SharedPreferencesUtils.POST_LAYOUT_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(postLayoutSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(postFeedScrolledPositionSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(SharedPreferencesUtils.MAIN_PAGE_TABS_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(mainActivityTabsSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(SharedPreferencesUtils.NSFW_AND_SPOILER_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(nsfwAndSpoilerSharedPreferencs, f.toString());
                                } else if (f.getName().startsWith(SharedPreferencesUtils.BOTTOM_APP_BAR_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(bottomAppBarSharedPreferences, f.toString());
                                } else if (f.getName().startsWith(SharedPreferencesUtils.POST_HISTORY_SHARED_PREFERENCES_FILE)) {
                                    result = result & importSharedPreferencsFromFile(postHistorySharedPreferences, f.toString());
                                }
                            } else if (f.isDirectory() && f.getName().equals("database")) {
                                if (!redditDataRoomDatabase.accountDao().isAnonymousAccountInserted()) {
                                    redditDataRoomDatabase.accountDao().insert(Account.getAnonymousAccount());
                                }

                                File anonymousSubscribedSubredditsFile = new File(f.getAbsolutePath() + "/anonymous_subscribed_subreddits.json");
                                File anonymousSubscribedUsersFile = new File(f.getAbsolutePath() + "/anonymous_subscribed_users.json");
                                File anonymousMultiredditsFile = new File(f.getAbsolutePath() + "/anonymous_multireddits.json");
                                File anonymousMultiredditSubredditsFile = new File(f.getAbsolutePath() + "/anonymous_multireddit_subreddits.json");
                                File customThemesFile = new File(f.getAbsolutePath() + "/custom_themes.json");
                                File postFiltersFile = new File(f.getAbsolutePath() + "/post_filters.json");
                                File postFilterUsageFile = new File(f.getAbsolutePath() + "/post_filter_usage.json");

                                if (anonymousSubscribedSubredditsFile.exists()) {
                                    List<SubscribedSubredditData> anonymousSubscribedSubreddits = getListFromFile(anonymousSubscribedSubredditsFile, new TypeToken<List<SubscribedSubredditData>>() {}.getType());
                                    redditDataRoomDatabase.subscribedSubredditDao().insertAll(anonymousSubscribedSubreddits);
                                }
                                if (anonymousSubscribedUsersFile.exists()) {
                                    List<SubscribedUserData> anonymousSubscribedUsers = getListFromFile(anonymousSubscribedUsersFile, new TypeToken<List<SubscribedUserData>>() {}.getType());
                                    redditDataRoomDatabase.subscribedUserDao().insertAll(anonymousSubscribedUsers);
                                }
                                if (anonymousMultiredditsFile.exists()) {
                                    List<MultiReddit> anonymousMultireddits = getListFromFile(anonymousMultiredditsFile, new TypeToken<List<MultiReddit>>() {}.getType());
                                    redditDataRoomDatabase.multiRedditDao().insertAll(anonymousMultireddits);

                                    if (anonymousMultiredditSubredditsFile.exists()) {
                                        List<AnonymousMultiredditSubreddit> anonymousMultiredditSubreddits = getListFromFile(anonymousMultiredditSubredditsFile, new TypeToken<List<AnonymousMultiredditSubreddit>>() {}.getType());
                                        redditDataRoomDatabase.anonymousMultiredditSubredditDao().insertAll(anonymousMultiredditSubreddits);
                                    }
                                }
                                if (customThemesFile.exists()) {
                                    List<CustomTheme> customThemes = getListFromFile(customThemesFile, new TypeToken<List<CustomTheme>>() {}.getType());
                                    redditDataRoomDatabase.customThemeDao().insertAll(customThemes);
                                }
                                if (postFiltersFile.exists()) {
                                    List<PostFilter> postFilters = getListFromFile(postFiltersFile, new TypeToken<List<PostFilter>>() {}.getType());
                                    redditDataRoomDatabase.postFilterDao().insertAll(postFilters);

                                    if (postFilterUsageFile.exists()) {
                                        List<PostFilterUsage> postFilterUsage = getListFromFile(postFilterUsageFile, new TypeToken<List<PostFilterUsage>>() {}.getType());
                                        redditDataRoomDatabase.postFilterUsageDao().insertAll(postFilterUsage);
                                    }
                                }
                            }
                        }
                    } else {
                        handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_failed_file_corrupted)));
                    }

                    FileUtils.deleteDirectory(new File(cachePath));

                    if (result) {
                        handler.post(restoreSettingsListener::success);
                    } else {
                        handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_partially_failed)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

                handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_partially_failed)));
            }
        });
    }

    private static boolean importSharedPreferencsFromFile(SharedPreferences sharedPreferences, String uriString) {
        boolean result = false;
        ObjectInputStream input = null;

        try {
            input = new ObjectInputStream(new FileInputStream(uriString));
            Object object = input.readObject();
            if (object instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) object;
                Set<Map.Entry<String, Object>> entrySet = map.entrySet();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (Map.Entry<String, Object> e : entrySet) {
                    if (e.getValue() instanceof String) {
                        editor.putString(e.getKey(), (String) e.getValue());
                    } else if (e.getValue() instanceof Integer) {
                        editor.putInt(e.getKey(), (Integer) e.getValue());
                    } else if (e.getValue() instanceof Float) {
                        editor.putFloat(e.getKey(), (Float) e.getValue());
                    } else if (e.getValue() instanceof Boolean) {
                        editor.putBoolean(e.getKey(), (Boolean) e.getValue());
                    } else if (e.getValue() instanceof Long) {
                        editor.putLong(e.getKey(), (Long) e.getValue());
                    }
                }

                editor.apply();

                result = true;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    @Nullable
    private static <T> List<T> getListFromFile(File file, Type dataType) {
        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            Gson gson = new Gson();
            return gson.fromJson(reader, dataType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public interface RestoreSettingsListener {
        void success();
        void failed(String errorMessage);
    }
}
