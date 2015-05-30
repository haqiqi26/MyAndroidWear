package com.floo.pedometer;

import android.animation.ObjectAnimator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class ChartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
       // setContentView(R.layout.graph_image);

        String days[] = {"M","T","W","Th","F","S","Su"};
        ArrayList<ProgressBar> pb = new ArrayList<>();

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative);

        Random rand = new Random();

        for(int i=0;i<10;i++) {

            TextView tv = new TextView(ChartActivity.this);
            TextView day = new TextView(ChartActivity.this);

            ProgressBar progressBar = new ProgressBar(ChartActivity.this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(false);
            progressBar.setMax(600);
            progressBar.setProgress(0);
            progressBar.setSecondaryProgress(0);
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress));

            progressBar.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,50);
            params.setMargins(0, 70 * i, 0, 0);

            int x = rand.nextInt(600);
            tv.setText(x + " M");
            tv.setTextSize(15);
            tv.setGravity(Gravity.CENTER);

            day.setText(days[x % 7]);
            day.setTextSize(15);
            day.setGravity(Gravity.CENTER);
            day.setBackgroundResource(R.color.light_green);

            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(50,50);
            params2.setMargins(0, 70 * i, 0, 0);


            layout.addView(progressBar, params);
            layout.addView(tv, params);

            layout.addView(day, params2);

            pb.add(progressBar);
            if(x>300)
            {
                ObjectAnimator animation2 = ObjectAnimator.ofInt(progressBar, "secondaryProgress", 1, 300);
                animation2.setDuration(2000); //in milliseconds
                //animation.setInterpolator(new DecelerateInterpolator());
                animation2.start();

                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 1, x);
                animation.setDuration(2000); //in milliseconds
                //animation.setInterpolator(new DecelerateInterpolator());
                animation.start();

            }
            else
            {
                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "secondaryProgress", 1, x);
                animation.setDuration(2000); //in milliseconds
                //animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
            }


        }
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
        this.finish();
        overridePendingTransition(
                getIntent().getIntExtra("anim id in", R.anim.left_in),
                getIntent().getIntExtra("anim id out", R.anim.left_out));
    }
}
