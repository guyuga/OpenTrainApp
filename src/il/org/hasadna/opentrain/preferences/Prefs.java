package il.org.hasadna.opentrain.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Calendar;

public final class Prefs {
    private static final String LOGTAG = Prefs.class.getName();
    private static final String PREFS_FILE = Prefs.class.getName();
    private static final String REPORTS_PREF = "reports";
    private static final String SEED_CREATED_ON = "seed_created_on";
    private static final String DAILY_SEED = "daily_seed";

    private static final int DATE_CHANGE_DELAY_HOURS = 5; // late trains will
    // count in the
    // previous day
    private static final int DEVICE_ID_BYTE_LENGTH = 8;

    public final int VERSION_CODE;
    public final String VERSION_NAME;
    private Context mContext;

    private static final String PREF_WIFI_MIN_UPDATE_TIME = "PREF_WIFI_MIN_UPDATE_TIME";
    private static final String PREF_WIFI_MODE_TRAIN_FOUND_PERIOD = "PREF_WIFI_MODE_TRAIN_FOUND_PERIOD";
    private static final String PREF_WIFI_MODE_TRAIN_SCANNIG_PERIOD = "PREF_WIFI_MODE_TRAIN_SCANNIG_PERIOD";
    private static final String PREF_LOCATION_API_UPDATE_INTERVAL = "PREF_LOCATION_API_UPDATE_INTERVAL";
    private static final String PREF_LOCATION_API_FAST_CEILING_INTERVAL = "PREF_LOCATION_API_FAST_CEILING_INTERVAL";
    private static final String PREF_RECORD_BATCH_SIZE = "PREF_RECORD_BATCH_SIZE";
    private static final String PREF_TRAIN_INDICATION_TTL = "PREF_TRAIN_INDICATION_TTL";
    private static final String PREF_CONFIG_VERSION = "PREF_CONFIG_VERSION";

    private static final long DEF_WIFI_MIN_UPDATE_TIME = 1000;
    private static final long DEF_WIFI_MODE_TRAIN_FOUND_PERIOD = 1 * 15 * 1000;
    private static final long DEF_WIFI_MODE_TRAIN_SCANNIG_PERIOD = 5 * 60 * 1000;
    private static final long DEF_LOCATION_API_UPDATE_INTERVAL = 5000;
    private static final long DEF_LOCATION_API_FAST_CEILING_INTERVAL = 1000;
    private static final int DEF_RECORD_BATCH_SIZE = 5;
    private static final long DEF_TRAIN_INDICATION_TTL = 1 * 1 * 30 * 1000;
    private static final long DEF_CONFIG_VERSION = 1;

    public long WIFI_MIN_UPDATE_TIME;
    public long WIFI_MODE_TRAIN_SCANNIG_PERIOD;
    public long WIFI_MODE_TRAIN_FOUND_PERIOD;
    public long LOCATION_API_UPDATE_INTERVAL;
    public long LOCATION_API_FAST_CEILING_INTERVAL;
    public int RECORD_BATCH_SIZE;
    public long TRAIN_INDICATION_TTL;
    public long CONFIG_VERSION;

    private Prefs(Context context) {
        int versionCode;
        String versionName;
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionCode = pi.versionCode;
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException exception) {
            Log.e(LOGTAG, "getPackageInfo failed", exception);
            versionCode = 0;
            versionName = "";
        }
        VERSION_CODE = versionCode;
        VERSION_NAME = versionName;
        mContext = context;

