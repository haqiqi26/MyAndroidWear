package com.floo.pedometer;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class HomeActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    ProgressBar progressBar;
    ImageButton chartButton,seedButton;
    Spinner spinner;
    MediaPlayer mp;
    DatabaseHandler db;
    MyTextView totalTime;
    ArrayAdapter<String>menuDrop;
    BluetoothDevice device;
    String deviceName;
    SwipeRefreshLayout swipeLayout;
    BluetoothDataService bluetoothDataService;
    String lastSync;
    UserPreferences userPreferences;
    //int todayMinutes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressBar = (ProgressBar) findViewById(R.id.myProgress);
        chartButton = (ImageButton) findViewById(R.id.chartButton);
        seedButton = (ImageButton) findViewById(R.id.seedButton);
        totalTime = (MyTextView) findViewById(R.id.progressText);
        spinner = (Spinner) findViewById(R.id.spinnerDevices);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        menuDrop = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_spinner_item);
        userPreferences = new UserPreferences(HomeActivity.this);
        deviceName = userPreferences.getUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME);
        lastSync = userPreferences.getUserPreferences(UserPreferences.KEY_LAST_SYNC);
        if(lastSync.equals(""))
        {
            lastSync = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC,lastSync);
        }
        //lastSync = "2015-06-15 10:15:00";

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //swipeLayout.setRefreshing(true);

        if(deviceName!=null||deviceName.equals(""))
            menuDrop.add(deviceName);
        menuDrop.add("Pair with new watch");
        menuDrop.notifyDataSetChanged();

        spinner.setAdapter(menuDrop);

        device = getIntent().getExtras().getParcelable("selectedDevice");

        bluetoothDataService = new BluetoothDataService(HomeActivity.this,mHandler);
        bluetoothDataService.setLastSync(lastSync);
        bluetoothDataService.connect(device,true);

        db = DatabaseHandler.getInstance(this);
        progressBar.setRotation(135);

        int todayMinutes = db.getTodayMinutes();
        Log.e("minute", Integer.toString(todayMinutes));
        //int todayMinutes = 188;
        startProgressAnim(todayMinutes);

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

                if(db.getUserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID))==null)
                {
                    db.addUserBadgeData(new UserBadge("1", 0, 12));
                    db.addUserBadgeData(new UserBadge("2", 2, 10));
                    db.addUserBadgeData(new UserBadge("3",4,5));
                }

                if(db.getAllOutdoorsDatas().size()==0)
                {
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
                }


                Intent i = new Intent(HomeActivity.this,ChartActivity.class);
                //i.putExtra("anim id in", R.anim.up_in);
                //i.putExtra("anim id out", R.anim.up_out);
                startActivity(i);
                //overridePendingTransition(R.anim.down_in, R.anim.down_out);
            }
        });
    }
    void startProgressAnim(int _todayMinutes){
        if(_todayMinutes<180)
        {
            double progressValue = ((double)_todayMinutes/(double)180)*150;
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

            double progressValue = ((double)_todayMinutes/(double)180)*150;

            ObjectAnimator animation2 = ObjectAnimator.ofInt(progressBar, "progress", 1, (int)Math.round(progressValue));
            animation2.setDuration(2000); //in milliseconds
            //animation2.setInterpolator(new DecelerateInterpolator());
            animation2.start();
        }
        totalTime.setText(Integer.toString(_todayMinutes / 60)+"h "+Integer.toString(_todayMinutes%60)+"m");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothDataService.stop();

    }

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case BluetoothDataService.DONE_READING:
                    String latestDate = msg.getData().getString(BluetoothDataService.MESSAGE);
                    if(!latestDate.equals(""))
                    {
                        userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC, latestDate);
                        lastSync = latestDate;
                        startProgressAnim(db.getTodayMinutes());
                        List<OutdoorData>unSyncData = db.getUnsyncOutdoorsDatas();
                        PushToServer pushToServer = new PushToServer(HomeActivity.this,userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID),"myPhoneID");//replace 1 with userID from login
                        pushToServer.setUnsyncDatas(unSyncData);
                        pushToServer.execute();
                    }
                    break;
                //if success thread
                //set last sync
                //queary data
                //push to server

                case BluetoothDataService.FAILED:
                    Log.e("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    break;

                //if failed
                //keluar sync failed
                case BluetoothDataService.STOPPED:
                    Log.e("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    break;
            }
            swipeLayout.setRefreshing(false);
            bluetoothDataService.stop();
        }
    };

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        bluetoothDataService = new BluetoothDataService(HomeActivity.this,mHandler);
        bluetoothDataService.setLastSync(lastSync);
        bluetoothDataService.connect(device,true);
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
