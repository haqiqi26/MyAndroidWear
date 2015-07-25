package com.floo.pedometer;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class ChartActivity extends ActionBarActivity {

    RelativeLayout layout;
    LinearLayout linearLayout;
    ArrayList<ProgressBar> pb;
    List<OutdoorData> outdoorDataList;
    TextView platinumCount,goldCount;
    Typeface tf;
    ImageView home,seed;
    DatabaseHandler db;
    int myWidth =100;
    int myHeight =100;
    UserPreferences userPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        pb = new ArrayList<ProgressBar>();
        db = DatabaseHandler.getInstance(this);

        layout = (RelativeLayout) findViewById(R.id.relative);
        linearLayout = (LinearLayout) findViewById(R.id.linearChart);
        platinumCount = (TextView) findViewById(R.id.platinumCount);
        goldCount = (TextView) findViewById(R.id.goldCount);

        userPreferences = new UserPreferences(ChartActivity.this);
        UserBadge userBadge = db.getUserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID));
        outdoorDataList  = db.getAllOutdoorsDatas();

        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_RUNNING);

        platinumCount.setText(Integer.toString(userBadge.getPlatinumBadge()));
        goldCount.setText(Integer.toString(userBadge.getGoldBadge()));

        tf = Typeface.createFromAsset(ChartActivity.this.getAssets(),
                "fonts/comic_sans.ttf");
        platinumCount.setTypeface(tf);
        goldCount.setTypeface(tf);


        OutdoorData outdoorData;
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");

        Date dateIterator=null;

        if(outdoorDataList.size()==0)
        {
            db.addOutdoorDataToday(0);
            outdoorDataList  = db.getAllOutdoorsDatas();
            dateIterator = new Date();
        }

        Calendar cal = Calendar.getInstance();



        Date todaysDate = new Date();
        for(int i = 0;i<outdoorDataList.size();i++){

            outdoorData = outdoorDataList.get(i);
            try {
                dateIterator = inFormat.parse(outdoorData.getTimeStamp());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(!outdoorDataList.get(outdoorDataList.size()-1).getTimeStamp().equals(inFormat.format(todaysDate)))
            {
                try {
                    cal.setTime(inFormat.parse(outdoorDataList.get(outdoorDataList.size()-1).getTimeStamp()));
                    cal.add(Calendar.DATE,1);
                    String nextDay = inFormat.format(cal.getTime());
                    outdoorDataList.add(new OutdoorData(nextDay,0));
                    db.addOutdoorData(new OutdoorData(nextDay+" 01:01:01",0,0,0.0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            cal.setTime(dateIterator);
            cal.add(Calendar.DATE,1);
            String followingDay = inFormat.format(cal.getTime());

            if((i+1)<outdoorDataList.size()-1)
            {
                if(!outdoorDataList.get(i+1).getTimeStamp().equals(followingDay)) {
                    OutdoorData temp = new OutdoorData(followingDay, 0);
                    outdoorDataList.add(i+1, temp);
                    db.addOutdoorData(new OutdoorData(followingDay+" 01:01:01",0,0,0.0));
                }
            }
            TextView day = new TextView(ChartActivity.this);
            day.setTypeface(tf);
            //outdoorDataList.

            ProgressBar progressBar = new ProgressBar(ChartActivity.this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(false);
            progressBar.setMax(360);
            progressBar.setProgress(0);
            progressBar.setSecondaryProgress(0);
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress));

            progressBar.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,myHeight);
            params.setMargins(myWidth, (myHeight+10) * i, 0, 0);


            String days="";
            try {
                Date date = inFormat.parse(outdoorData.getTimeStamp());
                days = dayFormat.format(date);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            String myDate[] = outdoorData.getTimeStamp().split("-");
            day.setText(days + "\n" + myDate[2] + "/" + myDate[1]);

            day.setTextSize(10);
            day.setGravity(Gravity.CENTER);
            day.setBackgroundResource(R.color.lightorange);
            day.setTextColor(Color.BLACK);

            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(myWidth,myHeight);
            params2.setMargins(0, (myHeight + 10) * i, 0, 0);

            layout.addView(progressBar, params);
            layout.addView(day, params2);
            pb.add(progressBar);

            if(outdoorData.getMinutes()>180)
            {
                ObjectAnimator animation2 = ObjectAnimator.ofInt(progressBar, "secondaryProgress", 1, 180);
                animation2.setDuration(2000); //in milliseconds
                //animation.setInterpolator(new DecelerateInterpolator());
                animation2.start();

                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 1, outdoorData.getMinutes());
                animation.setDuration(2000); //in milliseconds
                //animation.setInterpolator(new DecelerateInterpolator());
                animation.start();

            }
            else
            {
                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "secondaryProgress", 1, outdoorData.getMinutes());
                animation.setDuration(2000); //in milliseconds
                //animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
            }
        }
        linearLayout.post(new Runnable() {
            @Override
            public void run() {
                Log.d("linear", "width: " + linearLayout.getWidth() + " height:" + linearLayout.getHeight());

                int linearWidth = linearLayout.getWidth();

                int j =0;
                for(OutdoorData outdoorData:outdoorDataList)
                {
                    TextView tv = new TextView(ChartActivity.this);

                    int totalMinutes = outdoorData.getMinutes();
                    tv.setText(totalMinutes / 60 + "h " + totalMinutes % 60 + "m");
                    tv.setTextSize(15);
                    tv.setTextColor(Color.BLACK);
                    tv.setTypeface(tf);

                    double endChartWidth;
                    if(totalMinutes<360)
                        endChartWidth = ((double)totalMinutes/(double)360)*linearWidth;
                    else
                        endChartWidth = linearWidth;

                    RelativeLayout.LayoutParams params3 = new RelativeLayout
                            .LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            myHeight);

                    if(totalMinutes<=90) {
                        params3.setMargins(myWidth + (int) Math.round(endChartWidth), ((myHeight+10) * j)+14, 0, 0);
                        tv.setGravity(Gravity.CENTER_VERTICAL);
                        layout.addView(tv, params3);
                    }
                    else
                    {
                        params3.setMargins(myWidth , ((myHeight+10) * j)+14, linearWidth - (int) Math.round(endChartWidth), 0);
                        tv.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
                        layout.addView(tv, params3);
                    }
                    j++;
                }

            }
        });
        home = (ImageView)findViewById(R.id.home);
        seed = (ImageView)findViewById(R.id.seedButton);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ButtonSound(ChartActivity.this).execute();
                Intent i = new Intent(ChartActivity.this,HomeActivity.class);
                startActivity(i);
                finish();
            }
        });
        seed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ButtonSound(ChartActivity.this).execute();
                Intent i = new Intent(ChartActivity.this, SeedActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_NOT_RUNNING);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        //db.truncateOutdoorTable();
        //db.truncateUserBadgeTable();
        this.finish();
        /*overridePendingTransition(
                getIntent().getIntExtra("anim id in", R.anim.left_in),
                getIntent().getIntExtra("anim id out", R.anim.left_out));*/
    }
}
