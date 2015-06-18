package com.floo.pedometer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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


public class MainActivity extends ActionBarActivity {
    DatabaseHandler db;
    Button login;
    EditText username,pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void createNotification() {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(MainActivity.this, CongratsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);


        Notification noti = new Notification.Builder(this)
                .setContentTitle("Congratulations!")
                .setContentText("You Have Won a Badge")
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.home_icon))
                .setContentIntent(pIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);

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
            if(progressDialog.isShowing())
                progressDialog.dismiss();

            if(!result.equals(""))
            {
                try {
                    JSONObject reply = new JSONObject(result);
                    int valid = reply.getInt("result");
                    if(valid==1)
                    {
                        Log.e("result", "exist");
                        Random rand = new Random();
                        int x = rand.nextInt(150)+150;
                        db.addOutdoorDataToday(x);
                        db.testingQuery();

                        //String dateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                        //PushToServer pushToServer = new PushToServer(reply.getString("id_user"),"xperia J / 1234");
                        //pushToServer.addData(dateNow,x);
                       // pushToServer.execute();

                        //Intent i = new Intent(MainActivity.this,HomeActivity.class);
                        //startActivity(i);
                        createNotification();
                        UserPreferences userPreferences = new UserPreferences(MainActivity.this);
                        userPreferences.setUserPreferences(UserPreferences.KEY_USER_ID,reply.getString("id_user"));
                        userPreferences.setUserPreferences(UserPreferences.KEY_USER_ID,reply.getString("username"));

                        Intent i = new Intent(MainActivity.this,BluetoothActivity.class);
                        startActivity(i);

                    }
                    else if(valid==0)
                    {
                        Log.e("result", "not exist");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Log.e("result", "Please Try Again");
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.truncateOutdoorTable();
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
