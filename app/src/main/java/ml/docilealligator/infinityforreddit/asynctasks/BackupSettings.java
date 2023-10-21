package ml.docilealligator.infinityforreddit.asynctasks;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;

import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.apache.commons.io.file.PathUtils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.multireddit.AnonymousMultiredditSubreddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class BackupSettings {
    public static void backupSettings(Context context, Executor executor, Handler handler,
                                      ContentResolver contentResolver, Uri destinationDirUri,
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
                                      BackupSettingsListener backupSettingsListener) {
        executor.execute(() -> {
            final var backupRootDir = Objects.requireNonNull(context.getExternalCacheDir())
                    .toPath()
                    .resolve("Backup");
            final var backupDirPath = backupRootDir.resolve(BuildConfig.VERSION_NAME);
            final var databaseDirFile = backupDirPath.resolve("database");

            if (Files.exists(backupDirPath)) {
                try {
                    PathUtils.deleteDirectory(backupDirPath);
                    Files.createDirectories(backupDirPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            boolean res = saveSharedPreferencesToFile(defaultSharedPreferences, backupDirPath,
                    SharedPreferencesUtils.DEFAULT_PREFERENCES_FILE);
            boolean res1 = saveSharedPreferencesToFile(lightThemeSharedPreferences, backupDirPath,
                    CustomThemeSharedPreferencesUtils.LIGHT_THEME_SHARED_PREFERENCES_FILE);
            boolean res2 = saveSharedPreferencesToFile(darkThemeSharedPreferences, backupDirPath,
                    CustomThemeSharedPreferencesUtils.DARK_THEME_SHARED_PREFERENCES_FILE);
            boolean res3 = saveSharedPreferencesToFile(amoledThemeSharedPreferences, backupDirPath,
                    CustomThemeSharedPreferencesUtils.AMOLED_THEME_SHARED_PREFERENCES_FILE);
            boolean res4 = saveSharedPreferencesToFile(sortTypeSharedPreferences, backupDirPath,
                    SharedPreferencesUtils.SORT_TYPE_SHARED_PREFERENCES_FILE);
            boolean res5 = saveSharedPreferencesToFile(postLayoutSharedPreferences, backupDirPath,
                    SharedPreferencesUtils.POST_LAYOUT_SHARED_PREFERENCES_FILE);
            boolean res6 = saveSharedPreferencesToFile(postFeedScrolledPositionSharedPreferences, backupDirPath,
                    SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_SHARED_PREFERENCES_FILE);
            boolean res7 = saveSharedPreferencesToFile(mainActivityTabsSharedPreferences, backupDirPath,
                    SharedPreferencesUtils.MAIN_PAGE_TABS_SHARED_PREFERENCES_FILE);
            boolean res8 = saveSharedPreferencesToFile(nsfwAndSpoilerSharedPreferencs, backupDirPath,
                    SharedPreferencesUtils.NSFW_AND_SPOILER_SHARED_PREFERENCES_FILE);
            boolean res9 = saveSharedPreferencesToFile(bottomAppBarSharedPreferences, backupDirPath,
                    SharedPreferencesUtils.BOTTOM_APP_BAR_SHARED_PREFERENCES_FILE);
            boolean res10 = saveSharedPreferencesToFile(postHistorySharedPreferences, backupDirPath,
                    SharedPreferencesUtils.POST_HISTORY_SHARED_PREFERENCES_FILE);

            List<SubscribedSubredditData> anonymousSubscribedSubredditsData = redditDataRoomDatabase.subscribedSubredditDao().getAllSubscribedSubredditsList("-");
            String anonymousSubscribedSubredditsDataJson = new Gson().toJson(anonymousSubscribedSubredditsData);
            boolean res11 = saveDatabaseTableToFile(anonymousSubscribedSubredditsDataJson, databaseDirFile, "anonymous_subscribed_subreddits.json");

            List<SubscribedUserData> anonymousSubscribedUsersData = redditDataRoomDatabase.subscribedUserDao().getAllSubscribedUsersList("-");
            String anonymousSubscribedUsersDataJson = new Gson().toJson(anonymousSubscribedUsersData);
            boolean res12 = saveDatabaseTableToFile(anonymousSubscribedUsersDataJson, databaseDirFile, "anonymous_subscribed_users.json");

            List<MultiReddit> anonymousMultireddits = redditDataRoomDatabase.multiRedditDao().getAllMultiRedditsList("-");
            String anonymousMultiredditsJson = new Gson().toJson(anonymousMultireddits);
            boolean res13 = saveDatabaseTableToFile(anonymousMultiredditsJson, databaseDirFile, "anonymous_multireddits.json");

            List<AnonymousMultiredditSubreddit> anonymousMultiredditSubreddits = redditDataRoomDatabase.anonymousMultiredditSubredditDao().getAllSubreddits();
            String anonymousMultiredditSubredditsJson = new Gson().toJson(anonymousMultiredditSubreddits);
            boolean res14 = saveDatabaseTableToFile(anonymousMultiredditSubredditsJson, databaseDirFile, "anonymous_multireddit_subreddits.json");

            List<CustomTheme> customThemes = redditDataRoomDatabase.customThemeDao().getAllCustomThemesList();
            String customThemesJson = new Gson().toJson(customThemes);
            boolean res15 = saveDatabaseTableToFile(customThemesJson, databaseDirFile, "custom_themes.json");

            List<PostFilter> postFilters = redditDataRoomDatabase.postFilterDao().getAllPostFilters();
            String postFiltersJson = new Gson().toJson(postFilters);
            boolean res16 = saveDatabaseTableToFile(postFiltersJson, databaseDirFile, "post_filters.json");

            List<PostFilterUsage> postFilterUsage = redditDataRoomDatabase.postFilterUsageDao().getAllPostFilterUsageForBackup();
            String postFilterUsageJson = new Gson().toJson(postFilterUsage);
            boolean res17 = saveDatabaseTableToFile(postFilterUsageJson, databaseDirFile, "post_filter_usage.json");

            boolean zipRes = zipAndMoveToDestinationDir(context, contentResolver, destinationDirUri);

            try {
                PathUtils.deleteDirectory(backupRootDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                boolean finalResult = res && res1 && res2 && res3 && res4 && res5 && res6 && res7 && res8
                        && res9 && res10 && res11 && res12 && res13 && res14 && res15 && res16 && res17 && zipRes;
                if (finalResult) {
                    backupSettingsListener.success();
                } else {
                    if (!zipRes) {
                        backupSettingsListener.failed(context.getText(R.string.create_zip_in_destination_directory_failed).toString());
                    } else {
                        backupSettingsListener.failed(context.getText(R.string.backup_some_settings_failed).toString());
                    }
                }
            });
        });
    }

    private static boolean saveSharedPreferencesToFile(SharedPreferences sharedPreferences,
                                                       Path backupDir, String fileName) {
        final var backupFile = backupDir.resolve(fileName + ".txt");
        try (var output = new ObjectOutputStream(Files.newOutputStream(backupFile))) {
            output.writeObject(sharedPreferences.getAll());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveDatabaseTableToFile(String dataJson, Path backupDir, String fileName) {
        try (var out = Files.newBufferedWriter(backupDir.resolve(fileName))) {
            out.write(dataJson);
            out.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean zipAndMoveToDestinationDir(Context context, ContentResolver contentResolver, Uri destinationDirUri) {
        try {
            String time = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(System.currentTimeMillis()));
            String fileName = "Infinity_For_Reddit_Settings_Backup_v" + BuildConfig.VERSION_NAME + "-" + BuildConfig.VERSION_CODE + "-" + time + ".zip";

            final var backupRoot = Objects.requireNonNull(context.getExternalCacheDir())
                    .toPath().resolve("Backup");
            final var filePath = backupRoot.resolve(fileName);

            ZipFile zip = new ZipFile(filePath.toFile(), "123321".toCharArray());
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            zip.addFolder(backupRoot.resolve(BuildConfig.VERSION_NAME).toFile(), zipParameters);

            DocumentFile dir = DocumentFile.fromTreeUri(context, destinationDirUri);
            if (dir == null) {
                return false;
            }
            DocumentFile checkForDuplicate = dir.findFile(fileName);
            if (checkForDuplicate != null) {
                checkForDuplicate.delete();
            }
            DocumentFile destinationFile = dir.createFile("application/zip", fileName);
            if (destinationFile == null) {
                return false;
            }

            try (var outputStream = contentResolver.openOutputStream(destinationFile.getUri())) {
                if (outputStream == null) {
                    return false;
                }
                try (var inputStream = Files.newInputStream(filePath)) {
                    inputStream.transferTo(outputStream);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface BackupSettingsListener {
        void success();
        void failed(String errorMessage);
    }
}
