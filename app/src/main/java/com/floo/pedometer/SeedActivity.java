package com.floo.pedometer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SeedActivity extends ActionBarActivity {

    ImageView backText;
    LinearLayout imageWrapper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seed);
        backText = (ImageView) findViewById(R.id.seedBackButton);
        imageWrapper = (LinearLayout) findViewById(R.id.imageWrapper);
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seed, menu);
        return true;
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
