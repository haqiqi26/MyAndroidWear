package com.floo.pedometer;

import android.animation.ObjectAnimator;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ChartActivity extends ActionBarActivity {

    RelativeLayout layout;
    LinearLayout linearLayout;
    ArrayList<ProgressBar> pb;
    List<OutdoorData> outdoorDataList;
    TextView platinumCount,goldCount;
    Typeface tf;
    DatabaseHandler db;
    int myWidth =100;
    int myHeight =100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
       // setContentView(R.layout.graph_image);

        String days[] = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        pb = new ArrayList<ProgressBar>();
        db = DatabaseHandler.getInstance(this);

        layout = (RelativeLayout) findViewById(R.id.relative);
        linearLayout = (LinearLayout) findViewById(R.id.linearChart);
        platinumCount = (TextView) findViewById(R.id.platinumCount);
        goldCount = (TextView) findViewById(R.id.goldCount);

        UserPreferences userPreferences = new UserPreferences(ChartActivity.this);
        UserBadge userBadge = db.getUserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID));
        outdoorDataList  = db.getAllOutdoorsDatas();


        platinumCount.setText(Integer.toString(userBadge.getPlatinumBadge()));
        goldCount.setText(Integer.toString(userBadge.getGoldBadge()));

        tf = Typeface.createFromAsset(ChartActivity.this.getAssets(),
                "fonts/comic_sans.ttf");
        platinumCount.setTypeface(tf);
        goldCount.setTypeface(tf);

        Random rand = new Random();
        int i=0;
        for(OutdoorData outdoorData:outdoorDataList) {


            TextView day = new TextView(ChartActivity.this);
            day.setTypeface(tf);

            ProgressBar progressBar = new ProgressBar(ChartActivity.this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(false);
            progressBar.setMax(360);
            progressBar.setProgress(0);
            progressBar.setSecondaryProgress(0);
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress));

            progressBar.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,myHeight);
            params.setMargins(myWidth, (myHeight+10) * i, 0, 0);

            int x = rand.nextInt(360);
            //val.add(x);

            String myDate[] = outdoorData.getTimeStamp().split("-");
            day.setText(days[x % 7] + "\n" + myDate[2] + "/" + myDate[1]);

            day.setTextSize(10);
            day.setGravity(Gravity.CENTER);
            day.setBackgroundResource(R.color.lightorange);
            day.setTextColor(Color.BLACK);

            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(myWidth,myHeight);
            params2.setMargins(0, (myHeight+10) * i, 0, 0);

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
            i++;
        }
        linearLayout.post(new Runnable() {
            @Override
            public void run() {
                Log.e("linear", "width: " + linearLayout.getWidth() + " height:" + linearLayout.getHeight());

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

                    double endChartWidth = ((double)totalMinutes/(double)360)*linearWidth;
                    RelativeLayout.LayoutParams params3 = new RelativeLayout
                            .LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            myHeight);

                    if(totalMinutes<=90) {
                        params3.setMargins(myWidth + (int) Math.round(endChartWidth), (myHeight+14) * j, 0, 0);
                        tv.setGravity(Gravity.CENTER_VERTICAL);
                        layout.addView(tv, params3);
                    }
                    else if(totalMinutes>=350)
                    {
                        params3.setMargins(myWidth , (myHeight+14) * j, linearWidth - (int) Math.round(endChartWidth)-100, 0);
                        tv.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
                        layout.addView(tv, params3);
                    }
                    else
                    {
                        params3.setMargins(myWidth , (myHeight+14) * j, linearWidth - (int) Math.round(endChartWidth), 0);
                        tv.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
                        layout.addView(tv, params3);
                    }
                    j++;
                }

            }
        });
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

        db.truncateOutdoorTable();
        db.truncateUserBadgeTable();
        this.finish();
        /*overridePendingTransition(
                getIntent().getIntExtra("anim id in", R.anim.left_in),
                getIntent().getIntExtra("anim id out", R.anim.left_out));*/
    }
}
