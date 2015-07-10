package com.floo.pedometer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * Created by SONY_VAIO on 6/10/2015.
 */
public class PushToServer extends AsyncTask<Void,Void,String> {

    String userID,phoneID;
    JSONArray dataArray;
    Context context;
    List<OutdoorData> unsyncDatas;
    boolean pushList = false;
    public PushToServer(Context context,String userID,String phoneID)
    {
        this.context = context;
        this.userID= userID;
        this.phoneID = phoneID;
        dataArray = new JSONArray();
    }
    public void addData(String timeStamp,int duration)
    {
        JSONObject dataObj = new JSONObject();
        try {
            dataObj.put("datetime", timeStamp);
            dataObj.put("phoneid", phoneID);
            dataObj.put("user", userID);
            dataObj.put("duration", duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataArray.put(dataObj);
        pushList = false;
    }
    public void setUnsyncDatas(List<OutdoorData> unsyncDatas) {
        this.unsyncDatas = unsyncDatas;
        pushList = true;
    }

    @Override
    protected String doInBackground(Void... params) {

        if(pushList)
        {
            for (OutdoorData data: unsyncDatas) {
                JSONObject dataObj = new JSONObject();
                try {
                    dataObj.put("datetime", data.getTimeStamp());
                    dataObj.put("phoneid", phoneID);
                    dataObj.put("user", userID);
                    dataObj.put("duration", data.getMinutes());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dataArray.put(dataObj);
            }
            Log.d("dataarray", String.valueOf(dataArray.length()));
        }
        // Create a new HttpClient and Post Header
        String result="";
        HttpParams myParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(myParams, 10000);
        HttpConnectionParams.setSoTimeout(myParams, 10000);
        HttpClient httpclient = new DefaultHttpClient(myParams );
        String url = "http://development.ayowes.com/pedobackend/public/api/activitieslog";

        try {

            String json=dataArray.toString();

            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-type", "application/json");

            StringEntity se = new StringEntity(json);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httppost.setEntity(se);

            HttpResponse response = httpclient.execute(httppost);
            result = EntityUtils.toString(response.getEntity());
            Log.d("pushdata", result);


        } catch (ClientProtocolException e) {
            Log.e("pushdata",e.getMessage());
            result="";
        }
        catch (ConnectTimeoutException e){
            Log.e("pushdata","timeout");
            result="";
        }
        catch (SocketTimeoutException e){
            Log.e("pushdata","timeout");
            result="";
        }
        catch (IOException e) {
            Log.e("pushdata", "ioexception");
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
                int val = reply.getInt("saved_data");
                if(pushList)
                {
                    for (int i=0;i<val;i++)
                    {
                        OutdoorData data = unsyncDatas.get(i);
                        data.setFlag(1);
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateOutdoorData(data);
                    }
                }
                Log.d("pushdata","push data done, result: "+val+" data size: "+dataArray.length());
            } catch (JSONException e) {
                //e.printStackTrace();
                Log.e("pushdata",e.getMessage());
            }

        }
        else{
            Log.e("pushdata","push data failed");
        }

    }
}
