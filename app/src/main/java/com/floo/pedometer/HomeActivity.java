package com.floo.pedometer;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transition.Visibility;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
    ImageButton chartButton,seedButton,homeButton;
    Spinner spinner;
    MediaPlayer mp;
    DatabaseHandler db;
    MyTextView totalTime,textHome,tm;
    TextView syncInfo,pullInfo,adviceMessage;
    ArrayAdapter<String>menuDrop;
    BluetoothDevice device;
    String deviceName;
    SwipeRefreshLayout swipeLayout;
    BluetoothDataService bluetoothDataService;
    String lastSync;
    UserPreferences userPreferences;

    RelativeLayout RBLeft,RBRight,RBCenter;
    LinearLayout linerHead,chartWrapper;

    final int REQUEST_NEW_BT=1;
    //Your activity will respond to this action String
    public static final String RECEIVE_UPDATE = "com.floo.pedometer.RECEIVE_UPDATE";

    LocalBroadcastManager bManager;

    //int todayMinutes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressBar = (ProgressBar) findViewById(R.id.myProgress);
        chartButton = (ImageButton) findViewById(R.id.chartButton);
        seedButton = (ImageButton) findViewById(R.id.seedButton);
        homeButton = (ImageButton) findViewById(R.id.home);

        totalTime = (MyTextView) findViewById(R.id.progressText);
        textHome = (MyTextView) findViewById(R.id.textHome);
        tm = (MyTextView) findViewById(R.id.tm);

        spinner = (Spinner) findViewById(R.id.spinnerDevices);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        menuDrop = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_spinner_item);
        userPreferences = new UserPreferences(HomeActivity.this);
        syncInfo = (TextView) findViewById(R.id.syncInfo);
        pullInfo = (TextView) findViewById(R.id.pullInfo);
        adviceMessage = (TextView) findViewById(R.id.adviceMessage);


        linerHead = (LinearLayout)findViewById(R.id.linearTop);
        chartWrapper = (LinearLayout)findViewById(R.id.linearWrapper);

        RBLeft = (RelativeLayout)findViewById(R.id.RBLeft);
        RBRight = (RelativeLayout)findViewById(R.id.RBRight);
        RBCenter = (RelativeLayout)findViewById(R.id.RBCenter);


        deviceName = userPreferences.getUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME);
        lastSync = userPreferences.getUserPreferences(UserPreferences.KEY_LAST_SYNC);
        Log.e("lastsyncpref", lastSync);
        if(lastSync.equals(""))
        {
            //lastSync = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            lastSync = "2015-06-15 10:15:00";
            userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC,lastSync);
        }
        syncInfo.setText("Last Update: " + lastSync);

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_UPDATE);
        bManager.registerReceiver(bReceiver, intentFilter);

        swipeLayout.setOnRefreshListener(HomeActivity.this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        if(deviceName!=null||deviceName.equals(""))
            menuDrop.add(deviceName);
        menuDrop.add("Pair with new watch");
        menuDrop.notifyDataSetChanged();

        spinner.setAdapter(menuDrop);

        device = getIntent().getExtras().getParcelable("selectedDevice");

        Intent service = new Intent(this,MyService.class);
        startService(service);

        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
                onRefresh();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String menu = parent.getItemAtPosition(position).toString();
                if (menu.equals("Pair with new watch")) {
                    Intent i = new Intent(HomeActivity.this, BluetoothActivity.class);
                    i.putExtra("message", "change");
                    startActivityForResult(i, REQUEST_NEW_BT);

                    //startactivity result
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        db = DatabaseHandler.getInstance(HomeActivity.this);
        progressBar.setRotation(135);

        List<OutdoorData> datas =  db.getUnsyncOutdoorsDatas();
        for(OutdoorData data:datas){
            Log.e("unsync",data.getId()+" "+data.getTimeStamp()+" "+data.getMinutes()+" "+data.getFlag());
        }

        if(!MainActivity.ALLOW_CHANGE_DEVICE)
        {
            linerHead.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,80);
            chartWrapper.setLayoutParams(params);

        }

        //int todayMinutes = db.getTodayMinutes();
        //Log.e("minute", Integer.toString(todayMinutes));
        //int todayMinutes = 188;
        //startProgressAnim(todayMinutes);

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

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RBLeft.setVisibility(View.VISIBLE);
                RBRight.setVisibility(View.VISIBLE);
                RBCenter.setVisibility(View.GONE);
                pullInfo.setVisibility(View.VISIBLE);
                textHome.setVisibility(View.VISIBLE);
                tm.setVisibility(View.VISIBLE);
                syncInfo.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams params;
                if(MainActivity.ALLOW_CHANGE_DEVICE) {
                    params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 75);
                    linerHead.setVisibility(View.VISIBLE);
                    chartWrapper.setLayoutParams(params);
                }
                adviceMessage.setVisibility(View.GONE);
            }
        });


        chartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SoundPlayer().execute();

                if(db.getUserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID))==null)
                {
                    db.addUserBadgeData(new UserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID), 0, 0));
                }


                Intent i = new Intent(HomeActivity.this,ChartActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_NEW_BT)
        {
            if(resultCode==RESULT_OK){
                Log.e("result","ok");
                device = data.getExtras().getParcelable("selectedDevice");
                menuDrop = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_spinner_item);
                deviceName = userPreferences.getUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME);
                if(deviceName!=null||deviceName.equals(""))
                    menuDrop.add(deviceName);
                menuDrop.add("Pair with new watch");
                menuDrop.notifyDataSetChanged();
                spinner.setAdapter(menuDrop);
            }
        }
    }
    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(RECEIVE_UPDATE)) {
                lastSync = intent.getExtras().getString("latestUpdate");
                syncInfo.setText("Last Updated: "+lastSync);
                //Do something with the string
            }
        }
    };

    void startProgressAnim(int _todayMinutes){
        if(_todayMinutes<180)
        {
            double progressValue = ((double)_todayMinutes/(double)180)*150;
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "secondaryProgress", 1, (int)Math.round(progressValue));
            animation.setDuration(2000); //in milliseconds
            animation.start();
        }
        else
        {
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "secondaryProgress", 1, 150);
            animation.setDuration(1000); //in milliseconds
            animation.start();

            double progressValue = ((double)_todayMinutes/(double)180)*150;

            ObjectAnimator animation2 = ObjectAnimator.ofInt(progressBar, "progress", 1, (int)Math.round(progressValue));
            animation2.setDuration(2000); //in milliseconds
            animation2.start();
        }
        totalTime.setText(Integer.toString(_todayMinutes / 60) + "h " + Integer.toString(_todayMinutes % 60) + "m");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_NOT_RUNNING);
        bManager.unregisterReceiver(bReceiver);
        bluetoothDataService.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_NOT_RUNNING);

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
                        CalculateBadge calculateBadge = new CalculateBadge(HomeActivity.this,lastSync);
                        calculateBadge.execute();
                        userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC, latestDate);
                        lastSync = latestDate;
                        int todayMinutes =db.getTodayMinutes();
                        startProgressAnim(todayMinutes);
                        if(todayMinutes<=180)
                        {
                            RBLeft.setVisibility(View.GONE);
                            RBRight.setVisibility(View.GONE);
                            RBCenter.setVisibility(View.VISIBLE);
                            linerHead.setVisibility(View.GONE);
                            pullInfo.setVisibility(View.GONE);
                            syncInfo.setVisibility(View.GONE);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,80);
                            chartWrapper.setLayoutParams(params);
                            adviceMessage.setVisibility(View.VISIBLE);
                            textHome.setVisibility(View.GONE);
                            tm.setVisibility(View.GONE);

                        }

                        List<OutdoorData>unSyncData = db.getUnsyncOutdoorsDatas();
                        PushToServer pushToServer = new PushToServer(HomeActivity.this,userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID),"myPhoneID");//replace 1 with userID from login
                        pushToServer.setUnsyncDatas(unSyncData);
                        pushToServer.execute();

                        Toast.makeText(HomeActivity.this, "Updated "+lastSync,Toast.LENGTH_LONG).show();
                        syncInfo.setText("Last Updated: "+lastSync);

                        //        bluetoothDataService.stop();
                    }
                    else
                    {
                        Toast.makeText(HomeActivity.this, "no new data",Toast.LENGTH_LONG).show();
                        syncInfo.setText("Updated Just Now");
                    }
                    Log.e("handler", "done reading");

                    swipeLayout.setRefreshing(false);
                    break;
                //if success thread
                //set last sync
                //queary data
                //push to server

                case BluetoothDataService.FAILED:
                    Log.e("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    Toast.makeText(HomeActivity.this, msg.getData().getString(BluetoothDataService.MESSAGE), Toast.LENGTH_LONG);
                    syncInfo.setText("Last Update: " + lastSync);

                    swipeLayout.setRefreshing(false);
                    AlertDialog.Builder builder =  new AlertDialog.Builder(HomeActivity.this);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setTitle("Oooppss!!");
                    alertDialog.setMessage("Something's wrong\nPlease try again");
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();

//                    bluetoothDataService.stop();

                    Log.e("handler", "failed");
                    break;

                //if failed
                //keluar sync failed
                case BluetoothDataService.STOPPED:
                    Log.e("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    Toast.makeText(HomeActivity.this, msg.getData().getString(BluetoothDataService.MESSAGE), Toast.LENGTH_LONG);
                    syncInfo.setText("Last Update: "+lastSync);

                    swipeLayout.setRefreshing(false);
//                    bluetoothDataService.stop();

                    Log.e("handler","stopped");
                    break;
            }
        }
    };

    @Override
    public void onRefresh() {
        //swipeLayout.setRefreshing(true);
        syncInfo.setText("Syncing...");
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
    protected void onStart() {
        super.onStart();
        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_RUNNING);
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
