package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class LinkResolverActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE_FULLNAME = "ENF";
    public static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";
    public static final String EXTRA_IS_NSFW = "EIN";

    private static final String POST_PATTERN = "/r/\\w+/comments/\\w+/?\\w+/?";
    private static final String COMMENT_PATTERN = "/(r|u|U|user)/\\w+/comments/\\w+/?\\w+/\\w+/?";
    private static final String SUBREDDIT_PATTERN = "/[rR]/[\\w-]+/?";
    private static final String USER_PATTERN = "/(u|U|user)/[\\w-]+/?";
    private static final String SIDEBAR_PATTERN = "/[rR]/\\w+/about/sidebar";
    private static final String MULTIREDDIT_PATTERN = "/user/\\w+/m/\\w+/?";
    private static final String MULTIREDDIT_PATTERN_2 = "/[rR]/(\\w+\\+?)+/?";
    private static final String REDD_IT_POST_PATTERN = "/\\w+/?";
    private static final String GFYCAT_PATTERN = "(/ifr)?/[\\w-]+$";
    private static final String REDGIFS_PATTERN = "/watch/[\\w-]+$";
    private static final String IMGUR_GALLERY_PATTERN = "/gallery/\\w+/?";
    private static final String IMGUR_ALBUM_PATTERN = "/(album|a)/\\w+/?";
    private static final String IMGUR_IMAGE_PATTERN = "/\\w+/?";

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    private Uri getRedditUriByPath(String path) {
        return Uri.parse("https://www.reddit.com" + path);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        Uri uri = getIntent().getData();

        if (uri.getScheme() == null && uri.getHost() == null) {
            handleUri(getRedditUriByPath(uri.toString()));
        } else {
            handleUri(uri);
        }
    }

    private void handleUri(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, R.string.no_link_available, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String path = uri.getPath();
            if (path == null) {
                deepLinkError(uri);
            } else {
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }

                if (path.endsWith("jpg") || path.endsWith("png")) {
                    Intent intent = new Intent(this, ViewImageOrGifActivity.class);
                    String url = uri.toString();
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, url);
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, fileName);
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, fileName);
                    startActivity(intent);
                } else if (path.endsWith("gif")) {
                    Intent intent = new Intent(this, ViewImageOrGifActivity.class);
                    String url = uri.toString();
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, url);
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, fileName);
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, fileName);
                    startActivity(intent);
                } else if (path.endsWith("mp4")) {
                    Intent intent = new Intent(this, ViewVideoActivity.class);
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_DIRECT);
                    intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, getIntent().getBooleanExtra(EXTRA_IS_NSFW, false));
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    String messageFullname = getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME);
                    String newAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);

                    String authority = uri.getAuthority();
                    List<String> segments = uri.getPathSegments();

                    if (authority != null) {
                        if (authority.contains("reddit.com") || authority.contains("redd.it") || authority.contains("reddit.app")) {
                            if (authority.equals("reddit.app.link") && path.isEmpty()) {
                                String redirect = uri.getQueryParameter("$og_redirect");
                                handleUri(Uri.parse(redirect));
                            } else if (path.isEmpty()) {
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                            } else if (path.matches(POST_PATTERN)) {
                                int commentsIndex = segments.lastIndexOf("comments");
                                if (commentsIndex >= 0 && commentsIndex < segments.size() - 1) {
                                    Intent intent = new Intent(this, ViewPostDetailActivity.class);
                                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, segments.get(commentsIndex + 1));
                                    intent.putExtra(ViewPostDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                                    intent.putExtra(ViewPostDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                                    startActivity(intent);
                                } else {
                                    deepLinkError(uri);
                                }
                            } else if (path.matches(COMMENT_PATTERN)) {
                                int commentsIndex = segments.lastIndexOf("comments");
                                if (commentsIndex >= 0 && commentsIndex < segments.size() - 1) {
                                    Intent intent = new Intent(this, ViewPostDetailActivity.class);
                                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, segments.get(commentsIndex + 1));
                                    intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, segments.get(segments.size() - 1));
                                    intent.putExtra(ViewPostDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                                    intent.putExtra(ViewPostDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                                    startActivity(intent);
                                } else {
                                    deepLinkError(uri);
                                }
                            } else if (path.matches(SUBREDDIT_PATTERN)) {
                                Intent intent = new Intent(this, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, path.substring(3));
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                                startActivity(intent);
                            } else if (path.matches(USER_PATTERN)) {
                                Intent intent = new Intent(this, ViewUserDetailActivity.class);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, segments.get(1));
                                intent.putExtra(ViewUserDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                                startActivity(intent);
                            } else if (path.matches(SIDEBAR_PATTERN)) {
                                Intent intent = new Intent(this, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, path.substring(3, path.length() - 14));
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_VIEW_SIDEBAR, true);
                                startActivity(intent);
                            } else if (path.matches(MULTIREDDIT_PATTERN)) {
                                Intent intent = new Intent(this, ViewMultiRedditDetailActivity.class);
                                intent.putExtra(ViewMultiRedditDetailActivity.EXTRA_MULTIREDDIT_PATH, path);
                                startActivity(intent);
                            } else if (path.matches(MULTIREDDIT_PATTERN_2)) {
                                String subredditName = path.substring(3);
                                Intent intent = new Intent(this, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_MESSAGE_FULLNAME, messageFullname);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_NEW_ACCOUNT_NAME, newAccountName);
                                startActivity(intent);
                            } else if (authority.equals("redd.it") && path.matches(REDD_IT_POST_PATTERN)) {
                                Intent intent = new Intent(this, ViewPostDetailActivity.class);
                                intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, path.substring(1));
                                startActivity(intent);
                            } else {
                                deepLinkError(uri);
                            }
                        } else if (authority.contains("gfycat.com")) {
                            if (path.matches(GFYCAT_PATTERN)) {
                                Intent intent = new Intent(this, ViewVideoActivity.class);
                                intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, path.substring(path.lastIndexOf("/") + 1));
                                intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_GFYCAT);
                                intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, getIntent().getBooleanExtra(EXTRA_IS_NSFW, false));
                                startActivity(intent);
                            } else {
                                deepLinkError(uri);
                            }
                        } else if (authority.contains("redgifs.com")) {
                            if (path.matches(REDGIFS_PATTERN)) {
                                Intent intent = new Intent(this, ViewVideoActivity.class);
                                intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, path.substring(path.lastIndexOf("/") + 1));
                                intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_REDGIFS);
                                intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, true);
                                startActivity(intent);
                            } else {
                                deepLinkError(uri);
                            }
                        } else if (authority.contains("imgur.com")) {
                            if (path.matches(IMGUR_GALLERY_PATTERN)) {
                                Intent intent = new Intent(this, ViewImgurMediaActivity.class);
                                intent.putExtra(ViewImgurMediaActivity.EXTRA_IMGUR_TYPE, ViewImgurMediaActivity.IMGUR_TYPE_GALLERY);
                                intent.putExtra(ViewImgurMediaActivity.EXTRA_IMGUR_ID, segments.get(1));
                                startActivity(intent);
                            } else if (path.matches(IMGUR_ALBUM_PATTERN)) {
                                Intent intent = new Intent(this, ViewImgurMediaActivity.class);
                                intent.putExtra(ViewImgurMediaActivity.EXTRA_IMGUR_TYPE, ViewImgurMediaActivity.IMGUR_TYPE_ALBUM);
                                intent.putExtra(ViewImgurMediaActivity.EXTRA_IMGUR_ID, segments.get(1));
                                startActivity(intent);
                            } else if (path.matches(IMGUR_IMAGE_PATTERN)) {
                                Intent intent = new Intent(this, ViewImgurMediaActivity.class);
                                intent.putExtra(ViewImgurMediaActivity.EXTRA_IMGUR_TYPE, ViewImgurMediaActivity.IMGUR_TYPE_IMAGE);
                                intent.putExtra(ViewImgurMediaActivity.EXTRA_IMGUR_ID, path.substring(1));
                                startActivity(intent);
                            } else if (path.endsWith("gifv")) {
                                String url = uri.toString();
                                url = url.substring(0, url.length() - 5) + ".mp4";
                                Intent intent = new Intent(this, ViewVideoActivity.class);
                                intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_DIRECT);
                                intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, getIntent().getBooleanExtra(EXTRA_IS_NSFW, false));
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                            } else {
                                deepLinkError(uri);
                            }
                        } else {
                            deepLinkError(uri);
                        }
                    } else {
                        deepLinkError(uri);
                    }
                }
            }

            finish();
        }
    }

    private void deepLinkError(Uri uri) {
        PackageManager pm = getPackageManager();

        String authority = uri.getAuthority();
        if(authority != null && (authority.contains("reddit.com") || authority.contains("redd.it") || authority.contains("reddit.app.link"))) {
            openInCustomTabs(uri, pm, false);
            return;
        }

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.OPEN_LINK_IN_APP, false)) {
            openInCustomTabs(uri, pm, true);
        } else {
            openInBrowser(uri, pm, true);
        }
    }

    private void openInBrowser(Uri uri, PackageManager pm, boolean handleError) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            if (handleError) {
                openInCustomTabs(uri, pm, false);
            } else {
                openInWebView(uri);
            }
        }
    }

    private ArrayList<ResolveInfo> getCustomTabsPackages(PackageManager pm) {
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.fromParts("http", "", null));

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        ArrayList<ResolveInfo> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            // Check if this package also resolves the Custom Tabs service.
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info);
            }
        }
        return packagesSupportingCustomTabs;
    }

    private void openInCustomTabs(Uri uri, PackageManager pm, boolean handleError) {
        ArrayList<ResolveInfo> resolveInfos = getCustomTabsPackages(pm);
        if (!resolveInfos.isEmpty()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            // add share action to menu list
            builder.setShareState(CustomTabsIntent.SHARE_STATE_ON);
            builder.setDefaultColorSchemeParams(
                    new CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(mCustomThemeWrapper.getColorPrimary())
                            .build());
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(resolveInfos.get(0).activityInfo.packageName);
            if (uri.getScheme() == null) {
                uri = Uri.parse("http://" + uri.toString());
            }
            try {
                customTabsIntent.launchUrl(this, uri);
            } catch (ActivityNotFoundException e) {
                if (handleError) {
                    openInBrowser(uri, pm, false);
                } else {
                    openInWebView(uri);
                }
            }
        } else {
            if (handleError) {
                openInBrowser(uri, pm, false);
            } else {
                openInWebView(uri);
            }
        }
    }

    private void openInWebView(Uri uri) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }
}
