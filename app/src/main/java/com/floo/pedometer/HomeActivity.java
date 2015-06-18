package com.floo.pedometer;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.UUID;


public class HomeActivity extends ActionBarActivity {

    ProgressBar progressBar;
    ImageButton chartButton,seedButton;
    Spinner spinner;
    MediaPlayer mp;
    DatabaseHandler db;
    MyTextView totalTime;
    ArrayAdapter<String>menuDrop;
    BluetoothDevice device;
    UUID MY_UUID;
    boolean running;
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    //int todayMinutes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_kit);

        progressBar = (ProgressBar) findViewById(R.id.myProgress);
        chartButton = (ImageButton) findViewById(R.id.chartButton);
        seedButton = (ImageButton) findViewById(R.id.seedButton);
        totalTime = (MyTextView) findViewById(R.id.progressText);
        spinner = (Spinner) findViewById(R.id.spinnerDevices);
        menuDrop = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_spinner_item);
        UserPreferences userPreferences = new UserPreferences(HomeActivity.this);
        String deviceName = userPreferences.getUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME);
        running =true;

        if(deviceName!=null||deviceName.equals(""))
            menuDrop.add(deviceName+"\nConnecting...");
        menuDrop.add("Pair with new watch");
        menuDrop.notifyDataSetChanged();

        spinner.setAdapter(menuDrop);

        device = getIntent().getExtras().getParcelable("selectedDevice");
        MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
        myThreadConnectBTdevice.start();

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

                db.addUserBadgeData(new UserBadge("john", 0, 12));
                db.addUserBadgeData(new UserBadge("jane", 2, 10));
                db.addUserBadgeData(new UserBadge("jack",4,5));

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
        running = false;
        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }

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

    private class ThreadConnectBTdevice extends Thread {
        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        public ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.e("log","bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                Log.e("log",eMessage);

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String info = menuDrop.getItem(0);
                        menuDrop.remove(info);
                        info.replace("Connecting...", "Connected");
                        menuDrop.insert(info,0);
                        menuDrop.notifyDataSetChanged();

                    }
                });
                Log.e("log",msgconnected);

                startThreadConnected(bluetoothSocket);
            }else{
                //fail
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String info = menuDrop.getItem(0);
                        menuDrop.remove(info);
                        info.replace("Connecting...", "Failed");
                        menuDrop.insert(info,0);
                        menuDrop.notifyDataSetChanged();
                    }
                });
                Log.e("log","failed to connect");
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
        myThreadConnected.write("get".getBytes());
    }

    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (running) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //get data
                            int todayMinutes = Integer.parseInt(strReceived);
                            startProgressAnim(todayMinutes);

                            Toast.makeText(HomeActivity.this,"Sync",Toast.LENGTH_LONG).show();

                        }
                    });
                    Log.e("log",msgReceived);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    Log.e("log", msgConnectionLost);
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
