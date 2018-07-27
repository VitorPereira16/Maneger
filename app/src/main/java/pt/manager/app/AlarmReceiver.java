package pt.manager.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver
{
    static int times = 0;

    @Override
    public void onReceive( Context context, Intent intent )
    {
        Log.v("ALARM", context+"");
        PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
        PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "SUNNAH_PRO_NOTIFY" );
        wl.acquire();
        wl.release();

        MainActivity Async = new MainActivity();
        Async.new ServiceStubAsyncTask2(context).execute();

    }
}
