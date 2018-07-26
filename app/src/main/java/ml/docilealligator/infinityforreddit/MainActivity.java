package ml.docilealligator.infinityforreddit;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String nameState = "NS";
    private String profileImageUrlState = "PIUS";
    private String bannerImageUrlState = "BIUS";
    private String karmaState = "KS";

    private TextView mNameTextView;
    private TextView mKarmaTextView;
    private CircleImageView mProfileImageView;
    private ImageView mBannerImageView;

    private Fragment mFragment;
    private RequestManager glide;

    private String mName;
    private String mProfileImageUrl;
    private String mBannerImageUrl;
    private String mKarma;
    private boolean mFetchUserInfoSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        mNameTextView = header.findViewById(R.id.name_text_view_nav_header_main);
        mKarmaTextView = header.findViewById(R.id.karma_text_view_nav_header_main);
        mProfileImageView = header.findViewById(R.id.profile_image_view_nav_header_main);
        mBannerImageView = header.findViewById(R.id.banner_image_view_nav_header_main);

        mName = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.USER_KEY, "");
        mProfileImageUrl = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.PROFILE_IMAGE_URL_KEY, "");
        mBannerImageUrl = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.BANNER_IMAGE_URL_KEY, "");
        mKarma = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.KARMA_KEY, "");

        mNameTextView.setText(mName);
        mKarmaTextView.setText(mKarma);
        glide = Glide.with(this);
        if(!mProfileImageUrl.equals("")) {
            glide.load(mProfileImageUrl).into(mProfileImageView);
        }
        if(!mBannerImageUrl.equals("")) {
            glide.load(mBannerImageUrl).into(mBannerImageView);
        }

        String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        if(accessToken.equals("")) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        } else {
            if(savedInstanceState == null) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                mFragment = new BestPostFragment();
                fragmentTransaction.replace(R.id.frame_layout_content_main, mFragment).commit();
            } else {
                mFragment = getFragmentManager().getFragment(savedInstanceState, "outStateFragment");
                getFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();
            }
        }

        if(savedInstanceState == null && !mFetchUserInfoSuccess) {
            new FetchUserInfo(this, Volley.newRequestQueue(this)).queryUserInfo(new FetchUserInfo.FetchUserInfoListener() {
                @Override
                public void onFetchUserInfoSuccess(String response) {
                    new ParseUserInfo().parseUserInfo(MainActivity.this, response, new ParseUserInfo.ParseUserInfoListener() {
                        @Override
                        public void onParseUserInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                            mNameTextView.setText(name);
                            if(!mProfileImageUrl.equals("")) {
                                glide.load(profileImageUrl).into(mProfileImageView);
                            }
                            if(!mBannerImageUrl.equals("")) {
                                glide.load(bannerImageUrl).into(mBannerImageView);
                            }

                            mName = name;
                            mProfileImageUrl = profileImageUrl;
                            mBannerImageUrl = bannerImageUrl;
                            mKarma = getString(R.string.karma_info, karma);

                            mKarmaTextView.setText(mKarma);

                            SharedPreferences.Editor editor = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).edit();
                            editor.putString(SharedPreferencesUtils.USER_KEY, name);
                            editor.putString(SharedPreferencesUtils.PROFILE_IMAGE_URL_KEY, profileImageUrl);
                            editor.putString(SharedPreferencesUtils.BANNER_IMAGE_URL_KEY, bannerImageUrl);
                            editor.putString(SharedPreferencesUtils.KARMA_KEY, mKarma);
                            editor.apply();
                            mFetchUserInfoSuccess = true;
                        }

                        @Override
                        public void onParseUserInfoFail() {
                            mFetchUserInfoSuccess = false;
                        }
                    });
                }

                @Override
                public void onFetchUserInfoFail() {

                }
            }, 1);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mFragment != null) {
            getFragmentManager().putFragment(outState, "outStateFragment", mFragment);
        }
        outState.putString(nameState, mName);
        outState.putString(profileImageUrlState, mProfileImageUrl);
        outState.putString(bannerImageUrlState, mBannerImageUrl);
        outState.putString(karmaState, mKarma);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mName = savedInstanceState.getString(nameState);
        mProfileImageUrl = savedInstanceState.getString(profileImageUrlState);
        mBannerImageUrl = savedInstanceState.getString(bannerImageUrlState);
        mKarma = savedInstanceState.getString(karmaState);
        mNameTextView.setText(mName);
        mKarmaTextView.setText(mKarma);
        if(!mProfileImageUrl.equals("")) {
            glide.load(mProfileImageUrl).into(mProfileImageView);
        }
        if(!mBannerImageUrl.equals("")) {
            glide.load(mBannerImageUrl).into(mBannerImageView);
        }
    }
}
