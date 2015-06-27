package com.floo.pedometer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by SONY_VAIO on 6/27/2015.
 */
public class CalculateBadge extends AsyncTask<Void,Void,String> {
    Context context;
    String lastSync;
    DatabaseHandler db;
    int GOLD=1,PLATINUM=2;
    String BADGE_TYPE = "badgeType";

    public CalculateBadge(Context context,String lastSync)
    {
        this.context = context;
        this.lastSync = lastSync;

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void createNotification(int type) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(context, CongratsActivity.class);

        intent.putExtra(BADGE_TYPE,type);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);


        Notification noti = new Notification.Builder(context)
                .setContentTitle("Congratulations!")
                .setContentText("You Have Won a Badge")
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.home_icon))
                .setContentIntent(pIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);

    }


    @Override
    protected String doInBackground(Void... params) {
        db = DatabaseHandler.getInstance(context);
        UserPreferences pref = new UserPreferences(context);
        List<OutdoorData> datas = db.getWhereAllOutdoorsDatas(lastSync.split(" ")[0]);
        String lastBadgeDate = pref.getUserPreferences(UserPreferences.KEY_LAST_BADGE_DATE);
        String userID = pref.getUserPreferences(UserPreferences.KEY_USER_ID);
        int savedWeek=1;
        int goldWeek=0;
        int lastPlatinumWeek=0;
        if(!pref.getUserPreferences(UserPreferences.KEY_WEEK).equals(""))
            savedWeek = Integer.parseInt(pref.getUserPreferences(UserPreferences.KEY_WEEK));
        if(!pref.getUserPreferences(UserPreferences.KEY_COUNT_GOLD_WEEK).equals(""))
            goldWeek = Integer.parseInt(pref.getUserPreferences(UserPreferences.KEY_COUNT_GOLD_WEEK));
        if(!pref.getUserPreferences(UserPreferences.KEY_WIN_PLATINUM_WEEK).equals(""))
            lastPlatinumWeek = Integer.parseInt(pref.getUserPreferences(UserPreferences.KEY_WIN_PLATINUM_WEEK));

        UserBadge userBadge = db.getUserBadge(userID);
        UserTree userTree = db.getUserTree(userID);
        if(userBadge==null)
        {
            userBadge = new UserBadge(userID,0,0);
            db.addUserBadgeData(userBadge);
        }
        if(userTree==null)
        {
            userTree = new UserTree(userID,0,0);
            db.addUserTreeData(userTree);
        }
        int goldCount = userBadge.getGoldBadge();
        int platinumCount = userBadge.getPlatinumBadge();

        int lastProgressTree = userTree.getLastTreeProgress();
        int completedTrees = userTree.getTreesCompleted();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        for(OutdoorData data:datas)
        {
            if(!lastBadgeDate.equals("")) {
                try {
                    Date prev = sdf.parse(lastBadgeDate);
                    Date current = sdf.parse(data.getTimeStamp());
                    cal.setTime(current);
                    int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);

                    if (prev.before(current)) {
                        if(data.getMinutes()>180){
                            lastBadgeDate = data.getTimeStamp();
                            goldCount++;
                            lastProgressTree++;
                            createNotification(GOLD);
                            if(savedWeek==currentWeek)
                            {
                                goldWeek++;
                                if(goldWeek>3&&lastPlatinumWeek!=currentWeek)
                                {
                                    platinumCount++;
                                    lastPlatinumWeek=currentWeek;
                                }
                            }
                            else
                            {
                                goldWeek=1;
                            }

                        }
                        savedWeek =currentWeek;

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else{
                if(data.getMinutes()>180){
                    lastBadgeDate = data.getTimeStamp();
                    goldCount++;
                    lastProgressTree++;
                    createNotification(GOLD);
                    try {
                        Date current = sdf.parse(lastBadgeDate);
                        cal.setTime(current);
                        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
                        if(savedWeek==currentWeek)
                        {
                            goldWeek++;
                            if(goldWeek>3&&lastPlatinumWeek!=currentWeek)
                            {
                                platinumCount++;
                                lastPlatinumWeek=currentWeek;
                            }
                        }
                        else
                        {
                            goldWeek=1;
                        }
                        savedWeek =currentWeek;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        pref.setUserPreferences(UserPreferences.KEY_LAST_BADGE_DATE,lastBadgeDate);
        pref.setUserPreferences(UserPreferences.KEY_WEEK,Integer.toString(savedWeek));
        pref.setUserPreferences(UserPreferences.KEY_WIN_PLATINUM_WEEK,Integer.toString(lastPlatinumWeek));
        pref.setUserPreferences(UserPreferences.KEY_COUNT_GOLD_WEEK,Integer.toString(goldWeek));

        completedTrees=completedTrees+(lastProgressTree/10);
        lastProgressTree = lastProgressTree%10;
        userTree.setTreeCompleted(completedTrees);
        userTree.setLastTreeProgress(lastProgressTree);

        userBadge.setGoldBadge(goldCount);
        userBadge.setPlatinumBadge(platinumCount);
        db.updateUserBadgeData(userBadge);
        db.updateUserTreeData(userTree);
        return null;
    }
}
