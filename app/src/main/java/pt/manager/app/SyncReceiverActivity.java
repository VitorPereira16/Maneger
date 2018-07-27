package pt.manager.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by vitorpereira on 26/02/18.
 */

public class SyncReceiverActivity extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_LONG).show();
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}