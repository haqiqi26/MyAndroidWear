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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
        intent.setAction(Long.toString(System.currentTimeMillis()));
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Congratulations!")
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pIntent);
        if(type==GOLD){
            builder.setContentText("You Have Won a Gold Badge")
                    .setSmallIcon(R.drawable.gold_notif_back);
        }
        else if(type==PLATINUM){
            builder.setContentText("You Have Won a Platinum Badge")
                    .setSmallIcon(R.drawable.platinum_notif_back);
        }
        Notification noti = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        notificationManager.notify(m, noti);

    }


    @Override
    protected String doInBackground(Void... params) {
        db = DatabaseHandler.getInstance(context);
        UserPreferences pref = new UserPreferences(context);
        List<OutdoorData> datas = db.getWhereAllOutdoorsDatas(lastSync.split(" ")[0]);
        String lastGoldBadgeDate = pref.getUserPreferences(UserPreferences.KEY_LAST_GOLD_BADGE_DATE);
        String lastPlatinumBadgeDate=pref.getUserPreferences(UserPreferences.KEY_LAST_PLATINUM_BADGE_DATE);

        String prevGoldBadgeDate = lastGoldBadgeDate;
        String prevPlatinumBadgeDate = lastPlatinumBadgeDate;

        String userID = pref.getUserPreferences(UserPreferences.KEY_USER_ID);
        int goldCounter=0;
        if(!pref.getUserPreferences(UserPreferences.KEY_COUNT_GOLD).equals(""))
            goldCounter = Integer.parseInt(pref.getUserPreferences(UserPreferences.KEY_COUNT_GOLD));



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
            if(data.getMinutes()>180&&lastGoldBadgeDate.compareTo(data.getTimeStamp())<0)
            {
                goldCount++;
                lastProgressTree++;
                createNotification(GOLD);

                if(!lastGoldBadgeDate.equals(""))
                {
                    try {
                        Date prev = sdf.parse(lastGoldBadgeDate);
                        Date current = sdf.parse(data.getTimeStamp());

                        cal.setTime(prev);
                        cal.add(Calendar.DATE,1);
                        Date prevAddOne = cal.getTime();

                        if(prevAddOne.compareTo(current)==0)
                            goldCounter++;
                        else
                            goldCounter=1;

                        if(goldCounter>=3)
                        {
                            platinumCount++;
                            createNotification(PLATINUM);
                            lastPlatinumBadgeDate = data.getTimeStamp();
                            goldCounter=0;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
                else
                {
                    goldCounter++;
                    if(goldCounter>=3)
                    {
                        platinumCount++;
                        createNotification(PLATINUM);
                        lastPlatinumBadgeDate = data.getTimeStamp();
                        goldCounter=0;
                    }
                }

                lastGoldBadgeDate=data.getTimeStamp();
            }
        }
        pref.setUserPreferences(UserPreferences.KEY_LAST_GOLD_BADGE_DATE, lastGoldBadgeDate);
        pref.setUserPreferences(UserPreferences.KEY_LAST_PLATINUM_BADGE_DATE, lastPlatinumBadgeDate);
        pref.setUserPreferences(UserPreferences.KEY_COUNT_GOLD,Integer.toString(goldCounter));

        completedTrees=completedTrees+(lastProgressTree/10);
        lastProgressTree = lastProgressTree%10;
        userTree.setTreeCompleted(completedTrees);
        userTree.setLastTreeProgress(lastProgressTree);

        userBadge.setGoldBadge(goldCount);
        userBadge.setPlatinumBadge(platinumCount);
        db.updateUserBadgeData(userBadge);
        db.updateUserTreeData(userTree);

        String dateNow = sdf.format(new Date());

        if(MainActivity.SHOW_BADGE_CONGRAT_EVERY_SYNC) {
            if (lastPlatinumBadgeDate.equals(dateNow))
                return "platinum";
            else if (lastGoldBadgeDate.equals(dateNow))
                return "gold";
            else
                return "";
        }
        else
        {
            if (lastPlatinumBadgeDate.equals(dateNow)&&!lastPlatinumBadgeDate.equals(prevPlatinumBadgeDate))
                return "platinum";
            else if (lastGoldBadgeDate.equals(dateNow)&&!lastGoldBadgeDate.equals(prevGoldBadgeDate))
                return "gold";
            else
                return "";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(!result.equals(""))
        {
            if(result.equals("platinum")){
                Intent goldIntent = new Intent(context,CongratsActivity.class);
                goldIntent.putExtra(BADGE_TYPE,GOLD);
                goldIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //goldIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.getApplicationContext().startActivity(goldIntent);

                Intent intent = new Intent(context,CongratsActivity.class);
                intent.putExtra(BADGE_TYPE, PLATINUM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
            }
            else if(result.equals("gold")){
                Intent intent = new Intent(context,CongratsActivity.class);
                intent.putExtra(BADGE_TYPE,GOLD);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
            }
        }
    }
}
