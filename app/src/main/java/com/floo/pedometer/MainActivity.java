package com.floo.pedometer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class MainActivity extends ActionBarActivity{
    DatabaseHandler db;
    Button login;
    SwipeRefreshLayout refreshLayout;
    UserPreferences pref;
    EditText username,pass;
    BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT =1;
    String bluetoothAddr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref= new UserPreferences(MainActivity.this);
        username = (EditText) findViewById(R.id.userName);
        pass = (EditText)findViewById(R.id.userPass);
        db = DatabaseHandler.getInstance(this);
        login = (Button) findViewById(R.id.loginButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(MainActivity.this,HomeActivity.class);
                startActivity(i);
                createNotification();
                //finish();*/
                new DoLogin().execute();
                //Random rand = new Random();
                //int x = rand.nextInt(150)+150;
                //db.addOutdoorDataToday(x);
                /*UserPreferences userPreferences = new UserPreferences(MainActivity.this);
                userPreferences.setUserPreferences(UserPreferences.KEY_USER_ID,"1");
                userPreferences.setUserPreferences(UserPreferences.KEY_USER_ID,"user1");

                Intent i = new Intent(MainActivity.this,BluetoothActivity.class);
                startActivity(i);*/
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        pref.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_RUNNING);
        String userID = pref.getUserPreferences(UserPreferences.KEY_USER_ID);
        String username = pref.getUserPreferences(UserPreferences.KEY_USERNAME);
        bluetoothAddr = pref.getUserPreferences(UserPreferences.KEY_BLUETOOTH_ADDRESS);
        String bluetoothName = pref.getUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME);

        if(!userID.equals("")&&!username.equals("")&&!bluetoothAddr.equals("")&&!bluetoothName.equals(""))
        {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter==null){
                AlertDialog.Builder builder =  new AlertDialog.Builder(MainActivity.this);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("Oooppss!!");
                alertDialog.setMessage("Bluetooth not supported");
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
            else {
                turnOnBTandSwitch();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        pref.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_NOT_RUNNING);

    }

    public void turnOnBTandSwitch()
    {
        if(!bluetoothAdapter.isEnabled())
        {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            Toast.makeText(MainActivity.this,"Bluetooth turned on",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(MainActivity.this,"Bluetooth is already on",Toast.LENGTH_LONG).show();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothAddr);
            Intent i = new Intent(MainActivity.this,HomeActivity.class);
            i.putExtra("selectedDevice",device);
            startActivity(i);
            finish();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothAddr);
                Intent i = new Intent(MainActivity.this,HomeActivity.class);
                i.putExtra("selectedDevice",device);
                startActivity(i);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private class DoLogin extends AsyncTask<String,Void,String>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();


        }

        @Override
        protected String doInBackground(String... params) {
            // Create a new HttpClient and Post Header
            String result="";
            HttpParams myParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(myParams, 10000);
            HttpConnectionParams.setSoTimeout(myParams, 10000);
            HttpClient httpclient = new DefaultHttpClient(myParams );
            JSONObject jsonObj = new JSONObject();
            String url = "http://development.ayowes.com/pedobackend/public/api/auth";

            try {

                jsonObj.put("username",username.getText().toString());
                jsonObj.put("pass",pass.getText().toString());
                String json=jsonObj.toString();

                HttpPost httppost = new HttpPost(url);
                httppost.setHeader("Content-type", "application/json");

                StringEntity se = new StringEntity(json);
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);

                HttpResponse response = httpclient.execute(httppost);
                result = EntityUtils.toString(response.getEntity());
                Log.e("tag", result);


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            if(!result.equals(""))
            {
                try {
                    JSONObject reply = new JSONObject(result);
                    int valid = reply.getInt("result");
                    if(valid==1)
                    {
                        Log.e("result", "exist");
                        /*Random rand = new Random();
                        int x = rand.nextInt(150)+150;
                        db.addOutdoorDataToday(x);
                        db.testingQuery();

                        //String dateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                        //PushToServer pushToServer = new PushToServer(reply.getString("id_user"),"xperia J / 1234");
                        //pushToServer.addData(dateNow,x);
                       // pushToServer.execute();

                        //Intent i = new Intent(MainActivity.this,HomeActivity.class);
                        //startActivity(i);*/
                        //createNotification();
                        UserPreferences userPreferences = new UserPreferences(MainActivity.this);
                        userPreferences.setUserPreferences(UserPreferences.KEY_USER_ID,reply.getString("id_user"));
                        userPreferences.setUserPreferences(UserPreferences.KEY_USERNAME,reply.getString("username"));
                        Log.e("pref", userPreferences.getUserPreferences(UserPreferences.KEY_LAST_SYNC));
                        Log.e("pref",userPreferences.getUserPreferences(UserPreferences.KEY_USER_ID));
                        Log.e("pref",userPreferences.getUserPreferences(UserPreferences.KEY_USERNAME));



                        Intent i = new Intent(MainActivity.this,BluetoothActivity.class);
                        startActivity(i);
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                    else if(valid==0)
                    {
                        Log.e("result", "not exist");
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        AlertDialog.Builder builder =  new AlertDialog.Builder(MainActivity.this);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Oooppss!!");
                        alertDialog.setMessage("Please check your username and password");
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }
            else{
                Log.e("result", "Please Try Again");
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder builder =  new AlertDialog.Builder(MainActivity.this);
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
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //db.truncateOutdoorTable();
        this.finish();

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
