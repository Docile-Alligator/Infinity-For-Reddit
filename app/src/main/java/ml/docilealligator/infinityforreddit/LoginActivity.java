package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private String authCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView webView = findViewById(R.id.webview_login_activity);
        webView.getSettings().setJavaScriptEnabled(true);

        Uri baseUri = Uri.parse(RedditUtils.OAUTH_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(RedditUtils.CLIENT_ID_KEY, RedditUtils.CLIENT_ID);
        uriBuilder.appendQueryParameter(RedditUtils.RESPONSE_TYPE_KEY, RedditUtils.RESPONSE_TYPE);
        uriBuilder.appendQueryParameter(RedditUtils.STATE_KEY, RedditUtils.STATE);
        uriBuilder.appendQueryParameter(RedditUtils.REDIRECT_URI_KEY, RedditUtils.REDIRECT_URI);
        uriBuilder.appendQueryParameter(RedditUtils.DURATION_KEY, RedditUtils.DURATION);
        uriBuilder.appendQueryParameter(RedditUtils.SCOPE_KEY, RedditUtils.SCOPE);

        String url = uriBuilder.toString();

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.contains("&code=") || url.contains("?code=")) {
                    Uri uri = Uri.parse(url);
                    String state = uri.getQueryParameter("state");
                    if(state.equals(RedditUtils.STATE)) {
                        authCode = uri.getQueryParameter("code");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("authCode", authCode);

                        final SharedPreferences.Editor editor = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).edit();
                        editor.putString(SharedPreferencesUtils.AUTH_CODE_KEY, authCode);
                        editor.apply();

                        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                        String tokenRetrievalUrl = "https://www.reddit.com/api/v1/access_token";
                        StringRequest requestToken = new StringRequest(Request.Method.POST, tokenRetrievalUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject responseJSON = new JSONObject(response);
                                    String accessToken = responseJSON.getString(RedditUtils.ACCESS_TOKEN_KEY);
                                    String refreshToken = responseJSON.getString(RedditUtils.REFRESH_TOKEN_KEY);

                                    editor.putString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, accessToken);
                                    editor.putString(SharedPreferencesUtils.REFRESH_TOKEN_KEY, refreshToken);
                                    editor.apply();
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(LoginActivity.this, "Error occurred when parsing the JSON response", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(LoginActivity.this, "Error Retrieving the token", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put(RedditUtils.GRANT_TYPE_KEY, "authorization_code");
                                params.put("code", authCode);
                                params.put("redirect_uri", RedditUtils.REDIRECT_URI);

                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return RedditUtils.getHttpBasicAuthHeader();
                            }
                        };

                        queue.add(requestToken);
                    } else {
                        Toast.makeText(LoginActivity.this, "Something went wrong. Try again later.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else if (url.contains("error=access_denied")) {
                    Toast.makeText(LoginActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}
