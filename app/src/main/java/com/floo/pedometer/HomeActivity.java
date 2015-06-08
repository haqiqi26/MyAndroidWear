package com.floo.pedometer;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;



public class HomeActivity extends ActionBarActivity {

    ProgressBar progressBar;
    ImageButton chartButton,seedButton;
    MediaPlayer mp;
    DatabaseHandler db;
    MyTextView totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_kit);
        //setContentView(R.layout.home_image);

        progressBar = (ProgressBar) findViewById(R.id.myProgress);
        chartButton = (ImageButton) findViewById(R.id.chartButton);
        //centerButton= (ImageButton) findViewById(R.id.centerButton);
        seedButton = (ImageButton) findViewById(R.id.seedButton);
        totalTime = (MyTextView) findViewById(R.id.progressText);

        db = DatabaseHandler.getInstance(this);
        progressBar.setRotation(135);

        int todayMinutes = db.getTodayMinutes();
        Log.e("minute", Integer.toString(todayMinutes));
        //int todayMinutes = 188;

        if(todayMinutes<180)
        {
            double progressValue = ((double)todayMinutes/(double)180)*150;
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "secondaryProgress", 1, (int)Math.round(progressValue));
            animation.setDuration(2000); //in milliseconds
            //animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
        else
        {
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "secondaryProgress", 1, 150);
            animation.setDuration(1000); //in milliseconds
            //animation.setInterpolator(new DecelerateInterpolator());
            animation.start();

            double progressValue = ((double)todayMinutes/(double)180)*150;

            ObjectAnimator animation2 = ObjectAnimator.ofInt(progressBar, "progress", 1, (int)Math.round(progressValue));
            animation2.setDuration(2000); //in milliseconds
            //animation2.setInterpolator(new DecelerateInterpolator());
            animation2.start();
        }

        totalTime.setText(Integer.toString(todayMinutes / 60)+"h "+Integer.toString(todayMinutes%60)+"m");
        seedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SoundPlayer().execute();
                Intent i = new Intent(HomeActivity.this,SeedActivity.class);
                //i.putExtra("anim id in", R.anim.up_in);
                //i.putExtra("anim id out", R.anim.up_out);
                startActivity(i);
                //overridePendingTransition(R.anim.down_in, R.anim.down_out);

            }
        });


        chartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SoundPlayer().execute();

                db.addUserBadgeData(new UserBadge("john", 0, 12));
                db.addUserBadgeData(new UserBadge("jane", 2, 10));
                db.addUserBadgeData(new UserBadge("jack",4,5));

                db.addOutdoorDataToday(5);
                db.addOutdoorDataToday(10);
                db.addOutdoorDataToday(50);

                db.addOutdoorData(new OutdoorData("2015-06-08 11:11:11", 18));
                db.addOutdoorData(new OutdoorData("2015-06-07 11:11:11",50));
                db.addOutdoorData(new OutdoorData("2015-06-08 11:11:11",39));
                db.addOutdoorData(new OutdoorData("2015-06-06 11:11:11",55));
                db.addOutdoorData(new OutdoorData("2015-06-01 11:11:11",88));
                db.addOutdoorData(new OutdoorData("2015-06-02 11:11:11",91));
                db.addOutdoorData(new OutdoorData("2015-04-08 11:11:11",12));
                db.addOutdoorData(new OutdoorData("2015-02-08 11:11:11",355));
                db.addOutdoorData(new OutdoorData("2015-03-08 11:11:11",170));
                db.addOutdoorData(new OutdoorData("2015-05-08 11:11:11",150));



                Intent i = new Intent(HomeActivity.this,ChartActivity.class);
                //i.putExtra("anim id in", R.anim.up_in);
                //i.putExtra("anim id out", R.anim.up_out);
                startActivity(i);
                //overridePendingTransition(R.anim.down_in, R.anim.down_out);
            }
        });
    }

    private class SoundPlayer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mp = MediaPlayer.create(HomeActivity.this, Uri.parse("/system/media/audio/ui/Effect_Tick.ogg"));
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }

            });
            mp.start();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview_kit, menu);
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
}
