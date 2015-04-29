package app.bennsandoval.com.woodmin;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Mackbook on 1/10/15.
 */
public class Woodmin extends Application {

    public final String LOG_TAG = Woodmin.class.getSimpleName();

    @Override public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }

}
