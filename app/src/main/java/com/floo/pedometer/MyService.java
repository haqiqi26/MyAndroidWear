package com.floo.pedometer;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

public class MyService extends IntentService {


    UserPreferences userPreferences = new UserPreferences(this);
    DatabaseHandler db = DatabaseHandler.getInstance(this);
    String lastSync="";
    BluetoothDataService bluetoothDataService;
    String bluetoothAddr;
    BluetoothDevice device;
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MyService(String name) {
        super(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
                        CalculateBadge calculateBadge = new CalculateBadge(MyService.this,lastSync);
                        calculateBadge.execute();
                        userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC, latestDate);
                        lastSync = latestDate;

                        List<OutdoorData> unSyncData = db.getUnsyncOutdoorsDatas();
                        PushToServer pushToServer = new PushToServer(MyService.this,userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID),"myPhoneID");//replace 1 with userID from login
                        pushToServer.setUnsyncDatas(unSyncData);
                        pushToServer.execute();

                        Toast.makeText(MyService.this, "Updated " + lastSync, Toast.LENGTH_LONG).show();
                        Intent returnResult = new Intent(HomeActivity.RECEIVE_UPDATE);
                        returnResult.putExtra("latestUpdate", lastSync);
                        LocalBroadcastManager.getInstance(MyService.this).sendBroadcast(returnResult);
                        //sendBroadcast();

                        //        bluetoothDataService.stop();
                    }
                    else
                    {
                        Toast.makeText(MyService.this, "no new data",Toast.LENGTH_LONG).show();
                    }
                    Log.e("handler", "done reading");

                    break;
                //if success thread
                //set last sync
                //queary data
                //push to server

                case BluetoothDataService.FAILED:
                    Log.e("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    Toast.makeText(MyService.this, msg.getData().getString(BluetoothDataService.MESSAGE), Toast.LENGTH_LONG);

//                    bluetoothDataService.stop();

                    Log.e("handler", "failed");
                    break;

                //if failed
                //keluar sync failed
                case BluetoothDataService.STOPPED:
                    Log.e("bluetooth", msg.getData().getString(BluetoothDataService.MESSAGE));
                    Toast.makeText(MyService.this, msg.getData().getString(BluetoothDataService.MESSAGE), Toast.LENGTH_LONG);

                    Log.e("handler","stopped");
                    break;
            }
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {


        bluetoothAddr = userPreferences.getUserPreferences(UserPreferences.KEY_BLUETOOTH_ADDRESS);
        if(!bluetoothAddr.equals("")) {
            device = adapter.getRemoteDevice(bluetoothAddr);
            lastSync = userPreferences.getUserPreferences(UserPreferences.KEY_LAST_SYNC);
            Log.e("lastsyncpref", lastSync);
            if (lastSync.equals("")) {
                //lastSync = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                lastSync = "2015-06-15 10:15:00";
                userPreferences.setUserPreferences(UserPreferences.KEY_LAST_SYNC, lastSync);
            }

            bluetoothDataService = new BluetoothDataService(this, mHandler);
            bluetoothDataService.setLastSync(lastSync);
            bluetoothDataService.connect(device, true);
        }
        scheduleNextUpdate();

    }

    private void scheduleNextUpdate()
    {
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // The update frequency should often be user configurable.  This is not.

        long currentTimeMillis = System.currentTimeMillis();
        long nextUpdateTimeMillis = currentTimeMillis + 60 * DateUtils.MINUTE_IN_MILLIS;
        Time nextUpdateTime = new Time();
        nextUpdateTime.set(nextUpdateTimeMillis);

        if (nextUpdateTime.hour < 8 || nextUpdateTime.hour >= 20)
        {
            nextUpdateTime.hour = 8;
            nextUpdateTime.minute = 0;
            nextUpdateTime.second = 0;
            nextUpdateTimeMillis = nextUpdateTime.toMillis(false) + DateUtils.DAY_IN_MILLIS;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
    }
}
