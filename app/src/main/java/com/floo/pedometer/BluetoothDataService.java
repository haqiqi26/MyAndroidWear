package com.floo.pedometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by SONY_VAIO on 6/21/2015.
 */
public class BluetoothDataService {
    // Debugging
    private static final String TAG = "BluetoothDataService";

    // Name for the SDP record when creating server socket
    //private static final String NAME_SECURE = "BluetoothChatSecure";
    //private static final String NAME_INSECURE = "BluetoothChatInsecure";

    public  static final int FAILED = 0;
    public  static final int DONE_READING = 1;
    public  static final int STOPPED = 2;
    public  static final int READING_PROGRESS = 3;
    public  static final String MESSAGE = "message";


    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    //private final BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private String lastSync;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    Context context;
    Handler handler;
    public BluetoothDataService(Context context,Handler handler)
    {
        this.context = context;
        this.handler = handler;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */

    private synchronized void setState(int state) {
        Log.e(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        //mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }
    public void setLastSync(String lastSync)
    {
        this.lastSync = lastSync;
    }
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.e(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.e(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.e(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Send message to activity
        Message msg = handler.obtainMessage(BluetoothDataService.STOPPED);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothDataService.MESSAGE, "All thread Stopped");
        msg.setData(bundle);
        handler.sendMessage(msg);
        setState(STATE_NONE);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private  BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
                // Send message to activity
                Message msg = handler.obtainMessage(BluetoothDataService.FAILED);
                Bundle bundle = new Bundle();
                bundle.putString(BluetoothDataService.MESSAGE, "Socket Type: " + mSocketType + "create() failed");
                msg.setData(bundle);
                handler.sendMessage(msg);

            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            //mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

            } catch (IOException e) {
                Log.e(TAG, e.toString());
                // Close the socket
                try {
                    // Send message to activity
                    Message msg = handler.obtainMessage(BluetoothDataService.FAILED);
                    Bundle bundle = new Bundle();
                    bundle.putString(BluetoothDataService.MESSAGE, "unable to connect");
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    mmSocket.close();
                    /*try {
                        mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                    } catch (Exception e2) {
                        Log.e(TAG, e2.toString());
                    }*/
                    //connectionFailed();
                    // Cancel any thread attempting to make a connection
                    if (mConnectThread != null) {
                        mConnectThread.cancel();
                        mConnectThread = null;
                    }

                    // Cancel any thread currently running a connection
                    if (mConnectedThread != null) {
                        mConnectedThread.cancel();
                        mConnectedThread = null;
                    }

                } catch (IOException i2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", i2);
                    // Send message to activity
                    Message msg = handler.obtainMessage(BluetoothDataService.FAILED);
                    Bundle bundle = new Bundle();
                    bundle.putString(BluetoothDataService.MESSAGE, "unable to close() " + mSocketType +
                            " socket during connection failure");
                    msg.setData(bundle);
                    handler.sendMessage(msg);

                }
                //connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothDataService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
                Message msg = handler.obtainMessage(BluetoothDataService.FAILED);
                Bundle bundle = new Bundle();
                bundle.putString(BluetoothDataService.MESSAGE, "unable to close() " + mSocketType +
                        " socket during connection failure");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private DataInputStream mmDinput;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.e(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
                Message msg = handler.obtainMessage(BluetoothDataService.FAILED);
                Bundle bundle = new Bundle();
                bundle.putString(BluetoothDataService.MESSAGE, "temp sockets not created");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mmDinput = new DataInputStream(mmInStream);
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            proc_data_syn();
        }

        public void proc_data_syn(){
            byte []magicChar={'s','d'};
            SimpleDateFormat datetimeformat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            ByteBuffer bufferBuilder = ByteBuffer.allocate(2 + 8);
            bufferBuilder.put(magicChar);

            Calendar c=Calendar.getInstance();
            String[] lastSyncDate = lastSync.split(" ")[0].split("-");
            String[] lastSyncTime = lastSync.split(" ")[1].split(":");


            //c.set(2015,5,15,10,15,00);  //send the data later than 2015/06/15, 10:15:00,  5 here means June
            c.set(
                    Integer.parseInt(lastSyncDate[0]),
                    Integer.parseInt(lastSyncDate[1]) - 1,
                    Integer.parseInt(lastSyncDate[2]),
                    Integer.parseInt(lastSyncTime[0]),
                    Integer.parseInt(lastSyncTime[1]),
                    Integer.parseInt(lastSyncTime[2])
            );

            long timestamp=c.getTimeInMillis();
            bufferBuilder.putLong(timestamp);

            write(bufferBuilder.array());

            int id=0;
            //TODO
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            //Date prev = sdf.parse("2015-06-21");
            long diff=0;
            int counter =0;
            try {
                Date current = sdf.parse(lastSync);
                diff= now.getTime()-current.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }



            int minute = (int)TimeUnit.MILLISECONDS.toMinutes(diff);
            String latestDate="";
            DatabaseHandler db = DatabaseHandler.getInstance(context);
            List<OutdoorData> rows = new ArrayList<OutdoorData>();
            while (true) {
                byte []pdu=new byte[9];
                try {
                    mmDinput.readFully(pdu);
                }catch (IOException e){
                    Log.e(TAG, e.getMessage());
                    Message msg = handler.obtainMessage(BluetoothDataService.FAILED);
                    Bundle bundle = new Bundle();
                    bundle.putString(BluetoothDataService.MESSAGE, e.getMessage());
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
                ByteBuffer bb=ByteBuffer.wrap(pdu);
                long timepoint=bb.getLong();

                if (communicationComplete(timepoint, id)) {
                    //connectionLost(); // drop the current connection

                    // Send message to activity
                    for(OutdoorData row:rows)
                    {
                        db.addOutdoorData(row);
                    }
                    Message msg = handler.obtainMessage(BluetoothDataService.DONE_READING);
                    Bundle bundle = new Bundle();
                    bundle.putString(BluetoothDataService.MESSAGE, latestDate);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    // Cancel any thread attempting to make a connection
                    if (mConnectThread != null) {
                        mConnectThread.cancel();
                        mConnectThread = null;
                    }

                    // Cancel any thread currently running a connection
                    if (mConnectedThread != null) {
                        mConnectedThread.cancel();
                        mConnectedThread = null;
                    }
                    break;
                }else {
                    byte outdoors_y_n = bb.get();
                    latestDate = datetimeformat.format(new Date(timepoint));
                    String display = latestDate + " value: " + outdoors_y_n;
                    if(outdoors_y_n>0)
                    {
                        OutdoorData row = new OutdoorData(latestDate,outdoors_y_n,0);
                        rows.add(row);
                    }
                    counter++;
                    double progress = (double)counter/minute;
                    int percent = (int)Math.round(progress*100);
                    Message msg = handler.obtainMessage(BluetoothDataService.READING_PROGRESS);
                    Bundle bundle = new Bundle();
                    bundle.putString(BluetoothDataService.MESSAGE, percent+"%");
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    //Log.e("counter",counter+" "+Math.round(progress)+" "+percent+" "+diff);
                    Log.e(TAG, display);
                    id++;

                }
            }
        }

        public boolean communicationComplete(long timepoint, int id){
            if(timepoint==-1){
                return true;
            }
            return false;
        }



        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
           /*     mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();*/
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                Message msg = handler.obtainMessage(BluetoothDataService.FAILED);
                Bundle bundle = new Bundle();
                bundle.putString(BluetoothDataService.MESSAGE, "Exception during write");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
                Message msg = handler.obtainMessage(BluetoothDataService.FAILED);
                Bundle bundle = new Bundle();
                bundle.putString(BluetoothDataService.MESSAGE, "close() of connect socket failed");
                msg.setData(bundle);
                handler.sendMessage(msg);

            }
        }

    }



}
