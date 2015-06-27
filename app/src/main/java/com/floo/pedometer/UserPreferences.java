package com.floo.pedometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by SONY_VAIO on 6/11/2015.
 */
public class UserPreferences {
    public static final String KEY_USER_ID = "userID";
    public static final String KEY_USERNAME = "username";

    public static final String KEY_BLUETOOTH_NAME = "bluetoothDevice";
    public static final String KEY_BLUETOOTH_ADDRESS = "macAddres";

    public static final String KEY_LAST_SYNC = "lastSync";//save last sync with watch datetime

    public static final String KEY_LAST_BADGE_DATE = "lastBadgeDate"; //save time get gold badge
    public static final String KEY_WEEK = "weekState"; //save the week
    public static final String KEY_WIN_PLATINUM_WEEK = "lastWinPlatinumWeek";//save week get platinum badge
    public static final String KEY_COUNT_GOLD_WEEK = "totalGoldWeek";//save total gold in a week


    public static final String KEY_APP_STATE = "appState";
    public static final String APP_RUNNING = "running";
    public static final String APP_NOT_RUNNING = "notRunning";


    SharedPreferences sharedPreferences;
    Context context;
    public UserPreferences(Context context){
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }
    public void setUserPreferences(String key,String value)
    {
        if(key.equals(KEY_USER_ID)||key.equals(KEY_BLUETOOTH_ADDRESS)||
                key.equals(KEY_USERNAME)||key.equals(KEY_BLUETOOTH_NAME)||
                key.equals(KEY_LAST_SYNC)||key.equals(KEY_APP_STATE)||key.equals(APP_RUNNING)||key.equals(APP_NOT_RUNNING)
                ||key.equals(KEY_LAST_BADGE_DATE)||key.equals(KEY_WEEK)
                ||key.equals(KEY_WIN_PLATINUM_WEEK)||key.equals(KEY_COUNT_GOLD_WEEK)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }
    public String getUserPreferences(String key)
    {
        String result = "";
        if(key.equals(KEY_USER_ID)||key.equals(KEY_BLUETOOTH_ADDRESS)||
                key.equals(KEY_USERNAME)||key.equals(KEY_BLUETOOTH_NAME)||
                key.equals(KEY_LAST_SYNC)||key.equals(KEY_APP_STATE)||key.equals(APP_RUNNING)||key.equals(APP_NOT_RUNNING)
                ||key.equals(KEY_LAST_BADGE_DATE)||key.equals(KEY_WEEK)
                ||key.equals(KEY_WIN_PLATINUM_WEEK)||key.equals(KEY_COUNT_GOLD_WEEK)) {
            result = sharedPreferences.getString(key,"");
        }
        return result;
    }

    public void clearAllPreferences()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

}
