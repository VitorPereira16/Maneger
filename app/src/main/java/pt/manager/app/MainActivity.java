package pt.manager.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskCompleted {

    private Handler handler = new Handler();
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    public String apiPath = "http://itmanager.pt/mobile_contacts/get_contacts.php";
    private ProgressBar progressBar;
    public static String validate = "0";
    public static String nif = "";
    public static String data_sysnc;
    public static final String PREFS_NAME = "MyPrefsFile";
    public int progressStatus = 0;
    SharedPreferences settings;
    public static TextView t_date;
    PendingIntent myPendingIntent;
    AlarmManager alarmManager;
    BroadcastReceiver myBroadcastReceiver;
    Calendar firingCal;
    private static final String TAG ="MainActivity";
    Integer control_cancel;

    public TextView t_emp,t_nome, t_nume, t_carg, t_dep, t_nif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t_emp = (TextView)findViewById(R.id.textView9);
        t_nome = (TextView)findViewById(R.id.textView8);
        t_nume = (TextView)findViewById(R.id.textView7);
        t_carg = (TextView)findViewById(R.id.textView11);
        t_dep = (TextView)findViewById(R.id.textView10);
        t_date = (TextView)findViewById(R.id.textView4);

        control_cancel = 0;

        final RemindersIntentManager remindersIntentManager = RemindersIntentManager.getInstance( this );

        Calendar calendar = Calendar.getInstance();
        Date curr=new Date();
        curr.setHours(12);
        curr.setMinutes(00);
        calendar.setTime(curr);
        calendar.set(Calendar.SECOND, 0);

        final Long time = new GregorianCalendar().getTimeInMillis() + 1 * 60 * 1000;

        final AlarmManager alarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );

        //alarmManager.setRepeating( AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 30*1000, remindersIntentManager.getChristmasIntent() );
        alarmManager.setRepeating( AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, remindersIntentManager.getChristmasIntent(MainActivity.this) );

        verifyPremission();
        int ver_net = verifyConnection();
        if(ver_net==0) {
            ShowPopNet();
        }
        settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        validate = settings.getString("validar", "0");
        nif = settings.getString("nif", "");
        int ColorDate = Integer.parseInt(settings.getString("color_sync", "0"));
        String data = settings.getString("data_sysnc", "");

        t_nif = (TextView)findViewById(R.id.textNif);
        TextView disp = (TextView)findViewById(R.id.textView);
        t_nif.setText(nif);

        Typeface type = Typeface.createFromAsset(getAssets(),"font/pop_reg.ttf");
        t_nif.setTypeface(type);
        disp.setTypeface(type);

        t_date = (TextView) findViewById(R.id.textView4);
        t_date.setText(data);
        //
        t_date.setTextColor(ColorDate);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this,R.color.color6), PorterDuff.Mode.SRC_IN);

        t_emp = (TextView) findViewById(R.id.textView9);
        t_nome = (TextView)findViewById(R.id.textView8);
        t_nume = (TextView)findViewById(R.id.textView7);
        t_carg = (TextView)findViewById(R.id.textView11);
        t_dep = (TextView)findViewById(R.id.textView10);

        String empresa = "";
        String nome = "";
        String ID = "";
        String funcao = "";
        String departamento = "";

        empresa = settings.getString("empresa", "");
        nome = settings.getString("nome", "");
        ID = settings.getString("ID", "");
        funcao = settings.getString("funcao", "");
        departamento = settings.getString("departamento", "");


        setText2(t_emp, empresa,this);
        setText2(t_nome,nome,this);
        setText2(t_nume,ID,this);
        setText2(t_carg,funcao,this);
        setText2(t_dep,departamento,this);


        if(!validate.equals("1")){
            setText2(t_emp, "",this);
            setText2(t_nome,"",this);
            setText2(t_nume,"",this);
            setText2(t_carg,"",this);
            setText2(t_dep,"",this);
            progressBar.setVisibility(View.INVISIBLE);
            ShowPopNif();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            /*
            //Calendar calendar = Calendar.getInstance();
            //calendar.set(Calendar.HOUR_OF_DAY, 13); // For 1 PM or 2 PM
            //calendar.set(Calendar.MINUTE, 0);
            //calendar.set(Calendar.SECOND, 5);

            //Create an offset from the current time in which the alarm will go off.
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 5);
            //cal.set(Calendar.HOUR_OF_DAY, 21);
            //cal.set(Calendar.MINUTE, 19);

            Log.d("Alarm scheduler","Alarm is being scheduled");
            Intent intent = new Intent(this, SyncReceiverActivity.class);
            PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
            /*
            PendingIntent pi = PendingIntent.getService(this, 0,
                    new Intent(this, SyncReceiverActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);



            /*
            //Create a new PendingIntent and add it to the AlarmManager
            Intent intent = new Intent(this, SyncReceiverActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am =
                    (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    pendingIntent);*/

        }

        Button bt_nif = (Button) findViewById(R.id.button3);
        bt_nif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowPopNif();
            }
        });

        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        //progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //progressBar.setMax(100);

        TextView t = new TextView(this);
        t=(TextView)findViewById(R.id.android_id);
        t.setText(android_id);


        data_sysnc = settings.getString("data_sysnc","");
        t_date = (TextView)findViewById(R.id.textView4);
        t_date.setText(data_sysnc);

        Button fab = (Button) findViewById(R.id.button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSync();
            }
        });


    }

    public int verifyConnection(){
        int val = 0;
        if (isNetworkAvailable()) {
            val = 1;
        }
        return val;
    }
    public void verifyPremission(){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_CONTACTS)) {

                // Show a expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Toast toast = Toast.makeText(context, "desbloquiar premissoes", duration);
                //toast.show();
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                //finish();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);


            }
        }
    }

    public String getSafeSubstring(String s, int maxLength){
        if(!TextUtils.isEmpty(s)){
            if(s.length() >= maxLength){
                return s.substring(0, maxLength);
            }
        }
        return s;
    }

    public void ShowPopNif(){
        final Button pbutton;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_dialog);
        builder.setMessage(R.string.msg_dialog);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.getBackground().setColorFilter(getResources().getColor(R.color.color1), PorterDuff.Mode.SRC_IN);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});

        builder.setView(input);
        builder.setPositiveButton(R.string.confirm,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nif = input.getText().toString();
                SharedPreferences sett = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor edit = sett.edit();
                edit.putString("nif", nif);
                edit.commit();
                t_nif = (TextView)findViewById(R.id.textNif);
                t_nif.setText(nif);
                UpdateSync();
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.dismiss();
            }
        })
        .setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                    if(control_cancel.equals("0")){
                        finish();
                    }else {
                        control_cancel = 1;
                        dialog.dismiss();
                    }

                return false;
            }
        });


        SharedPreferences sett = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sett.edit();
        edit.putString("nif", nif);
        edit.commit();

        AlertDialog alertdialog=builder.create();
        //alertdialog.setCanceledOnTouchOutside(false);

        alertdialog.show();

        Button nbutton = alertdialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        //nbutton.setBackgroundColor(getResources().getColor(R.color.color1));
        nbutton.setTextColor(getResources().getColor(R.color.color1));
        pbutton = alertdialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setVisibility(View.INVISIBLE);
        pbutton.setEnabled(false);
        //pbutton.setBackgroundColor(getResources().getColor(R.color.color1));
        pbutton.setTextColor(getResources().getColor(R.color.color1));

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if edittext is empty
                if (TextUtils.isEmpty(s)) {
                    // Disable ok button
                    pbutton.setEnabled(false);
                    pbutton.setVisibility(View.INVISIBLE);
                }else if (s.length()<9) {
                    // Something into edit text. Enable the button.
                    pbutton.setEnabled(false);

                    pbutton.setVisibility(View.INVISIBLE);
                } else {
                    // Disable ok button
                    pbutton.setEnabled(true);
                    pbutton.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    public void ShowPopNet(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.no_net);
        builder.setMessage("");
        builder.setPositiveButton(R.string.tenta, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton(R.string.sair,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        final AlertDialog alertdialog=builder.create();
        alertdialog.show();

        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        alertdialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int ver_net = verifyConnection();

                if(ver_net==1) {
                    alertdialog.dismiss();
                    if(!validate.equals("1")){
                        ShowPopNif();
                    }else{
                        UpdateSync();
                    }

                }
            }
        });

        Button nbutton = alertdialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        //nbutton.setBackgroundColor(getResources().getColor(R.color.color1));
        nbutton.setTextColor(getResources().getColor(R.color.color1));
        Button pbutton = alertdialog.getButton(DialogInterface.BUTTON_POSITIVE);
        //pbutton.setBackgroundColor(getResources().getColor(R.color.color1));
        pbutton.setTextColor(getResources().getColor(R.color.color1));


    }

    public void UpdateSync(){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_CONTACTS)) {

                // Show a expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Toast toast = Toast.makeText(context, "desbloquiar premissoes", duration);
                //toast.show();
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                //Toast toast = Toast.makeText(context, "permissoes desbloquiadas:", duration);
                //toast.show();
                new ServiceStubAsyncTask(MainActivity.this,MainActivity.this).execute();
            }
        }else{
            if (isNetworkAvailable()) {
                //progressBar.setVisibility(View.VISIBLE);
                //progressBar.setProgress(0);
                new ServiceStubAsyncTask(MainActivity.this, MainActivity.this).execute();
            }

        }

    }

    private void updateContact(String id, String firstname, String lastname, List<Object> arr) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        String where1 = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?";

        String[] emailParams = new String[]{id, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE};
        String[] nameParams = new String[]{id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};

        // Name
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
        builder.withSelection(where1, nameParams);
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastname);
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstname);
        ops.add(builder.build());

        for (Object obj : arr){
            Contact c = (Contact) obj;
            String type = c.getType();
            if (type.equals("5")) {
                if(c.getAtivo_contact().equals("0")){
                    String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                            ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
                    String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)};
                    // Number TELEMOVEL
                    builder = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI);
                    builder.withSelection(where, numberParams);
                    ops.add(builder.build());
                }else {

                    String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                            ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
                    String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)};

                    /*Integer ver = verifyExist(where,numberParams);
                    if(ver == 0){
                        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number())
                                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                                .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, c.getType_name())
                                .build());

                    }else{*/
                        // Number TELEMOVEL
                        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                        builder.withSelection(where, numberParams);

                        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number());
                        ops.add(builder.build());
                    //}
                }
            } else if (type.equals("6")) {
                if(c.getAtivo_contact().equals("0")){
                    String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                            ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
                    String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)};
                    // Number TELEFONE
                    builder = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI);
                    builder.withSelection(where, numberParams);
                    ops.add(builder.build());
                }else {
                    String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                            ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
                    String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)};
                    /*Integer ver = verifyExist(where,numberParams);
                    if(ver == 0){
                        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number())
                                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                                .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, c.getType_name())
                                .build());

                    }else {*/
                        // Number TELEFONE
                        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                        builder.withSelection(where, numberParams);
                        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number());
                        ops.add(builder.build());
                    //}
                }

            } else if (type.equals("1")) {
                if(c.getAtivo_contact().equals("0")){
                    String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                            ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
                    String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)};
                    // Number TELEFONE
                    //builder = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI);
                    //builder.withSelection(where, numberParams);
                    //ops.add(builder.build());
                    //Log.d("ENTROU:", " "+c.getContact_number());
                    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI).withSelection(where, numberParams) .build());
                }else {
                    String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                            ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
                    String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)};
                    /*Integer ver = verifyExist(where,numberParams);
                    if(ver == 0){
                        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number())
                                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                                .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, c.getType_name())
                                .build());

                    }else {*/
                        // Number TELEFONE
                        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                        builder.withSelection(where, numberParams);
                        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number());
                        //Log.d("ENTROU:", " " + builder);
                        ops.add(builder.build());
                        //Log.d("ENTROU1:", " " + c.getContact_number());
                    //}
                }
            } else if (type.equals("4")) {
                if(c.getAtivo_contact().equals("0")){
                    where1 = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                            ContactsContract.Data.MIMETYPE + " = ?";
                    builder = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI);
                    builder.withSelection(where1, emailParams);
                    builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, c.getContact_number());
                    ops.add(builder.build());
                }else {
                    //EMAIL
                    where1 = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                            ContactsContract.Data.MIMETYPE + " = ?";
                    builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                    /*Integer ver = verifyExist(where1,emailParams);
                    if(ver == 0){
                        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Email.DATA, c.getContact_number())
                                .build());

                    }else {*/
                        builder.withSelection(where1, emailParams);
                        builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, c.getContact_number());
                        ops.add(builder.build());
                    //}
                }
            }
        }

        // Update
        try
        {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean insertContact2(String firstName, String lastname, String id, List<Object> arr, String web, String empresa, String departamento, String funcao, Context mContext){

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withValue(ContactsContract.RawContacts.SYNC1, id)
                .build());

        //Display name/Contact name
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastname)

                .withValue(ContactsContract.RawContacts.Data.SYNC1, id)
                .build());
        Integer bb = 0;
        for (Object obj : arr){
            Contact c = (Contact) obj;
            String type = c.getType();
            if(c.getAtivo_contact().equals("1")){
                bb = 1;
                if (type.equals("5") || type.equals("7")) {
                    // Number TELEMOVEL
                    ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number())
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                            .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, c.getType_name())
                            .build());
                } else if (type.equals("6")) {
                    // Number TELEFONE
                    ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number())
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                            .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, c.getType_name())
                            .build());
                }else if (type.equals("1")) {
                    // Number TELEFONE
                    ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number())
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                            .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, c.getType_name())
                            .build());
                } else if (type.equals("4")) {
                    //EMAIL
                    ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Email.DATA, c.getContact_number())
                            .build());
                }
            }
        }

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)

                .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, departamento+" - "+funcao)
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, empresa)
                .build());

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Website.URL, web)
                .withValue(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM)
                .build());
        if(bb==0) return false;
        System.out.println(web);
        ContentProviderResult[] results;

        try {
            results = mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

        } catch (Exception e) {
            return false;
        }
        return true;

    }


    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build();
        }
        return uri;
    }



    public String getContactByWebsite(String web, Context mContext) {
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Website.URL, ContactsContract.CommonDataKinds.Website.TYPE };
        String selection = ContactsContract.Data.DELETED + " = '0'";
        String sel = ContactsContract.RawContacts.SOURCE_ID + " = ? AND " +
                ContactsContract.RawContacts.DELETED + " = '0'";
        String contactId = "";
        Cursor cur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI,null, null, null, null);
        try {
            cur.moveToFirst();
            if (cur != null  ) {
                while(cur.moveToNext()){
                    String source_id2 = "1";
                    source_id2 = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Website.DATA));
                    //if(source_id2.equals(web)){
                    if(source_id2 != null) {
                        if (source_id2.indexOf("www.itmanager.pt") != -1) {
                            contactId = cur.getString(cur.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                            deleteContact(contactId, mContext);
                        }
                    }

                }
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return contactId;
    }

    public boolean deleteContact(String id, Context mContext) {
        Cursor cur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.RawContacts.CONTACT_ID + "="
                + id, null, null);
        startManagingCursor(cur);
        while (cur.moveToNext()) {
            try {
                String lookupKey = cur.getString(cur
                        .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                        lookupKey);
                mContext.getContentResolver().delete(uri, ContactsContract.RawContacts.CONTACT_ID + "=" + id, null);
            } catch (Exception e) {
                //System.out.println(e.getStackTrace());
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    @Override
    public void onTaskComplete(String result) {
        Button fab = (Button) findViewById(R.id.button);
        //Toast.makeText(getApplicationContext(), result+" PR", Toast.LENGTH_LONG).show();
        validate = result;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("validate",validate);
        if(validate.equals("1")) {
            fab.setEnabled(true);
            fab.setClickable(true);

        }else{

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //finish();
                }
            }, 2000);

        }
    }

    public class ServiceStubAsyncTask extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private Activity mActivity;
        String response = "";
        HashMap<String, String> postDataParams;
        //private TaskCompleted mCallback;

        public ServiceStubAsyncTask(Context context, Activity activity) {
            mContext = context;
            mActivity = activity;
        }


        @Override
        protected String doInBackground(String... arg0) {
            String[] numeros = new String[20];
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            String android_id = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();

            HttpConnectionService service = new HttpConnectionService();
            response = service.sendRequest(apiPath+"?api_key="+android_id+"&nif="+nif, postDataParams);
            JSONArray jsonArray = null;
            String id;
            String firstname = "";
            String lastname = "";
            String website = "";
            String activo = "";
            String empresa = "";
            String cargo = "";
            String departamento = "";

            String fDate = "";
            int colorDate;
            //progressBar.setProgress(0);
            try {

                JSONObject jsonResponse = new JSONObject(response);
                validate = jsonResponse.getString("ativo");

                SharedPreferences sett = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor edit = sett.edit();
                edit.putString("validar", validate);

                JSONObject empr = jsonResponse.optJSONObject("empresa");

                getContactByWebsite("", mContext);

                if(validate.equals("1")) {
                    setText2(t_emp, empr.getString("empresa"),mContext);
                    setText2(t_nome,empr.getString("nome"),mContext);
                    setText2(t_nume,empr.getString("ID"),mContext);
                    setText2(t_carg,empr.getString("funcao"),mContext);
                    setText2(t_dep,empr.getString("departamento"),mContext);
                    setText2(t_nif,nif,mContext);

                    edit.putString("empresa",empr.getString("empresa"));
                    edit.putString("nome", empr.getString("nome"));
                    edit.putString("ID",empr.getString("ID"));
                    edit.putString("funcao", empr.getString("funcao"));
                    edit.putString("departamento",empr.getString("departamento"));

                    JSONObject jj = jsonResponse.optJSONObject("contacts");
                    Iterator keys = jj.keys();

                    while (keys.hasNext()) {
                        List<Object> lstObject = new ArrayList<Object>();
                        String dynamicKey = (String) keys.next();
                        JSONObject line = jj.getJSONObject(dynamicKey);

                        if (line.has("contacts")) {
                            id = line.getString("id");
                            firstname = line.getString("nome");
                            lastname = line.getString("ultimonome")+line.getString("numero_funcionario");
                            website = line.getString("Website");
                            empresa = line.getString("empresa");
                            cargo = line.getString("funcao");
                            departamento = line.getString("departamento");
                            JSONObject jja = line.optJSONObject("contacts");
                            //jsonArray = line.getJSONArray("contacts");
                            activo = line.getString("ativo");
                            Iterator keys2 = jja.keys();

                            while (keys2.hasNext()) {
                                String dynamicKey2 = (String) keys2.next();
                                JSONObject line2 = jja.getJSONObject(dynamicKey2);

                                Contact c = new Contact((String) line2.get("id"), (String) line2.get("type"), (String) line2.get("type_name"), (String) line2.get("contact_name"), (String) line2.get("contact_number"), (String) line2.get("ativo_contact"));
                                lstObject.add(c);

                                //}
                            }

                            insertContact2(firstname, lastname, id, lstObject, website, empresa, departamento, cargo, mContext);
                        }
                    }
                    Date cDate = new Date();
                    fDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(cDate);
                    colorDate = getResources().getColor(R.color.color6);
                }else{
                    nif="";

                    t_emp = (TextView) ((Activity) mContext).findViewById(R.id.textView9);
                    t_nome = (TextView) ((Activity) mContext).findViewById(R.id.textView8);
                    t_nume = (TextView) ((Activity) mContext).findViewById(R.id.textView7);
                    t_carg = (TextView) ((Activity) mContext).findViewById(R.id.textView11);
                    t_dep = (TextView) ((Activity) mContext).findViewById(R.id.textView10);
                    t_nif = (TextView) ((Activity) mContext).findViewById(R.id.textNif);

                    setText2(t_emp, "",mContext);
                    setText2(t_nome,"",mContext);
                    setText2(t_nume,"",mContext);
                    setText2(t_carg,"",mContext);
                    setText2(t_dep,"",mContext);
                    setText2(t_nif,"",mContext);

                    colorDate = mContext.getResources().getColor(R.color.color1);

                    edit.putString("empresa", "");
                    edit.putString("nome", "");
                    edit.putString("ID","");
                    edit.putString("funcao", "");
                    edit.putString("departamento","");
                    edit.putString("nif", "");

                    fDate = "Não Autorizado";
                }

                edit.putString("data_sysnc", fDate);
                edit.putString("color_sync", Integer.toString(colorDate));
                edit.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return validate;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        protected void onPostExecute(String result) {

            String fDate = "";
            int colorDate;

            if(result.equals("1")) {
                progressBar.setVisibility(View.VISIBLE);
                Date cDate = new Date();
                fDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(cDate);
                colorDate = mContext.getResources().getColor(R.color.color6);

                progressStatus=0;
                new Thread(new Runnable() {
                    public void run() {
                        while (progressStatus < 100) {
                            progressStatus += 10;
                            // Update the progress bar and display the
                            //current value in the text view
                            handler.post(new Runnable() {
                                public void run() {
                                    progressBar.setProgress(progressStatus);
                                    if(progressStatus==100){
                                        int colorDate2 = getResources().getColor(R.color.color6);
                                        TextView t_date2;
                                        Date cDate2 = new Date();
                                        String fDate2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(cDate2);
                                        //t_date2 = (TextView) findViewById(R.id.textView4);
                                        t_date.setTextColor(colorDate2);
                                        t_date.setText(fDate2);
                                    }
                                }
                            });
                            try {
                                // Sleep for 200 milliseconds.
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }else{
                colorDate = mContext.getResources().getColor(R.color.color1);
                progressBar = (ProgressBar) mActivity.findViewById(R.id.progressBar);
                progressBar.setVisibility(View.INVISIBLE);
                t_date.setTextColor(colorDate);

                fDate = "Não Autorizado";
                ShowPopNif();
                t_date.setText(fDate);

            }


            super.onPostExecute(result);
            onTaskComplete(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //System.out.println("Contact id=" + values);

        }


    }

    private void setText2(final TextView text, final String value, final Context mContext){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService( CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class ServiceStubAsyncTask2 extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private Activity mActivity;
        String response = "";
        HashMap<String, String> postDataParams;
        //private TaskCompleted mCallback;

        public ServiceStubAsyncTask2(Context context) {
            mContext = context;
        }
        /*
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }*/

        @Override
        protected String doInBackground(String... arg0) {
            String[] numeros = new String[20];
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            String android_id = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();

            HttpConnectionService service = new HttpConnectionService();
            response = service.sendRequest(apiPath+"?api_key="+android_id+"&nif="+nif, postDataParams);
            JSONArray jsonArray = null;
            String id;
            String firstname = "";
            String lastname = "";
            String website = "";
            String activo = "";
            String empresa = "";
            String cargo = "";
            String departamento = "";

            String fDate = "";
            int colorDate;
            //progressBar.setProgress(0);
            try {

                JSONObject jsonResponse = new JSONObject(response);
                validate = jsonResponse.getString("ativo");
                //validate = "0";
                SharedPreferences sett = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor edit = sett.edit();
                edit.putString("validar", validate);

                JSONObject empr = jsonResponse.optJSONObject("empresa");

                getContactByWebsite("", mContext);

                //final TextView t_emp = (TextView) ((MainActivity) mContext).findViewById(R.id.textView9);
                //final TextView t_nome = (TextView) ((MainActivity) mContext).findViewById(R.id.textView8);
                //final TextView t_nume = (TextView) ((MainActivity) mContext).findViewById(R.id.textView7);
                //final TextView t_carg = (TextView) ((MainActivity) mContext).findViewById(R.id.textView11);
                //final TextView t_dep = (TextView) ((MainActivity) mContext).findViewById(R.id.textView10);
                //final TextView t_nif = (TextView) ((MainActivity) mContext).findViewById(R.id.textNif);

                if(validate.equals("1")) {

                    //setText2(t_emp, empr.getString("empresa"),mContext);
                    //setText2(t_nome,empr.getString("nome"),mContext);
                    //setText2(t_nume,empr.getString("ID"),mContext);
                    //setText2(t_carg,empr.getString("funcao"),mContext);
                    //setText2(t_dep,empr.getString("departamento"),mContext);
                    //setText2(t_nif,nif,mContext);

                    edit.putString("empresa",empr.getString("empresa"));
                    edit.putString("nome", empr.getString("nome"));
                    edit.putString("ID",empr.getString("ID"));
                    edit.putString("funcao", empr.getString("funcao"));
                    edit.putString("departamento",empr.getString("departamento"));

                    JSONObject jj = jsonResponse.optJSONObject("contacts");
                    Iterator keys = jj.keys();

                    while (keys.hasNext()) {
                        List<Object> lstObject = new ArrayList<Object>();
                        String dynamicKey = (String) keys.next();
                        JSONObject line = jj.getJSONObject(dynamicKey);

                        if (line.has("contacts")) {
                            id = line.getString("id");
                            firstname = line.getString("nome");
                            lastname = line.getString("ultimonome")+line.getString("numero_funcionario");
                            website = line.getString("Website");
                            empresa = line.getString("empresa");
                            cargo = line.getString("funcao");
                            departamento = line.getString("departamento");
                            JSONObject jja = line.optJSONObject("contacts");
                            //jsonArray = line.getJSONArray("contacts");
                            activo = line.getString("ativo");
                            Iterator keys2 = jja.keys();

                            while (keys2.hasNext()) {
                                String dynamicKey2 = (String) keys2.next();
                                JSONObject line2 = jja.getJSONObject(dynamicKey2);

                                Contact c = new Contact((String) line2.get("id"), (String) line2.get("type"), (String) line2.get("type_name"), (String) line2.get("contact_name"), (String) line2.get("contact_number"), (String) line2.get("ativo_contact"));
                                lstObject.add(c);

                                //}
                            }

                            insertContact2(firstname, lastname, id, lstObject, website, empresa, departamento, cargo,mContext);
                        }
                    }
                    Date cDate = new Date();
                    fDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(cDate);
                    colorDate = mContext.getResources().getColor(R.color.color6);
                }else{

                    colorDate = mContext.getResources().getColor(R.color.color1);

                    //setText2(t_emp, "",mContext);
                    //setText2(t_nome,"",mContext);
                    //setText2(t_nume,"",mContext);
                    //setText2(t_carg,"",mContext);
                   // setText2(t_dep,"",mContext);
                    //setText2(t_nif,"",mContext);
                    nif="";  
                    edit.putString("empresa", "");
                    edit.putString("nome", "");
                    edit.putString("ID","");
                    edit.putString("funcao", "");
                    edit.putString("departamento","");

                    fDate = "Não Autorizado";
                }

                edit.putString("data_sysnc", fDate);
                edit.putString("color_sync", Integer.toString(colorDate));
                edit.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            return validate;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }


        @Override
        protected void onProgressUpdate(Integer... values) {

        }


    }
    
}