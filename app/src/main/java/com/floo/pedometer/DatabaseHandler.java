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
    private static final String TABLE_OUTDOOR_DATAS = "outdoorDatas";
    private static final String TABLE_USER_BADGES = "userBadges";
    private static final String TABLE_USER_TREES = "userTrees";

    // Contacts Table Columns names
    private static final String KEY_OUTDOOR_ID = "id";
    private static final String KEY_OUTDOOR_TIMESTAMP = "recordstamp";
    private static final String KEY_OUTDOOR_MINUTES = "minutes";
    private static final String KEY_OUTDOOR_FLAG = "flag";

    private static final String KEY_USER_BADGES = "userid";
    private static final String KEY_PLATINUM_BADGES = "platinum";
    private static final String KEY_GOLD_BADGES = "gold";

    private static final String KEY_USER_TREES = "userid";
    private static final String KEY_TREES_COMPLETED = "treesCompleted";
    private static final String KEY_LAST_TREE_PROGRESS= "lastTreeProgress";



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
        String CREATE_OUTDOOR_TABLE = "CREATE TABLE " + TABLE_OUTDOOR_DATAS + "("
                + KEY_OUTDOOR_ID + " INTEGER PRIMARY KEY," + KEY_OUTDOOR_TIMESTAMP + " DATETIME,"
                + KEY_OUTDOOR_MINUTES + " INTEGER," + KEY_OUTDOOR_FLAG + " INTEGER"+ ")";
        db.execSQL(CREATE_OUTDOOR_TABLE);
        String CREATE_BADGES_TABLE = "CREATE TABLE " + TABLE_USER_BADGES + "("
                + KEY_USER_BADGES+ " VARCHAR(100) PRIMARY KEY," + KEY_PLATINUM_BADGES+ " INTEGER,"
                + KEY_GOLD_BADGES + " INTEGER" + ")";
        db.execSQL(CREATE_BADGES_TABLE);
        String CREATE_TREES_TABLE = "CREATE TABLE " + TABLE_USER_TREES + "("
                + KEY_USER_TREES+ " VARCHAR(100) PRIMARY KEY," + KEY_TREES_COMPLETED+ " INTEGER,"
                + KEY_LAST_TREE_PROGRESS + " INTEGER" + ")";
        db.execSQL(CREATE_TREES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OUTDOOR_DATAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_BADGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_TREES);
        // Create tables again
        onCreate(db);
    }

    //outdoor data operation
    public void addOutdoorData(OutdoorData outdoorData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_OUTDOOR_TIMESTAMP, outdoorData.getTimeStamp());
        values.put(KEY_OUTDOOR_MINUTES, outdoorData.getMinutes());
        values.put(KEY_OUTDOOR_FLAG, outdoorData.getFlag());

        // Inserting Row
        db.insert(TABLE_OUTDOOR_DATAS, null, values);
        db.close(); // Closing database connection
    }

    public void addOutdoorDataToday(int minutes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_OUTDOOR_TIMESTAMP, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        values.put(KEY_OUTDOOR_MINUTES, minutes);
        values.put(KEY_OUTDOOR_FLAG, 0);


        // Inserting Row
        db.insert(TABLE_OUTDOOR_DATAS, null, values);
        db.close(); // Closing database connection
    }



    public List<OutdoorData> getAllOutdoorsDatas() {
        List<OutdoorData> outdoorDatas = new ArrayList<OutdoorData>();
        // Select All Query
        String selectQuery = "SELECT DATE("+ KEY_OUTDOOR_TIMESTAMP +"),SUM("+ KEY_OUTDOOR_MINUTES +") FROM " + TABLE_OUTDOOR_DATAS + " GROUP BY DATE("+ KEY_OUTDOOR_TIMESTAMP +") ORDER BY DATE("+ KEY_OUTDOOR_TIMESTAMP +") ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                OutdoorData data= new OutdoorData(cursor.getString(0),cursor.getInt(1));

                outdoorDatas.add(data);
            } while (cursor.moveToNext());
        }
        // return list
        return outdoorDatas;
    }

    public List<OutdoorData> getUnsyncOutdoorsDatas() {
        List<OutdoorData> outdoorDatas = new ArrayList<OutdoorData>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_OUTDOOR_DATAS + " WHERE "+KEY_OUTDOOR_FLAG+" = ?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0"});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                OutdoorData data= new OutdoorData(cursor.getInt(0),cursor.getString(1),cursor.getInt(2),cursor.getInt(3));

                outdoorDatas.add(data);
            } while (cursor.moveToNext());
        }
        // return list
        return outdoorDatas;
    }


    public int getTodayMinutes(){
        int minutes=0;
        String selectQuery = "SELECT SUM("+ KEY_OUTDOOR_MINUTES +") AS TOTAL_MINUTES FROM " + TABLE_OUTDOOR_DATAS + " WHERE DATE("+ KEY_OUTDOOR_TIMESTAMP +") = DATE('now','localtime') GROUP BY DATE("+ KEY_OUTDOOR_TIMESTAMP +")";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            minutes = Integer.parseInt(cursor.getString(0));
        }
        return minutes;
    }
    public void testingQuery(){
        String selectQuery = "SELECT DATE("+ KEY_OUTDOOR_TIMESTAMP +"),DATE('now','localtime') FROM " + TABLE_OUTDOOR_DATAS ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null&&cursor.moveToFirst()) {
            Log.e("data",cursor.getString(0)+" "+cursor.getString(1));
        }
    }
    public void deleteOutdoorData(OutdoorData outdoorData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OUTDOOR_DATAS, KEY_OUTDOOR_ID + " = ?",
                new String[]{String.valueOf(outdoorData.getId())});
        db.close();
    }

    public int updateOutdoorData(OutdoorData outdoorData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_OUTDOOR_TIMESTAMP, outdoorData.getTimeStamp());
        values.put(KEY_OUTDOOR_MINUTES, outdoorData.getMinutes());
        values.put(KEY_OUTDOOR_FLAG, outdoorData.getFlag());


        // updating row
        return db.update(TABLE_OUTDOOR_DATAS, values, KEY_OUTDOOR_ID + " = ?",
                new String[] { String.valueOf(outdoorData.getId()) });
    }
    public void truncateOutdoorTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OUTDOOR_DATAS,null,null);
    }

    //user badges data operation
    public void addUserBadgeData(UserBadge userBadge)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_USER_BADGES, userBadge.getUserID());
        values.put(KEY_PLATINUM_BADGES, userBadge.getPlatinumBadge());
        values.put(KEY_GOLD_BADGES, userBadge.getGoldBadge());

        // Inserting Row
        db.insert(TABLE_USER_BADGES, null, values);
        db.close(); // Closing database connection
    }
    public int updateUserBadgeData(UserBadge userBadge) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PLATINUM_BADGES, userBadge.getPlatinumBadge());
        values.put(KEY_GOLD_BADGES, userBadge.getGoldBadge());

        // updating row
        return db.update(TABLE_USER_BADGES, values, KEY_USER_BADGES + " = ?",
                new String[] { String.valueOf(userBadge.getUserID()) });
    }
    public UserBadge getUserBadge(String userID)
    {
        UserBadge userBadge=null;
        String selectQuery = "SELECT * FROM " + TABLE_USER_BADGES + " WHERE "+KEY_USER_BADGES+" = ?";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userID});

        if (cursor != null && cursor.moveToFirst()) {
            userBadge = new UserBadge(cursor.getString(0),cursor.getInt(1),cursor.getInt(2));
        }
        return userBadge;
    }
    public List<UserBadge> getAllUserBadges()
    {
        List<UserBadge> userBadges= new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_USER_BADGES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                UserBadge data= new UserBadge(cursor.getString(0),cursor.getInt(1),cursor.getInt(2));
                userBadges.add(data);

            } while (cursor.moveToNext());
        }
        // return list
        return userBadges;
    }
    public void truncateUserBadgeTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_BADGES,null,null);
    }

    //user tree data operation
    public void addUserTreeData(UserTree userTree)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_USER_TREES, userTree.getUserID());
        values.put(KEY_TREES_COMPLETED, userTree.getTreesCompleted());
        values.put(KEY_LAST_TREE_PROGRESS, userTree.getLastTreeProgress());

        // Inserting Row
        db.insert(TABLE_USER_TREES, null, values);
        db.close(); // Closing database connection
    }
    public int updateUserTreeData(UserTree userTree) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TREES_COMPLETED, userTree.getTreesCompleted());
        values.put(KEY_LAST_TREE_PROGRESS, userTree.getLastTreeProgress());

        // updating row
        return db.update(TABLE_USER_TREES, values, KEY_USER_TREES + " = ?",
                new String[] { String.valueOf(userTree.getUserID()) });
    }
    public UserTree getUserTree(String userID)
    {
        UserTree userTree=null;
        String selectQuery = "SELECT * FROM " + TABLE_USER_TREES + " WHERE "+KEY_USER_TREES+" = ?";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userID});

        if (cursor != null&&cursor.moveToFirst()) {
            userTree = new UserTree(cursor.getString(0),cursor.getInt(1),cursor.getInt(2));
        }
        return userTree;
    }
    public List<UserTree> getAllUserTrees()
    {
        List<UserTree> userTrees= new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_USER_TREES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                UserTree data= new UserTree(cursor.getString(0),cursor.getInt(1),cursor.getInt(2));
                userTrees.add(data);

            } while (cursor.moveToNext());
        }
        // return list
        return userTrees;
    }
    public void truncateUserTreeTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_TREES,null,null);
    }


}
