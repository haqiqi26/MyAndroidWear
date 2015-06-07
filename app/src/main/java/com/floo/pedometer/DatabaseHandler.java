package com.floo.pedometer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by SONY_VAIO on 6/6/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "dataManager";

    // Contacts table name
    private static final String TABLE_DATA = "outdoorDatas";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TIMESTAMP = "recordstamp";
    private static final String KEY_MINUTES = "minutes";

    private static DatabaseHandler sInstance;

    public static synchronized DatabaseHandler getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIMESTAMP + " DATETIME,"
                + KEY_MINUTES + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        // Create tables again
        onCreate(db);
    }

    public void addData(OutdoorData outdoorData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, outdoorData.getTimeStamp());
        values.put(KEY_MINUTES, outdoorData.getMinutes());

        // Inserting Row
        db.insert(TABLE_DATA, null, values);
        db.close(); // Closing database connection
    }

    public void addDataToday(int minutes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        values.put(KEY_MINUTES, minutes);

        // Inserting Row
        db.insert(TABLE_DATA, null, values);
        db.close(); // Closing database connection
    }



    public List<OutdoorData> getAllOutdoorsDatas() {
        List<OutdoorData> outdoorDatas = new ArrayList<OutdoorData>();
        // Select All Query
        String selectQuery = "SELECT DATE("+KEY_TIMESTAMP+"),SUM("+KEY_MINUTES+") FROM " + TABLE_DATA + " GROUP BY DATE("+KEY_TIMESTAMP+") ORDER BY DATE("+KEY_TIMESTAMP+") ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                OutdoorData data= new OutdoorData(cursor.getString(0),Integer.parseInt(cursor.getString(1)));

                outdoorDatas.add(data);
            } while (cursor.moveToNext());
        }

        // return list
        return outdoorDatas;
    }


    public void testingQuery(){
        String selectQuery = "SELECT DATE("+KEY_TIMESTAMP+"),DATE('now'),DATE(),"+KEY_MINUTES+"  FROM " + TABLE_DATA ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Log.e("result","data: "+cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getInt(3));
            } while (cursor.moveToNext());
        }
    }
    public int getTodayMinutes(){
        int minutes=0;
        String selectQuery = "SELECT SUM("+KEY_MINUTES+") FROM " + TABLE_DATA + " WHERE DATE("+KEY_TIMESTAMP+") = DATE() GROUP BY DATE("+KEY_TIMESTAMP+")";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
            minutes = Integer.parseInt(cursor.getString(0));
        }
        return minutes;
    }
    public void deleteData(OutdoorData outdoorData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATA, KEY_ID + " = ?",
                new String[] { String.valueOf(outdoorData.getId()) });
        db.close();
    }

    public int updateData(OutdoorData outdoorData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, outdoorData.getTimeStamp());
        values.put(KEY_MINUTES, outdoorData.getMinutes());

        // updating row
        return db.update(TABLE_DATA, values, KEY_ID + " = ?",
                new String[] { String.valueOf(outdoorData.getId()) });
    }
    public void truncateTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATA,null,null);
    }
}
