package com.floo.pedometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
    //Button onButton,offButton,listButton,findButton;
    Button findButton;
    TextView statusBT;
    BluetoothAdapter myBTAdapter;
    Set<BluetoothDevice> pairedDevices;
    List<BluetoothDevice> newDevices;
    ListView listViewPaired,listViewNew;
    ArrayAdapter<String> BTPairedArrayAdapter,BTNewArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        /*onButton = (Button)findViewById(R.id.turnOn);
        offButton= (Button)findViewById(R.id.turnOff);
        listButton = (Button)findViewById(R.id.paired);
        findButton= (Button)findViewById(R.id.search);*/
        findButton= (Button)findViewById(R.id.find);
        statusBT = (TextView)findViewById(R.id.bluetoothStatus);
        //listViewPaired = (ListView)findViewById(R.id.listPairedDevices);
        listViewNew = (ListView)findViewById(R.id.listNewDevices);

        myBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBTAdapter == null)
        {
            /*
            onButton.setEnabled(false);
            offButton.setEnabled(false);
            listButton.setEnabled(false);
            findButton.setEnabled(false);*/
            statusBT.setText("Status: device not supported");
        }
        else
        {
            findButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findBT();
                }
            });
            /*
            onButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnOnBT();
                }
            });
            offButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnOffBT();
                }
            });
            findButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findBT();
                }
            });
            listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listPairedDevices();
                }
            });

*/
            //BTPairedArrayAdapter = new ArrayAdapter<String>(BluetoothActivity.this,android.R.layout.simple_expandable_list_item_1);
            BTNewArrayAdapter = new ArrayAdapter<String>(BluetoothActivity.this,android.R.layout.simple_expandable_list_item_1){

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    text.setGravity(Gravity.CENTER);
                    text.setTextColor(getResources().getColor(R.color.blue));
                    return view;
                }
            };;

            //listViewPaired.setAdapter(BTPairedArrayAdapter);
            listViewNew.setAdapter(BTNewArrayAdapter);
            listViewNew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    UserPreferences userPreferences= new UserPreferences(BluetoothActivity.this);
                    userPreferences.setUserPreferences(UserPreferences.KEY_BLUETOOTH_ADDRESS,newDevices.get(position).getAddress());
                    userPreferences.setUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME,newDevices.get(position).getName());
                    try {
                        createBond(newDevices.get(position));
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
        findBT();
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
            statusBT.setText("Status: Enabled");
            makeDiscoverable();
        }
    }

    public void listPairedDevices()
    {
        pairedDevices = myBTAdapter.getBondedDevices();
        for(BluetoothDevice device:pairedDevices)
            BTPairedArrayAdapter.add(device.getName()+"\n"+device.getAddress());
    }

    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                int newBTcount = newDevices.size();
                boolean flag = false;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(newBTcount==0)
                {
                    newDevices.add(device);
                    BTNewArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    BTNewArrayAdapter.notifyDataSetChanged();
                }
                else if(newBTcount>0)
                {
                    for(BluetoothDevice bt:newDevices)
                    {
                        if(!bt.getAddress().equals(device.getAddress()))
                        {
                            flag=true;
                        }
                    }
                    if(flag)
                    {
                        newDevices.add(device);
                        BTNewArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        BTNewArrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    public void findBT()
    {
        if(myBTAdapter.isDiscovering())
            myBTAdapter.cancelDiscovery();
        else
        {
            BTNewArrayAdapter.clear();
            newDevices = new ArrayList<>();
            myBTAdapter.startDiscovery();
            registerReceiver(receiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void turnOffBT()
    {
        myBTAdapter.disable();
        statusBT.setText("Status: Disconnected");
        Toast.makeText(BluetoothActivity.this,"Bluetooth turned off",Toast.LENGTH_LONG).show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT)
        {
            if(myBTAdapter.isEnabled()) {
                statusBT.setText("Status: Enabled");
                makeDiscoverable();
            }
            else
                statusBT.setText("Status: Disabled");
        }
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
