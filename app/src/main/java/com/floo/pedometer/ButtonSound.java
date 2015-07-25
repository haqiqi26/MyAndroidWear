package com.floo.pedometer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * Created by SONY_VAIO on 25-Jul-15.
 */
public class ButtonSound extends AsyncTask<Void,Void,Void> {
    Context context;
    MediaPlayer mp;
    public ButtonSound(Context context){
        this.context = context.getApplicationContext();
    }

    @Override
    protected Void doInBackground(Void... params) {
        mp = MediaPlayer.create(context, Uri.parse("/system/media/audio/ui/Effect_Tick.ogg"));
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
