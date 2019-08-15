package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.ArrayList;
import java.util.List;

import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

public class LinkResolverActivity extends AppCompatActivity {

    private static final String POST_PATTERN = "/r/\\w+/comments/\\w+/{0,1}\\w+/{0,1}";
    private static final String COMMENT_PATTERN = "/r/\\w+/comments/\\w+/{0,1}\\w+/\\w+/{0,1}";
    private static final String SUBREDDIT_PATTERN = "/r/\\w+/*";
    private static final String USER_PATTERN = "/user/\\w+/*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        String path = uri.getPath();
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if(path.matches(POST_PATTERN)) {
            List<String> segments = uri.getPathSegments();
            int commentsIndex = segments.lastIndexOf("comments");
            if(commentsIndex >=0 && commentsIndex < segments.size() - 1) {
                Intent intent = new Intent(this, ViewPostDetailActivity.class);
                intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, segments.get(commentsIndex + 1));
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
                startActivity(intent);
            } else {
                deepLinkError(uri);
            }
        } else if(path.matches(SUBREDDIT_PATTERN)) {
            String subredditName = path.substring(3);
            if(subredditName.equals("popular") || subredditName.equals("all")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_POST_TYPE, subredditName);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, path.substring(3));
                startActivity(intent);
            }
        } else if(path.matches(USER_PATTERN)) {
            Intent intent = new Intent(this, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, path.substring(6));
            startActivity(intent);
        } else {
            deepLinkError(uri);
        }

        finish();
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
            String uriString = uri.toString();
            if(!uriString.startsWith("http://") || (!uriString.startsWith("https://"))) {
                uriString = "http://" + uriString;
            }
            customTabsIntent.launchUrl(this, Uri.parse(uriString));
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
}
