package com.polito.did2017.lampup.utilities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by matil on 20/04/2018.
 */

public class ConnectTask extends AsyncTask<TCPClient, String, Long> {

    private Context context;
    private Activity activity;

    public ConnectTask(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(Long aLong) {
        if (aLong == -1) {
            Toast.makeText( context, "Connection refused", Toast.LENGTH_LONG ).show();
            activity.finish();
        }
    }

    @Override
    protected Long doInBackground(TCPClient... tcpClients) {
        try {
            tcpClients[0].run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new Long( 0 );
    }
}
