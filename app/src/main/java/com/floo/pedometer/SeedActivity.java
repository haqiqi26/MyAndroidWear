package com.floo.pedometer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SeedActivity extends ActionBarActivity {

    ImageView backText,seedImage;
    LinearLayout imageWrapper;
    DatabaseHandler db;
    UserPreferences userPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seed);
        backText = (ImageView) findViewById(R.id.seedBackButton);
        seedImage = (ImageView) findViewById(R.id.seedImage);
        imageWrapper = (LinearLayout) findViewById(R.id.imageWrapper);
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        db = DatabaseHandler.getInstance(SeedActivity.this);
        userPreferences = new UserPreferences(SeedActivity.this);
        String id = userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID);
        UserTree tree = db.getUserTree(id);
        if(tree==null)
        {
            db.addUserTreeData(new UserTree(id,4,1));
            tree = db.getUserTree(id);
        }
        for(int i=0;i<tree.getTreesCompleted();i++)
        {
            addImageTrees(R.drawable.trees_grown);
        }
        int lastState = tree.getLastTreeProgress();

        if(lastState==1)
        {
            addImageTrees(R.drawable.seed2);
            seedImage.setImageResource(R.drawable.seed2);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seed, menu);
        return true;
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
