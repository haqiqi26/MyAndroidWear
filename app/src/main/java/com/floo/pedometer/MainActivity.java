package com.floo.pedometer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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


public class MainActivity extends ActionBarActivity{
    DatabaseHandler db;
    Button login;
    UserPreferences pref;
    EditText usernameEdit,pass;
    BluetoothAdapter bluetoothAdapter;

    public static final boolean ALLOW_CHANGE_DEVICE = false;//false to hide,true to show
    public static final String FIRST_SYNC_TIME = "2015-08-01 10:15:00";//set the oldest data
    public static final boolean SHOW_BADGE_CONGRAT_EVERY_SYNC = false;//true to show every sync


    String bluetoothAddr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = new UserPreferences(MainActivity.this);
        pref.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_RUNNING);
        String userID = pref.getUserPreferences(UserPreferences.KEY_USER_ID);
        String username = pref.getUserPreferences(UserPreferences.KEY_USERNAME);
        bluetoothAddr = pref.getUserPreferences(UserPreferences.KEY_BLUETOOTH_ADDRESS);
        String bluetoothName = pref.getUserPreferences(UserPreferences.KEY_BLUETOOTH_NAME);

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
            TextView messageView = (TextView)alertDialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.CENTER);
        }
        else{
            if(!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this,"Enabling bluetooth",Toast.LENGTH_LONG).show();
                bluetoothAdapter.enable();
            }
            else{
                Toast.makeText(this,"Bluetooth already on",Toast.LENGTH_LONG).show();

            }
        }

        if(!userID.equals("")&&!username.equals("")&&!bluetoothAddr.equals("")&&!bluetoothName.equals(""))
        {
            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
            while (!bluetoothAdapter.isEnabled());
            progressDialog.dismiss();

            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothAddr);
            Intent i = new Intent(MainActivity.this,HomeActivity.class);
            i.putExtra("selectedDevice",device);
            startActivity(i);
            finish();
        }
        else {


            setContentView(R.layout.activity_main);
            usernameEdit = (EditText) findViewById(R.id.userName);
            pass = (EditText) findViewById(R.id.userPass);
            db = DatabaseHandler.getInstance(this);
            login = (Button) findViewById(R.id.loginButton);

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DoLogin().execute();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        pref.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_RUNNING);


    }

    @Override
    protected void onStop() {
        super.onStop();
        pref.setUserPreferences(UserPreferences.KEY_APP_STATE, UserPreferences.APP_NOT_RUNNING);

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
            //String url = "http://development.ayowes.com/pedobackend/public/api/auth";
            String url = "http://www.datatestserversg.com/api/auth";

            try {

                jsonObj.put("username",usernameEdit.getText().toString());
                jsonObj.put("pass",pass.getText().toString());
                String json=jsonObj.toString();

                HttpPost httppost = new HttpPost(url);
                httppost.setHeader("Content-type", "application/json");

                StringEntity se = new StringEntity(json);
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);

                HttpResponse response = httpclient.execute(httppost);
                result = EntityUtils.toString(response.getEntity());
                Log.d("tag", result);


            } catch (ClientProtocolException e) {
                e.printStackTrace();
                result="";
            } catch (IOException e) {
                e.printStackTrace();
                result="";
            } catch (JSONException e) {
                e.printStackTrace();
                result="";
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
                        Log.d("result", "exist");

                        UserPreferences userPreferences = new UserPreferences(MainActivity.this);
                        userPreferences.setUserPreferences(UserPreferences.KEY_USER_ID,reply.getString("id_user"));
                        userPreferences.setUserPreferences(UserPreferences.KEY_USERNAME,reply.getString("username"));


                        Intent i = new Intent(MainActivity.this,BluetoothActivity.class);
                        startActivity(i);
                        finish();
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                    else if(valid==0)
                    {
                        Log.d("result", "not exist");
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
                        TextView messageView = (TextView)alertDialog.findViewById(android.R.id.message);
                        messageView.setGravity(Gravity.CENTER);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }
            else{
                Log.d("result", "Please Try Again");
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
                TextView messageView = (TextView)alertDialog.findViewById(android.R.id.message);
                messageView.setGravity(Gravity.CENTER);
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
