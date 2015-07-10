package com.floo.pedometer;

import com.baoyz.widget.PullRefreshLayout;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class HomeActivity extends ActionBarActivity {

    ProgressBar progressBar;
    ImageButton chartButton,seedButton,homeButton;
    Spinner spinner;
    MediaPlayer mp;
    DatabaseHandler db;
    MyTextView totalTime,textHome,tm;
    TextView syncInfo,pullInfo,adviceMessage,syncProgress;
    ArrayAdapter<String>menuDrop;
    BluetoothDevice device;
    String deviceName;
    PullRefreshLayout swipeLayout;
    BluetoothDataService bluetoothDataService;
    String lastSync;
    UserPreferences userPreferences;

    RelativeLayout RBLeft,RBRight,RBCenter;
    LinearLayout linerHead,chartWrapper;

    final int REQUEST_NEW_BT=1;
    //Your activity will respond to this action String
    //public static final String RECEIVE_UPDATE = "com.floo.pedometer.RECEIVE_UPDATE";

    //LocalBroadcastManager bManager;

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
        swipeLayout = (PullRefreshLayout) findViewById(R.id.swipeRefresh);
        menuDrop = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_spinner_item);
        userPreferences = new UserPreferences(HomeActivity.this);
        syncInfo = (TextView) findViewById(R.id.syncInfo);
        syncProgress = (TextView) findViewById(R.id.syncProgress);
        pullInfo = (TextView) findViewById(R.id.pullInfo);
        adviceMessage = (TextView) findViewById(R.id.adviceMessage);
        db = DatabaseHandler.getInstance(HomeActivity.this);



        linerHead = (LinearLayout)findViewById(R.id.linearTop);
        chartWrapper = (LinearLayout)findViewById(R.id.linearWrapper);

        RBLeft = (RelativeLayout)findViewById(R.id.RBLeft);
        RBRight = (RelativeLayout)findViewById(R.id.RBRight);
        RBCenter = (RelativeLayout)findViewById(R.id.RBCenter);


        deviceName = userPreferences.getUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME);
        lastSync = userPreferences.getUserPreferences(UserPreferences.KEY_LAST_SYNC);
        if(lastSync.equals(""))
        {
            //lastSync = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            lastSync = MainActivity.FIRST_SYNC_TIME;
            userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC,lastSync);
        }
        syncInfo.setText("Last Update: " + lastSync);


        /*swipeLayout.setOnRefreshListener(HomeActivity.this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        */

        if(deviceName!=null||deviceName.equals(""))
            menuDrop.add(deviceName);
        menuDrop.add("Pair with new watch");
        menuDrop.notifyDataSetChanged();

        spinner.setAdapter(menuDrop);

        device = getIntent().getExtras().getParcelable("selectedDevice");


        swipeLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
                doRefresh();
            }
        }, 3000);

        swipeLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });
        swipeLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING);
/*
        Intent service = new Intent(this,MyService.class);
        startService(service);

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_UPDATE);
        bManager.registerReceiver(bReceiver, intentFilter);
*/
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
        progressBar.setRotation(135);

        if(!MainActivity.ALLOW_CHANGE_DEVICE)
        {
            linerHead.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,80);
            chartWrapper.setLayoutParams(params);

        }
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
                Log.d("result","ok");
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

    /*
    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(RECEIVE_UPDATE)) {
                lastSync = intent.getExtras().getString("latestUpdate");
                syncInfo.setText("Last Updated: "+lastSync);
                int todayMinutes =db.getTodayMinutes();
                startProgressAnim(todayMinutes);
                //Do something with the string
            }
        }
    };*/

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
        //bManager.unregisterReceiver(bReceiver);
        bluetoothDataService.stop();
    }

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int todayMinutes;
            switch (msg.what)
            {
                case BluetoothDataService.DONE_READING:
                    String latestDate = msg.getData().getString(BluetoothDataService.MESSAGE);
                    todayMinutes =db.getTodayMinutes();
                    startProgressAnim(todayMinutes);



                    if(!latestDate.equals(""))
                    {
                        CalculateBadge calculateBadge = new CalculateBadge(HomeActivity.this, lastSync);
                        calculateBadge.execute();
                        userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC, latestDate);
                        lastSync = latestDate;
                        if(todayMinutes<=180)
                        {
                            if(todayMinutes<=120)
                                adviceMessage.setText("Try harder.\nGo to the park tomorrow\nto hit your target\n");
                            else
                                adviceMessage.setText("Keep it up! Try harder.\nYou can hit\nthe target tomorrow\n");
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


                        Toast.makeText(HomeActivity.this, "Updated "+lastSync,Toast.LENGTH_LONG).show();
                        syncInfo.setText("Last Updated: "+lastSync);

                        //        bluetoothDataService.stop();
                    }
                    else
                    {
                        Toast.makeText(HomeActivity.this, "no new data",Toast.LENGTH_LONG).show();
                        syncInfo.setText("Updated Just Now");
                    }
                    Log.d("handler", "done reading");
                    List<OutdoorData>unSyncData = db.getUnsyncOutdoorsDatas();
                    if(unSyncData.size()>0) {
                        PushToServer pushToServer = new PushToServer(HomeActivity.this, userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID), "myPhoneID");//replace 1 with userID from login
                        pushToServer.setUnsyncDatas(unSyncData);
                        pushToServer.execute();
                    }
                    syncProgress.setText("");
                    swipeLayout.setRefreshing(false);
                    break;
                //if success thread
                //set last sync
                //queary data
                //push to server

                case BluetoothDataService.FAILED:
                    Log.d("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    Toast.makeText(HomeActivity.this, msg.getData().getString(BluetoothDataService.MESSAGE), Toast.LENGTH_LONG);
                    syncInfo.setText("Last Update: " + lastSync);
                    syncProgress.setText("");
                    todayMinutes =db.getTodayMinutes();
                    startProgressAnim(todayMinutes);

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
                    alertDialog.setMessage("The watch might be powered off or out of range\nPlease turn on the watch, bring it nearby and try again");
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                    // Must call show() prior to fetching text view
                    TextView messageView = (TextView)alertDialog.findViewById(android.R.id.message);
                    messageView.setGravity(Gravity.CENTER);

//                    bluetoothDataService.stop();

                    Log.d("handler", "failed");
                    break;

                //if failed
                //keluar sync failed
                case BluetoothDataService.STOPPED:
                    Log.d("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    Toast.makeText(HomeActivity.this, msg.getData().getString(BluetoothDataService.MESSAGE), Toast.LENGTH_LONG);
                    syncInfo.setText("Last Update: " + lastSync);
                    syncProgress.setText("");
                    todayMinutes =db.getTodayMinutes();
                    startProgressAnim(todayMinutes);


                    swipeLayout.setRefreshing(false);
//                    bluetoothDataService.stop();

                    Log.d("handler","stopped");
                    break;
                case BluetoothDataService.READING_PROGRESS:
                    syncInfo.setText(msg.getData().getString(BluetoothDataService.MESSAGE));
                    Log.d("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    break;

            }
        }
    };

    public void doRefresh() {
        //swipeLayout.setRefreshing(true);
        syncInfo.setText("Syncing...");
        bluetoothDataService = new BluetoothDataService(HomeActivity.this,mHandler);
        Log.d("lastsyncpref", lastSync);
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
