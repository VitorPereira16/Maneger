package pt.manager.maneger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import pt.manager.maneger.MainActivity;



public class AlarmReceiver extends BroadcastReceiver
{
    static int times = 0;

    @Override
    public void onReceive( Context context, Intent intent )
    {
        PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
        PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "SUNNAH_PRO_NOTIFY" );
        wl.acquire();


        wl.release();
        //Toast.makeText( context, "Alarm Scheduled for next minute", Toast.LENGTH_LONG ).show();

        MainActivity Async = new MainActivity();
        Async.new ServiceStubAsyncTask2(context).execute();



    }
}
