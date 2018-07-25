package pt.manager.maneger;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


public class RemindersIntentManager
{

    private static final int CHRISTMAS = 0;
    private static final int THE_INTERNATIONALS = 1;
    private static final int SPECIFIC_EVENT = 3;
    static RemindersIntentManager remindersIntentManager;
    private static PendingIntent[] reminderIntents;
    private Context mContext;

    private RemindersIntentManager( Context context )
    {

        mContext = context;


        reminderIntents = new PendingIntent[ 3 ];
    }

    public static RemindersIntentManager getInstance( Context context )
    {

        if ( remindersIntentManager == null )
        {
            remindersIntentManager = new RemindersIntentManager( context );
        }


        return remindersIntentManager;

    }

    public PendingIntent getChristmasIntent()
    {


        Intent intentAlarm = new Intent( mContext,  pt.manager.maneger.AlarmReceiver.class );
        intentAlarm.putExtra( "reminder", "Christmas Happies!" );
        intentAlarm.putExtra( "code", ( CHRISTMAS + 1 ) * 133 );

        reminderIntents[ CHRISTMAS ] = PendingIntent.getBroadcast( mContext, CHRISTMAS, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT );

        return reminderIntents[ CHRISTMAS ];

    }



    public PendingIntent getDotaIntent()
    {
        Intent intentAlarm = new Intent( mContext, pt.manager.maneger.AlarmReceiver.class );
        intentAlarm.putExtra( "reminder", "Aegis is up for grabs! The Internationals starts now." );
        intentAlarm.putExtra( "code", ( THE_INTERNATIONALS + 1 ) * 133 );

        reminderIntents[ THE_INTERNATIONALS ] = PendingIntent.getBroadcast(
                mContext, THE_INTERNATIONALS, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT );

        return reminderIntents[ THE_INTERNATIONALS ];
    }



    public PendingIntent getSpecificIntent()
    {
        Intent intentAlarm = new Intent( mContext,  pt.manager.maneger.AlarmReceiver.class );
        intentAlarm.putExtra( "reminder", "You see but you do not observe." );
        intentAlarm.putExtra( "code", ( SPECIFIC_EVENT + 1 ) * 133 );

        reminderIntents[ SPECIFIC_EVENT ] = PendingIntent.getBroadcast( mContext, SPECIFIC_EVENT, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT );

        return reminderIntents[ SPECIFIC_EVENT ];
    }


}
