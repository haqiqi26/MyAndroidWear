package com.floo.pedometer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class MainActivity extends ActionBarActivity {
    DatabaseHandler db;
    Button login;
    List<OutdoorData>outdoorDatas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = DatabaseHandler.getInstance(this);
        Random rand = new Random();
        int x = rand.nextInt(150)+150;
        db.addDataToday(x);

        String dateNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        outdoorDatas = db.getAllOutdoorsDatas();

        for(OutdoorData outdoorData:outdoorDatas)
        {
            String log = outdoorData.getTimeStamp()+" "+outdoorData.getMinutes();

            Log.e("data", log);

            if(outdoorData.getTimeStamp().equals(dateNow))
            {
                Log.e("data", "today progress: "+ outdoorData.getMinutes());

            }
        }
        Log.e("minutes", "data: " + db.getTodayMinutes());
        login = (Button) findViewById(R.id.loginButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,HomeActivity.class);
                startActivity(i);
                createNotification();
                //finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void createNotification() {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(MainActivity.this, CongratsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);


        Notification noti = new Notification.Builder(this)
                .setContentTitle("Congratulations!")
                .setContentText("You Have Won a Badge")
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.home_icon))
                .setContentIntent(pIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        db.truncateTable();
        outdoorDatas.clear();

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
