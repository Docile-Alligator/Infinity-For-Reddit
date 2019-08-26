package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

public class LinkResolverActivity extends AppCompatActivity {

    static final String EXTRA_MESSAGE_FULLNAME = "ENF";
    static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";

    private static final String POST_PATTERN = "/r/\\w+/comments/\\w+/{0,1}\\w+/{0,1}";
    private static final String COMMENT_PATTERN = "/r/\\w+/comments/\\w+/{0,1}\\w+/\\w+/{0,1}";
    private static final String SUBREDDIT_PATTERN = "/r/\\w+/*";
    private static final String USER_PATTERN_1 = "/user/\\w+/*";
    private static final String USER_PATTERN_2 = "/u/\\w+/*";

    @Inject
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int themeType = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        switch (themeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                break;
            case 2:
                if(systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                }

        }

        Uri uri = getIntent().getData();
        if(uri == null) {
            Toast.makeText(this, R.string.no_link_available, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String path = uri.getPath();
            if(path == null) {
                deepLinkError(uri);
            } else {
                if(path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }

                String messageFullname = getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME);
                String newAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);

                if(path.matches(POST_PATTERN)) {
                    List<String> segments = uri.getPathSegments();
                    int commentsIndex = segments.lastIndexOf("comments");
                    if(commentsIndex >=0 && commentsIndex < segments.size() - 1) {
                        Intent intent = new Intent(this, ViewPostDetailActivity.class);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, segments.get(commentsIndex + 1));
                        intent.putExtra(ViewPostDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                        startActivity(intent);
                    } else {
                        deepLinkError(uri);
                    }
                } else if(path.matches(COMMENT_PATTERN)) {
                    List<String> segments = uri.getPathSegments();
                    int commentsIndex = segments.lastIndexOf("comments");
                    if(commentsIndex >=0 && commentsIndex < segments.size() - 1) {
                        Intent intent = new Intent(this, ViewPostDetailActivity.class);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, segments.get(commentsIndex + 1));
                        intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, segments.get(segments.size() - 1));
                        intent.putExtra(ViewPostDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                        startActivity(intent);
                    } else {
                        deepLinkError(uri);
                    }
                } else if(path.matches(SUBREDDIT_PATTERN)) {
                    String subredditName = path.substring(3);
                    if(subredditName.equals("popular") || subredditName.equals("all")) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra(MainActivity.EXTRA_POST_TYPE, subredditName);
                        intent.putExtra(MainActivity.EXTRA_MESSSAGE_FULLNAME, messageFullname);
                        intent.putExtra(MainActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, ViewSubredditDetailActivity.class);
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, path.substring(3));
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                        startActivity(intent);
                    }
                } else if(path.matches(USER_PATTERN_1)) {
                    Intent intent = new Intent(this, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, path.substring(6));
                    intent.putExtra(ViewUserDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                    startActivity(intent);
                } else if(path.matches(USER_PATTERN_2)) {
                    Intent intent = new Intent(this, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, path.substring(3));
                    intent.putExtra(ViewUserDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                    startActivity(intent);
                } else {
                    deepLinkError(uri);
                }
            }

            finish();
        }
    }

    private void deepLinkError(Uri uri) {
        PackageManager pm = getPackageManager();
        ArrayList<ResolveInfo> resolveInfos = getCustomTabsPackages(pm);
        if(!resolveInfos.isEmpty()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            // add share action to menu list
            builder.addDefaultShareMenuItem();
            builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(resolveInfos.get(0).activityInfo.packageName);
            if(uri.getScheme() == null) {
                uri = Uri.parse("http://" + uri.toString());
            }
            customTabsIntent.launchUrl(this, uri);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);

            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            ArrayList<String> packageNames = new ArrayList<>();

            String currentPackageName = getApplicationContext().getPackageName();

            for(ResolveInfo info : activities) {
                if(!info.activityInfo.packageName.equals(currentPackageName)) {
                    packageNames.add(info.activityInfo.packageName);
                }
            }

            if(!packageNames.isEmpty()) {
                intent.setPackage(packageNames.get(0));
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.no_browser_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ArrayList<ResolveInfo> getCustomTabsPackages(PackageManager pm) {
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"));

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        ArrayList<ResolveInfo> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            // Check if this package also resolves the Custom Tabs service.
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info);
            }
        }
        return packagesSupportingCustomTabs;
    }

    public static Uri getRedditUriByPath(String path) {
        return Uri.parse("https://www.reddit.com" + path);
    }
}
