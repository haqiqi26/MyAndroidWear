package com.floo.pedometer;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;


public class CongratsActivity extends ActionBarActivity {

    ImageView homeButton,badge;
    TextView congratsMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congrats);
        homeButton = (ImageView) findViewById(R.id.homeButton);
        badge= (ImageView) findViewById(R.id.badgeImage);
        congratsMessage = (TextView) findViewById(R.id.congratsMessage);

        Random rand = new Random();

        int x = rand.nextInt(10)%2;
        if(x==1){//platinum
            badge.setImageResource(R.drawable.platinum_badge);
            congratsMessage.setText("Congratulations! You Have\nWon a Platinum Badge\nThis Week");
        }
        else{
            badge.setImageResource(R.drawable.gold_badge);
            congratsMessage.setText("Congratulations! You Have\nWon a Gold Badge\nToday");
        }

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CongratsActivity.this,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_congrats, menu);
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
