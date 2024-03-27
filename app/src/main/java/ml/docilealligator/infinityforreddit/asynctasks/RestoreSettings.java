package ml.docilealligator.infinityforreddit.asynctasks;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.lingala.zip4j.ZipFile;

import org.apache.commons.io.file.PathUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private static final String TAG = RestoreSettings.class.getSimpleName();

    @SuppressWarnings("NewApi") // StandardOpenOption is desugared for all Android versions
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
                final var cachePath = Objects.requireNonNull(context.getExternalCacheDir())
                        .toPath().resolve("Restore");

                try (var zipFileInputStream = contentResolver.openInputStream(zipFileUri)) {
                    if (zipFileInputStream == null) {
                        handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_failed_cannot_get_file)));
                        return;
                    }

                    if (Files.exists(cachePath)) {
                        PathUtils.deleteDirectory(cachePath);
                    }
                    Files.createDirectory(cachePath);

                    final var zipCache = cachePath.resolve("restore.zip");
                    try (var zipCacheOutputStream = Files.newOutputStream(zipCache,
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.DELETE_ON_CLOSE)) {
                        zipFileInputStream.transferTo(zipCacheOutputStream);

                        try (var zipCacheFile = new ZipFile(zipCache.toFile(), "123321".toCharArray())) {
                            zipCacheFile.extractAll(cachePath.toString());
                        }
                    }
                }

                final Optional<Path> restoreFilesDir;
                try (var fileStream = Files.list(cachePath)) {
                    restoreFilesDir = fileStream.filter(Files::isDirectory).findFirst();
                }
                if (restoreFilesDir.isEmpty()) {
                    handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_failed_file_corrupted)));
                } else {
                    boolean result = true;

                    try (var restoreFilesStream = Files.newDirectoryStream(restoreFilesDir.get())) {
                        for (var path : restoreFilesStream) {
                            final var fileName = path.getFileName().toString();

                            if (Files.isRegularFile(path)) {
                                if (fileName.startsWith(SharedPreferencesUtils.DEFAULT_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(defaultSharedPreferences, path);
                                } else if (fileName.startsWith(CustomThemeSharedPreferencesUtils.LIGHT_THEME_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(lightThemeSharedPreferences, path);
                                } else if (fileName.startsWith(CustomThemeSharedPreferencesUtils.DARK_THEME_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(darkThemeSharedPreferences, path);
                                } else if (fileName.startsWith(CustomThemeSharedPreferencesUtils.AMOLED_THEME_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(amoledThemeSharedPreferences, path);
                                } else if (fileName.startsWith(SharedPreferencesUtils.SORT_TYPE_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(sortTypeSharedPreferences, path);
                                } else if (fileName.startsWith(SharedPreferencesUtils.POST_LAYOUT_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(postLayoutSharedPreferences, path);
                                } else if (fileName.startsWith(SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(postFeedScrolledPositionSharedPreferences, path);
                                } else if (fileName.startsWith(SharedPreferencesUtils.MAIN_PAGE_TABS_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(mainActivityTabsSharedPreferences, path);
                                } else if (fileName.startsWith(SharedPreferencesUtils.NSFW_AND_SPOILER_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(nsfwAndSpoilerSharedPreferencs, path);
                                } else if (fileName.startsWith(SharedPreferencesUtils.BOTTOM_APP_BAR_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(bottomAppBarSharedPreferences, path);
                                } else if (fileName.startsWith(SharedPreferencesUtils.POST_HISTORY_SHARED_PREFERENCES_FILE)) {
                                    result &= importSharedPreferencesFromFile(postHistorySharedPreferences, path);
                                }
                            } else if (Files.isDirectory(path) && fileName.equals("database")) {
                                if (!redditDataRoomDatabase.accountDao().isAnonymousAccountInserted()) {
                                    redditDataRoomDatabase.accountDao().insert(Account.getAnonymousAccount());
                                }

                                final var anonymousSubscribedSubredditsFile = path.resolve("anonymous_subscribed_subreddits.json");
                                final var anonymousSubscribedUsersFile = path.resolve("anonymous_subscribed_users.json");
                                final var anonymousMultiredditsFile = path.resolve("anonymous_multireddits.json");
                                final var anonymousMultiredditSubredditsFile = path.resolve("anonymous_multireddit_subreddits.json");
                                final var customThemesFile = path.resolve("custom_themes.json");
                                final var postFiltersFile = path.resolve("post_filters.json");
                                final var postFilterUsageFile = path.resolve("post_filter_usage.json");

                                if (Files.exists(anonymousSubscribedSubredditsFile)) {
                                    List<SubscribedSubredditData> anonymousSubscribedSubreddits = getListFromFile(anonymousSubscribedSubredditsFile, new TypeToken<List<SubscribedSubredditData>>() {}.getType());
                                    redditDataRoomDatabase.subscribedSubredditDao().insertAll(anonymousSubscribedSubreddits);
                                }
                                if (Files.exists(anonymousSubscribedUsersFile)) {
                                    List<SubscribedUserData> anonymousSubscribedUsers = getListFromFile(anonymousSubscribedUsersFile, new TypeToken<List<SubscribedUserData>>() {}.getType());
                                    redditDataRoomDatabase.subscribedUserDao().insertAll(anonymousSubscribedUsers);
                                }
                                if (Files.exists(anonymousMultiredditsFile)) {
                                    List<MultiReddit> anonymousMultireddits = getListFromFile(anonymousMultiredditsFile, new TypeToken<List<MultiReddit>>() {}.getType());
                                    redditDataRoomDatabase.multiRedditDao().insertAll(anonymousMultireddits);

                                    if (Files.exists(anonymousMultiredditSubredditsFile)) {
                                        List<AnonymousMultiredditSubreddit> anonymousMultiredditSubreddits = getListFromFile(anonymousMultiredditSubredditsFile, new TypeToken<List<AnonymousMultiredditSubreddit>>() {}.getType());
                                        redditDataRoomDatabase.anonymousMultiredditSubredditDao().insertAll(anonymousMultiredditSubreddits);
                                    }
                                }
                                if (Files.exists(customThemesFile)) {
                                    List<CustomTheme> customThemes = getListFromFile(customThemesFile, new TypeToken<List<CustomTheme>>() {}.getType());
                                    redditDataRoomDatabase.customThemeDao().insertAll(customThemes);
                                }
                                if (Files.exists(postFiltersFile)) {
                                    List<PostFilter> postFilters = getListFromFile(postFiltersFile, new TypeToken<List<PostFilter>>() {}.getType());
                                    redditDataRoomDatabase.postFilterDao().insertAll(postFilters);

                                    if (Files.exists(postFilterUsageFile)) {
                                        List<PostFilterUsage> postFilterUsage = getListFromFile(postFilterUsageFile, new TypeToken<List<PostFilterUsage>>() {}.getType());
                                        redditDataRoomDatabase.postFilterUsageDao().insertAll(postFilterUsage);
                                    }
                                }
                            }
                        }
                    }

                    PathUtils.deleteDirectory(cachePath);

                    if (result) {
                        handler.post(restoreSettingsListener::success);
                    } else {
                        handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_partially_failed)));
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error while restoring backup", e);

                handler.post(() -> restoreSettingsListener.failed(context.getString(R.string.restore_settings_partially_failed)));
            }
        });
    }

    private static boolean importSharedPreferencesFromFile(SharedPreferences sharedPreferences, Path path) {
        try (var input = new ObjectInputStream(Files.newInputStream(path))) {
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

                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable
    private static <T> List<T> getListFromFile(Path path, Type dataType) {
        try (var reader = new JsonReader(Files.newBufferedReader(path))) {
            var gson = new Gson();
            return gson.fromJson(reader, dataType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface RestoreSettingsListener {
        void success();
        void failed(String errorMessage);
    }
}