        loadPreferenceFromStorage();
    }

    public void setReports(String json) {
        setStringPref(REPORTS_PREF, json);
    }

    public String getReports() {
        return getStringPref(REPORTS_PREF);
    }

    public String getDailyID() {
        // Prepare a string consisting of the year and day. This string will not
        // change at midnight, but rather in the early morning.
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, -DATE_CHANGE_DELAY_HOURS);
        String nowDate = "" + now.get(Calendar.YEAR)
                + now.get(Calendar.DAY_OF_YEAR);
        String existingSeedDate = getStringPref(SEED_CREATED_ON);

        // Create a new seed if current one is out of date
        if (existingSeedDate == null || !existingSeedDate.equals(nowDate)) {

            Log.d(LOGTAG, "Creating new daily random ID...");
            SecureRandom sr = new SecureRandom();
            // take 8 bytes -> 10^-9 chance of collision for 190,000 device_ids,
            // according to
            // http://en.wikipedia.org/wiki/Birthday_problem#Probability_table
            byte[] randomIdByteArray = new byte[DEVICE_ID_BYTE_LENGTH];
            sr.nextBytes(randomIdByteArray);
            String randomId = byteArrayToString(randomIdByteArray);

            SharedPreferences.Editor editor = getPrefs().edit();
            editor.putString(DAILY_SEED, randomId);
            editor.putString(SEED_CREATED_ON, nowDate);
            apply(editor);
            Log.d(LOGTAG, "New daily random ID is: " + randomId);
        }
        String dailyID = getPrefs().getString(DAILY_SEED, "");
        String userName = getUserPrefs().getString(PREF_USER_NAME, "");
        if (!("").equals(userName)) {
            dailyID = userName + "_" + dailyID;
        }
        return dailyID;
    }

    private static String byteArrayToString(byte[] ba) {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(String.format("%02x", b));
        return hex.toString();
    }

    private String getStringPref(String key) {
        return getPrefs().getString(key, null);
    }

    private void setStringPref(String key, String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(key, value);
        apply(editor);
    }

    @SuppressLint("NewApi")
    private static void apply(SharedPreferences.Editor editor) {
        if (VERSION.SDK_INT >= 9) {
            editor.apply();
        } else if (!editor.commit()) {
            Log.e(LOGTAG, "", new IllegalStateException("commit() failed?!"));
        }
    }

    @SuppressLint("InlinedApi")
    private SharedPreferences getPrefs() {
        return mContext.getSharedPreferences(PREFS_FILE,
                Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();

    }

    private static Prefs mInstance;

    public static Prefs getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Prefs(context);
        }
        return mInstance;
    }

    private void loadPreferenceFromStorage() {

        SharedPreferences sharedPreferences = getPrefs();

        WIFI_MIN_UPDATE_TIME = sharedPreferences.getLong(PREF_WIFI_MIN_UPDATE_TIME, DEF_WIFI_MIN_UPDATE_TIME);
        WIFI_MODE_TRAIN_FOUND_PERIOD = sharedPreferences.getLong(PREF_WIFI_MODE_TRAIN_FOUND_PERIOD, DEF_WIFI_MODE_TRAIN_FOUND_PERIOD);
        WIFI_MODE_TRAIN_SCANNIG_PERIOD = sharedPreferences.getLong(PREF_WIFI_MODE_TRAIN_SCANNIG_PERIOD, DEF_WIFI_MODE_TRAIN_SCANNIG_PERIOD);
        LOCATION_API_UPDATE_INTERVAL = sharedPreferences.getLong(PREF_LOCATION_API_UPDATE_INTERVAL, DEF_LOCATION_API_UPDATE_INTERVAL);
        LOCATION_API_FAST_CEILING_INTERVAL = sharedPreferences.getLong(PREF_LOCATION_API_FAST_CEILING_INTERVAL, DEF_LOCATION_API_FAST_CEILING_INTERVAL);
        RECORD_BATCH_SIZE = sharedPreferences.getInt(PREF_RECORD_BATCH_SIZE, DEF_RECORD_BATCH_SIZE);
        TRAIN_INDICATION_TTL = sharedPreferences.getLong(PREF_TRAIN_INDICATION_TTL, DEF_TRAIN_INDICATION_TTL);
        CONFIG_VERSION = sharedPreferences.getLong(PREF_CONFIG_VERSION, DEF_CONFIG_VERSION);
    }

    public void setPreferenceFromServer(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);

            long wifi_min_update_time = jsonObject.optLong("WIFI_MIN_UPDATE_TIME");
            if (wifi_min_update_time != 0) {
                WIFI_MIN_UPDATE_TIME = wifi_min_update_time;
            }

            long wifi_mode_train_found_period = jsonObject
                    .optLong("MODE_TRAIN_WIFI_FOUND_PERIOD");
            if (wifi_mode_train_found_period != 0) {
                WIFI_MODE_TRAIN_FOUND_PERIOD = wifi_mode_train_found_period;
            }

            long wifi_mode_train_scannig_period = jsonObject.optLong("MODE_TRAIN_WIFI_SCANNIG_PERIOD");
            if (wifi_mode_train_scannig_period != 0) {
                WIFI_MODE_TRAIN_SCANNIG_PERIOD = wifi_mode_train_scannig_period;
            }

            long location_api_update_interval = jsonObject.optLong("LOCATION_API_UPDATE_INTERVAL");
            if (location_api_update_interval != 0) {
                LOCATION_API_UPDATE_INTERVAL = location_api_update_interval;
            }

            long location_api_fast_ceiling_interval = jsonObject.optLong("LOCATION_API_FAST_CEILING_INTERVAL");
            if (location_api_fast_ceiling_interval != 0) {
                LOCATION_API_FAST_CEILING_INTERVAL = location_api_fast_ceiling_interval;
            }

            int record_batch_size = jsonObject.optInt("RECORD_BATCH_SIZE");
            if (record_batch_size != 0) {
                RECORD_BATCH_SIZE = record_batch_size;
            }

            long train_indication_ttl = jsonObject.optLong("TRAIN_INDICATION_TTL");
            if (train_indication_ttl != 0) {
                TRAIN_INDICATION_TTL = train_indication_ttl;
            }

            long config_version = jsonObject.optLong("CONFIG_VERSION");
            if (config_version != 0) {
                CONFIG_VERSION = config_version;
            }
        } catch (Exception e) {

        }
    }

    public void savePreferenceFromServer() {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putLong(PREF_WIFI_MIN_UPDATE_TIME, WIFI_MIN_UPDATE_TIME);
        editor.putLong(PREF_WIFI_MODE_TRAIN_FOUND_PERIOD, WIFI_MODE_TRAIN_FOUND_PERIOD);
        editor.putLong(PREF_WIFI_MODE_TRAIN_SCANNIG_PERIOD, WIFI_MODE_TRAIN_SCANNIG_PERIOD);
        editor.putLong(PREF_LOCATION_API_UPDATE_INTERVAL, LOCATION_API_UPDATE_INTERVAL);
        editor.putLong(PREF_LOCATION_API_FAST_CEILING_INTERVAL, LOCATION_API_FAST_CEILING_INTERVAL);
        editor.putInt(PREF_RECORD_BATCH_SIZE, RECORD_BATCH_SIZE);
        editor.putLong(PREF_TRAIN_INDICATION_TTL, TRAIN_INDICATION_TTL);
        editor.putLong(PREF_CONFIG_VERSION, CONFIG_VERSION);

        apply(editor);
    }

    //user selectable settings
    private SharedPreferences getUserPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    private static final String PREF_USER_NAME = "prefUsername";

    public void setPreferenceFromUser(String newPresfs) {
        try {
            String[] keyValue = newPresfs.split(":");
            setStringUserPref(keyValue[0], keyValue[1]);
        } catch (Exception e) {

        }
    }

    private void setStringUserPref(String key, String value) {
        SharedPreferences.Editor editor = getUserPrefs().edit();
        editor.putString(key, value);
        apply(editor);
    }
}
