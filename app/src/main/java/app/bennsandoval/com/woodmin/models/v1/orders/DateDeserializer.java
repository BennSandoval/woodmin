package app.bennsandoval.com.woodmin.models.v1.orders;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by bennsandoval on 3/1/17.
 */

public class DateDeserializer implements JsonDeserializer<Date> {
    private final String LOG_TAG = DateDeserializer.class.getSimpleName();
    private static final String[] DATE_FORMATS = new String[] {
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
    };

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String date = json.getAsString();
        for (String format : DATE_FORMATS) {
            try {
                Log.v(LOG_TAG, "Date: " + date + " Format " + format);

                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                return sdf.parse(date);
            } catch (Exception e) {
            }
        }
        return null;
        /*
        throw new JsonParseException("Unparseable date: \"" + json.getAsString()
                + "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
                */
    }
}
