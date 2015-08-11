package com.floo.pedometer;

import com.baoyz.widget.PullRefreshLayout;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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
    DatabaseHandler db;
    MyTextView totalTime,textHome,tm;
    TextView syncInfo,pullInfo,adviceMessage;
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
    BluetoothAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressBar = (ProgressBar) findViewById(R.id.myProgress);
        chartButton = (ImageButton) findViewById(R.id.chartButton);
        seedButton = (ImageButton) findViewById(R.id.seedButton);
        homeButton = (ImageButton) findViewById(R.id.home);

        adapter = BluetoothAdapter.getDefaultAdapter();

        totalTime = (MyTextView) findViewById(R.id.progressText);
        textHome = (MyTextView) findViewById(R.id.textHome);
        tm = (MyTextView) findViewById(R.id.tm);

        spinner = (Spinner) findViewById(R.id.spinnerDevices);
        swipeLayout = (PullRefreshLayout) findViewById(R.id.swipeRefresh);
        menuDrop = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_spinner_item);
        userPreferences = new UserPreferences(HomeActivity.this);
        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_RUNNING);
        Log.e("homePref",userPreferences.getUserPreferences(UserPreferences.KEY_APP_STATE));
        syncInfo = (TextView) findViewById(R.id.syncInfo);
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

        if(deviceName!=null||deviceName.equals(""))
            menuDrop.add(deviceName);
        menuDrop.add("Pair with new watch");
        menuDrop.notifyDataSetChanged();

        spinner.setAdapter(menuDrop);

        if(getIntent().getExtras()!=null) {
            device = getIntent().getExtras().getParcelable("selectedDevice");
            Log.d("device","hello");
        }
        else
        {
            device = adapter.getRemoteDevice(userPreferences.getUserPreferences(UserPreferences.KEY_BLUETOOTH_ADDRESS));
            Log.d("device","null");
        }
        Log.d("device", userPreferences.getUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME));

        progressBar.setRotation(135);
        startProgressAnim(db.getTodayMinutes());

        swipeLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*db.addOutdoorData(new OutdoorData("2015-08-07 10:15:00",181,0,0));
                db.addOutdoorData(new OutdoorData("2015-08-09 10:15:00",181,0,0));
                db.addOutdoorData(new OutdoorData("2015-08-10 10:15:00",181,0,0));
                db.addOutdoorData(new OutdoorData("2015-08-11 10:15:00",181,0,0));
