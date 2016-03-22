package app.bennsandoval.com.woodmin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.util.Base64;

import com.crashlytics.android.Crashlytics;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import app.bennsandoval.com.woodmin.interfaces.Woocommerce;
import app.bennsandoval.com.woodmin.utilities.Utility;
import io.fabric.sdk.android.Fabric;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by Mackbook on 1/10/15.
 */
public class Woodmin extends Application {

    public final String LOG_TAG = Woodmin.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }

    public Woocommerce getWoocommerceApiHandler() {

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                String authenticationHeader = getAuthenticationHeader(getApplicationContext());
                request.addHeader("Authorization", authenticationHeader);
                request.addHeader("Accept" , "application/json");
                request.addHeader("Content-Type" , "application/json");
            }
        };

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(60000, TimeUnit.MILLISECONDS);
        client.setReadTimeout(60000, TimeUnit.MILLISECONDS);
        client.setCache(null);

        //TODO Remove this if you don't have a self cert
        if(Utility.getSSLSocketFactory() != null){
            client.setSslSocketFactory(Utility.getSSLSocketFactory());
            client.setHostnameVerifier(Utility.getHostnameVerifier());
        }

        String server = Utility.getPreferredServer(getApplicationContext());
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(server + "/wc-api/v3")
                .setClient(new OkClient(client))
                .setConverter(new GsonConverter(new GsonBuilder().create()))
                .setRequestInterceptor(requestInterceptor)
                .build();

        return restAdapter.create(Woocommerce.class);
    }

    private String getAuthenticationHeader(Context context) {

        String user = Utility.getPreferredUser(context);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account account = new Account("Woodmin", context.getString(R.string.sync_account_type));
        if ( accountManager.getPassword(account) == null  ) {
            String secret = Utility.getPreferredSecret(context);
            if (!accountManager.addAccountExplicitly(account, secret, null)) {
                return "";
            }
        }

        return "Basic " + Base64.encodeToString(
                (user + ":" + accountManager.getPassword(account)).getBytes(),
                Base64.NO_WRAP);

    }

}
