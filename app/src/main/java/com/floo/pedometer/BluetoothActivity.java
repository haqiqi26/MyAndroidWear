package com.floo.pedometer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BluetoothActivity extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT =1;
    Button findButton;
    //TextView statusBT;
    BluetoothAdapter myBTAdapter;
    List<BluetoothDevice> pairedDevices;
    List<BluetoothDevice> newDevices;
    ListView listViewPaired,listViewNew;
    ArrayAdapter<String> BTPairedArrayAdapter,BTNewArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        findButton= (Button)findViewById(R.id.find);
        //statusBT = (TextView)findViewById(R.id.bluetoothStatus);
        listViewPaired = (ListView)findViewById(R.id.listPairedDevices);
        listViewNew = (ListView)findViewById(R.id.listNewDevices);

        myBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBTAdapter == null)
        {
            //statusBT.setText("Status: device not supported");
            Log.e("bluetooth","not supported");
        }
        else
        {
            findButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findBT();
                }
            });

            BTPairedArrayAdapter = new ArrayAdapter<String>(BluetoothActivity.this,R.layout.list_row);
            BTNewArrayAdapter = new ArrayAdapter<String>(BluetoothActivity.this,R.layout.list_row);

            listViewPaired.setAdapter(BTPairedArrayAdapter);
            listViewPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    UserPreferences userPreferences= new UserPreferences(BluetoothActivity.this);
                    userPreferences.setUserPreferences(UserPreferences.KEY_BLUETOOTH_ADDRESS,pairedDevices.get(position).getAddress());
                    userPreferences.setUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME,pairedDevices.get(position).getName());

                    Log.e("selectedDevice",pairedDevices.get(position).getName());

                    Intent i = new Intent(BluetoothActivity.this,HomeActivity.class);
                    i.putExtra("selectedDevice", pairedDevices.get(position));
                    startActivity(i);

                }
            });
            listViewNew.setAdapter(BTNewArrayAdapter);
            listViewNew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    UserPreferences userPreferences= new UserPreferences(BluetoothActivity.this);
                    userPreferences.setUserPreferences(UserPreferences.KEY_BLUETOOTH_ADDRESS,newDevices.get(position).getAddress());
                    userPreferences.setUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME,newDevices.get(position).getName());
                    try {
                        boolean flag = false;
                        if(createBond(newDevices.get(position)))
                        {
                            String temp = BTNewArrayAdapter.getItem(position);
                            BTNewArrayAdapter.remove(temp);
                            BTNewArrayAdapter.notifyDataSetChanged();
                            flag = true;
                        }
                        if(flag)
                            newDevices.remove(position);
                        listPairedDevices();

                        Intent i = new Intent(BluetoothActivity.this,HomeActivity.class);
                        i.putExtra("selectedDevice",newDevices.get(position));
                        startActivity(i);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        turnOnBT();
        //listPairedDevices();
        //findBT();
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        Log.e("Log", "Discoverable ");

    }

    public void turnOnBT()
    {
        if(!myBTAdapter.isEnabled())
        {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            Toast.makeText(BluetoothActivity.this,"Bluetooth turned on",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(BluetoothActivity.this,"Bluetooth is already on",Toast.LENGTH_LONG).show();
            //statusBT.setText("Status: Enabled");
            listPairedDevices();
            findBT();
            //makeDiscoverable();
        }
    }

    public void listPairedDevices()
    {
        pairedDevices = new ArrayList<>(myBTAdapter.getBondedDevices());
        BTPairedArrayAdapter.clear();
        for(BluetoothDevice device:pairedDevices) {
            BTPairedArrayAdapter.add(device.getName());
            Log.e("paired","addr: "+device.getAddress()+" name: "+device.getName()+" uuid: "+device.getUuids());
        }
        BTPairedArrayAdapter.notifyDataSetChanged();
    }

    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {

                findButton.setEnabled(true);
                findButton.setText("Find Again");
                int newBTcount = newDevices.size();
                boolean flag = false;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //newDevices.add(device);
                //BTNewArrayAdapter.add(device.getName());
                //BTNewArrayAdapter.notifyDataSetChanged();

                if(newBTcount==0)
                {
                    newDevices.add(device);
                    BTNewArrayAdapter.add(device.getName());
                    BTNewArrayAdapter.notifyDataSetChanged();
                }
                else if(newBTcount>0)
                {
                    for(BluetoothDevice bt:newDevices)
                    {
                        flag=true;
                        if(bt.getAddress().equals(device.getAddress()))
                        {
                            flag=false;
                            break;
                        }
                    }
                    if(flag)
                    {
                        newDevices.add(device);
                        BTNewArrayAdapter.add(device.getName());
                        BTNewArrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                findButton.setEnabled(true);
                findButton.setText("Find Again");
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                listPairedDevices();
                findBT();
            }
        }
    }

    public void findBT()
    {
        findButton.setText("Searching...");
        findButton.setEnabled(false);
        if(myBTAdapter.isDiscovering())
            myBTAdapter.cancelDiscovery();
        else
        {
            BTNewArrayAdapter.clear();
            BTNewArrayAdapter.notifyDataSetChanged();
            newDevices = new ArrayList<>();
            myBTAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, intentFilter);
        }
    }
    public boolean createBond(BluetoothDevice btDevice)throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
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