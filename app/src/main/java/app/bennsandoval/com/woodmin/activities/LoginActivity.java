package app.bennsandoval.com.woodmin.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.interfaces.Woocommerce;
import app.bennsandoval.com.woodmin.models.shop.Shop;
import app.bennsandoval.com.woodmin.sync.WoodminSyncAdapter;
import app.bennsandoval.com.woodmin.utilities.Utility;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class LoginActivity extends AppCompatActivity {

    public final String LOG_TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private EditText mServerView;
    private EditText mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Gson gson = new GsonBuilder().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Utility.getPreferredServer(getApplicationContext())!= null){
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(main);
            finish();
        } else {
            setContentView(R.layout.activity_login);

            // Set up the login form.
            mServerView = (EditText) findViewById(R.id.server);
            mUserView = (EditText) findViewById(R.id.user);
            mPasswordView = (EditText) findViewById(R.id.password);

            //Test instance

            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
            mSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);
        }
    }

    public void attemptLogin() {

        // Reset errors.
        mServerView.setError(null);
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String server = mServerView.getText().toString();
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid server address.
        if (TextUtils.isEmpty(server) && !isValid(server)) {
            mServerView.setError(getString(R.string.error_field_length));
            focusView = mServerView;
            cancel = true;
        } else if (!server.contains("https")){
            mServerView.setError(getString(R.string.error_server_https));
            focusView = mServerView;
            cancel = true;
        }

        // Check for a valid user address.
        if (TextUtils.isEmpty(user) && !isValid(user)) {
            mUserView.setError(getString(R.string.error_field_length));
            focusView = mUserView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) && !isValid(password)) {
            mPasswordView.setError(getString(R.string.error_field_length));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            userLogin(server, user, password);
        }
    }


    private boolean isValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void userLogin (final String server, final String user, final String password){

        showProgress(true);

        final String authenticationHeader = "Basic " + Base64.encodeToString(
                (user + ":" + password).getBytes(),
                Base64.NO_WRAP);

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Authorization", authenticationHeader);
                request.addHeader("Accept" , "application/json");
                request.addHeader("Content-Type" , "application/json");
            }
        };

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(60000, TimeUnit.MILLISECONDS);
        client.setReadTimeout(60000, TimeUnit.MILLISECONDS);
        client.setCache(null);
        if(Utility.getSSLSocketFactory() != null){
            client.setSslSocketFactory(Utility.getSSLSocketFactory());
            client.setHostnameVerifier(Utility.getHostnameVerifier());
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(server+"/wc-api/v2")
                .setClient(new OkClient(client))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(requestInterceptor)
                .build();

        Woocommerce woocommerceApi = restAdapter.create(Woocommerce.class);
        woocommerceApi.getShop(new Callback<Shop>() {
            @Override
            public void success(Shop shop, Response response) {
                showProgress(false);

                Utility.setPreferredServer(getApplicationContext(), server);
                Utility.setPreferredUserSecret(getApplicationContext(),user, password);

                WoodminSyncAdapter.initializeSyncAdapter(getApplicationContext());
                WoodminSyncAdapter.syncImmediately(getApplicationContext());

                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                finish();
            }

            @Override
            public void failure(RetrofitError error) {

                showProgress(false);
                mUserView.setError(getString(R.string.error_incorrect));
                mPasswordView.setError(getString(R.string.error_incorrect));
                mServerView.setError(getString(R.string.error_incorrect));
                mServerView.requestFocus();

                Log.v(LOG_TAG,"Shop sync error");
                if (error.getCause() instanceof SSLHandshakeException) {
                    Log.e(LOG_TAG,"SSLHandshakeException Shop sync");
                } else if (error.getResponse()==null) {
                    Log.e(LOG_TAG,"Not response error Shop sync");
                } else {
                    int httpCode = error.getResponse().getStatus();
                    Log.e(LOG_TAG,httpCode + " error Shop sync");
                    switch (httpCode){
                        case 401:
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }
}