*/
                userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_RUNNING);
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

        if(!MainActivity.ALLOW_CHANGE_DEVICE)
        {
            linerHead.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,85);
            chartWrapper.setLayoutParams(params);

        }
        seedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothDataService!=null)
                    bluetoothDataService.stop();
                new ButtonSound(HomeActivity.this).execute();
                Intent i = new Intent(HomeActivity.this,SeedActivity.class);
                startActivity(i);
                finish();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ButtonSound(HomeActivity.this).execute();
                RBLeft.setVisibility(View.VISIBLE);
                RBRight.setVisibility(View.VISIBLE);
                RBCenter.setVisibility(View.GONE);
                pullInfo.setVisibility(View.VISIBLE);
                textHome.setVisibility(View.VISIBLE);
                tm.setVisibility(View.VISIBLE);
                syncInfo.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams params;
                if(MainActivity.ALLOW_CHANGE_DEVICE) {
                    params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 80);
                    linerHead.setVisibility(View.VISIBLE);
                    chartWrapper.setLayoutParams(params);
                }
                adviceMessage.setVisibility(View.GONE);
            }
        });


        chartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothDataService!=null)
                    bluetoothDataService.stop();
                new ButtonSound(HomeActivity.this).execute();

                if(db.getUserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID))==null)
                {
                    db.addUserBadgeData(new UserBadge(userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID), 0, 0));
                }
                Intent i = new Intent(HomeActivity.this,ChartActivity.class);
                startActivity(i);
                finish();
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

    void startProgressAnim(int _todayMinutes){
        totalTime.setText(Integer.toString(_todayMinutes / 60) + "h " + Integer.toString(_todayMinutes % 60) + "m");
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
            if(_todayMinutes>360)
                progressValue=300;

            ObjectAnimator animation2 = ObjectAnimator.ofInt(progressBar, "progress", 1, (int)Math.round(progressValue));
            animation2.setDuration(2000); //in milliseconds
            animation2.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userPreferences.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_NOT_RUNNING);
        Log.e("homePref", userPreferences.getUserPreferences(UserPreferences.KEY_APP_STATE));
        if(bluetoothDataService!=null)
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
                        String prevSync = lastSync;
                        lastSync = latestDate;
                        userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC, latestDate);
                        CalculateBadge calculateBadge = new CalculateBadge(HomeActivity.this, prevSync);
                        calculateBadge.execute();
                        String notifMessage = "";
                        if(todayMinutes<=180)
                        {
                            if(todayMinutes<=120)
                                notifMessage= "Try harder.\nGo to the park\nto hit your target";
                            else
                                notifMessage= "Keep it up! Try harder.\nYou can hit\nthe target";

                            adviceMessage.setText(notifMessage+"\n");
                            if(!isFinishing()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.setTitle("Synchronization completed");
                                alertDialog.setMessage(notifMessage);
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();
                                TextView messageView = (TextView) alertDialog.findViewById(android.R.id.message);
                                messageView.setGravity(Gravity.CENTER);
                            /*RBLeft.setVisibility(View.GONE);
                            RBRight.setVisibility(View.GONE);
                            RBCenter.setVisibility(View.VISIBLE);
                            linerHead.setVisibility(View.GONE);
                            pullInfo.setVisibility(View.GONE);
                            syncInfo.setVisibility(View.GONE);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,85);
                            chartWrapper.setLayoutParams(params);
                            adviceMessage.setVisibility(View.VISIBLE);
                            textHome.setVisibility(View.GONE);
                            tm.setVisibility(View.GONE);*/
                            }
                        }
						else{
							if(!isFinishing()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.setTitle("Synchronization completed");
                                alertDialog.setMessage("Congratulation.\nYou have won a badge");
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();
                                TextView messageView = (TextView) alertDialog.findViewById(android.R.id.message);
                                messageView.setGravity(Gravity.CENTER);
                            }
						}
                        Toast.makeText(HomeActivity.this, "Updated "+lastSync,Toast.LENGTH_LONG).show();
                        syncInfo.setText("Last Updated: "+lastSync);
                    }
                    else
                    {
                        Toast.makeText(HomeActivity.this, "no new data",Toast.LENGTH_LONG).show();
                        syncInfo.setText("Updated Just Now");
                    }
                    Log.d("handler", "done reading");
                    List<OutdoorData>unSyncData = db.getUnsyncOutdoorsDatas();
                    if(unSyncData.size()>0) {
                        PushToServer pushToServer = new PushToServer(HomeActivity.this, userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID), deviceName);
                        pushToServer.setUnsyncDatas(unSyncData);
                        pushToServer.execute();
                    }
                    swipeLayout.setRefreshing(false);
                    break;
                case BluetoothDataService.FAILED:
                    Log.d("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    Toast.makeText(HomeActivity.this, msg.getData().getString(BluetoothDataService.MESSAGE), Toast.LENGTH_LONG);
                    syncInfo.setText("Last Update: " + lastSync);
                    todayMinutes =db.getTodayMinutes();
                    startProgressAnim(todayMinutes);

                    swipeLayout.setRefreshing(false);
                    if(!isFinishing()) {
                        if(!adapter.isEnabled()) {
                            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(turnOnIntent, 2);
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Synchronization interrupted");
                        alertDialog.setMessage("Data is not synced yet.\nPlease try again.\nThanks.");
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        TextView messageView = (TextView) alertDialog.findViewById(android.R.id.message);
                        messageView.setGravity(Gravity.CENTER);
                    }
                    Log.d("handler", "failed");
                    break;

                case BluetoothDataService.STOPPED:
                    Log.d("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    Toast.makeText(HomeActivity.this, msg.getData().getString(BluetoothDataService.MESSAGE), Toast.LENGTH_LONG);
                    syncInfo.setText("Last Update: " + lastSync);
                    todayMinutes =db.getTodayMinutes();
                    startProgressAnim(todayMinutes);
                    swipeLayout.setRefreshing(false);
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
        syncInfo.setText("Syncing...");
        bluetoothDataService = new BluetoothDataService(HomeActivity.this,mHandler);
        Log.d("lastsyncpref", lastSync);
        bluetoothDataService.setLastSync(lastSync);
        bluetoothDataService.connect(device, true);
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
