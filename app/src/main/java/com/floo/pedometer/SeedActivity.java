package com.floo.pedometer;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class SeedActivity extends ActionBarActivity {

    ImageView home,seedImage,chart;
    LinearLayout imageWrapper;
    DatabaseHandler db;
    UserPreferences userPreferences;
    int[] seedSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seed);
        home = (ImageView) findViewById(R.id.home);
        chart = (ImageView) findViewById(R.id.chartButton);
        seedImage = (ImageView) findViewById(R.id.seedImage);
        imageWrapper = (LinearLayout) findViewById(R.id.imageWrapper);
        seedSource = new int[]{
                R.drawable.seeds1,
                R.drawable.seeds2,
                R.drawable.seeds3,
                R.drawable.seeds4,
                R.drawable.seeds5,
                R.drawable.seeds6,
                R.drawable.seeds7,
                R.drawable.seeds8,
                R.drawable.seeds9,
                R.drawable.seeds10
        };

        db = DatabaseHandler.getInstance(SeedActivity.this);
        userPreferences = new UserPreferences(SeedActivity.this);
        String id = userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID);
        UserTree tree = db.getUserTree(id);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ButtonSound(SeedActivity.this).execute();
                Intent i = new Intent(SeedActivity.this,HomeActivity.class);
                startActivity(i);
                finish();
            }
        });
        chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ButtonSound(SeedActivity.this).execute();
                if(db.getUserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID))==null)
                {
                    db.addUserBadgeData(new UserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID), 0, 0));
                }
                Intent i = new Intent(SeedActivity.this,ChartActivity.class);
                startActivity(i);
                finish();
            }
        });
        if(tree==null)
        {
            tree = new UserTree(id,0,0);
            db.addUserTreeData(tree);
        }
        for(int i=0;i<tree.getTreesCompleted();i++)
        {
            addImageTrees(seedSource[9]);
        }
        int lastState = tree.getLastTreeProgress();
        addImageTrees(seedSource[lastState]);
        seedImage.setImageResource(seedSource[lastState]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seed, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_RUNNING);
    }

    @Override
    protected void onStop() {
        super.onStop();
        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_NOT_RUNNING);
    }

    void addImageTrees(int resId)
    {
        ImageView treeImage = new ImageView(SeedActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(convertDPtoPX(80),convertDPtoPX(80));
        params.gravity = Gravity.CENTER;
        treeImage.setImageResource(resId);
        treeImage.setLayoutParams(params);
        imageWrapper.addView(treeImage);
    }

    int convertDPtoPX(int dp){
        int px=0;
        DisplayMetrics displayMetrics = SeedActivity.this.getResources().getDisplayMetrics();
        px = (int) Math.round((double)dp * ((double)displayMetrics.densityDpi/(double)DisplayMetrics.DENSITY_DEFAULT));

        return px;
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
