package pt.manager.maneger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskCompleted {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private String apiPath = "http://itmanager.pt/mobile_contacts/get_contacts.php";
    private ProgressBar progressBar;
    public static String validate = "0";
    public static String nif = "";
    public static String data_sysnc;
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    public static TextView t_date;
    PendingIntent myPendingIntent;
    AlarmManager alarmManager;
    BroadcastReceiver myBroadcastReceiver;
    Calendar firingCal;
    private static final String TAG ="MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        validate = settings.getString("validar", "0");
        nif = settings.getString("nif", "0");
        int ColorDate = Integer.parseInt(settings.getString("color_sysnc", "0"));
        String data = settings.getString("data_sysnc", "0");

        TextView t_nif = (TextView)findViewById(R.id.textNif);
        t_nif.setText(nif);

        t_date = (TextView)findViewById(R.id.textView4);
        t_date.setTextColor(ColorDate);
        t_date.setText(data);

        TextView t_emp = (TextView)findViewById(R.id.textView7);
        TextView t_nome = (TextView)findViewById(R.id.textView8);
        TextView t_nume = (TextView)findViewById(R.id.textView9);
        TextView t_carg = (TextView)findViewById(R.id.textView10);
        TextView t_dep = (TextView)findViewById(R.id.textView11);

        String empresa = "";
        String nome = "";
        String ID = "";
        String funcao = "";
        String departamento = "";

        empresa = settings.getString("empresa", "0");
        nome = settings.getString("nome", "0");
        ID = settings.getString("ID", "0");
        funcao = settings.getString("funcao", "0");
        departamento = settings.getString("departamento", "0");


        setText(t_emp, empresa);
        setText(t_nome,nome);
        setText(t_nume,ID);
        setText(t_carg,funcao);
        setText(t_dep,departamento);

        Log.v("validade;",":"+validate);

        if(!validate.equals("1")){
            ShowPopNif();
        }else{
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
                Log.v("entrou;","aqui 2");
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
                Log.v("entrou;","aqui 2");
                UpdateSync();
            }
        });


    }

    public void ShowPopNif(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_dialog);
        builder.setMessage(R.string.msg_dialog);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton(R.string.confirm,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nif = input.getText().toString();
                Log.v("entrou;","aqui 1");

                TextView t_nif = (TextView)findViewById(R.id.textNif);
                t_nif.setText(nif);
                UpdateSync();
            }
        });

        SharedPreferences sett = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sett.edit();
        edit.putString("nif", nif);
        edit.commit();

        AlertDialog alertdialog=builder.create();
        alertdialog.setCanceledOnTouchOutside(false);
        alertdialog.show();


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
                Toast toast = Toast.makeText(context, "desbloquiar premissoes", duration);
                toast.show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                Toast toast = Toast.makeText(context, "permissoes desbloquiadas:", duration);
                toast.show();
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
                    Log.d("ENTROU:", " "+c.getContact_number());
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

    public boolean insertContact2(String firstName, String lastname, String id, List<Object> arr, String web, String empresa, String departamento, String funcao){

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
                if (type.equals("5")) {
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
        Log.v("SIZE", String.valueOf(arr.size()));
        if(bb==0) return false;
        System.out.println(web);
        ContentProviderResult[] results;

        try {
            results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

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

    public Integer verifyExist(String where, String[] parm) {
        Integer exist = 0;

        String[] projection = new String[] { ContactsContract.CommonDataKinds.Website.URL, ContactsContract.CommonDataKinds.Website.TYPE };
        String contactId = "";
        Cursor cur = getContentResolver().query(ContactsContract.Data.CONTENT_URI,projection, where, parm, null);
        try {
            cur.moveToFirst();
            if (cur != null  ) {
                while(cur.moveToNext()){
                    String source_id2 = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    exist = 1;
                }
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }


        return exist;
    }

    public String getSy(String id){
        ContentResolver cr = this.getContentResolver();
        String[] projection = new String[] { ContactsContract.RawContacts._ID, ContactsContract.RawContacts.CONTACT_ID, ContactsContract.RawContacts.SYNC1, ContactsContract.RawContacts.SOURCE_ID };
        String selection = ContactsContract.Contacts._ID + " = '" + id + "' AND " + ContactsContract.RawContacts.DELETED + " = '0'";
        Cursor cur = cr.query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, null, null);
        String sync1 = "";
        System.out.println(selection);
        while (cur.moveToNext()) {
            Long rawId = cur.getLong(0);
            Long contactId = cur.getLong(1);
            sync1 = cur.getString(2);
            String source = cur.getString(2);

            System.out.println("Contact id=" + contactId + " raw-id=" + rawId + " sync1=" + sync1 + " source=" + source);
        }

        cur.close();
        return sync1;
    }

    public String getContactBySYSNC(String sysnc_id) {
        String[] projection = new String[] { ContactsContract.RawContacts._ID, ContactsContract.RawContacts.CONTACT_ID, ContactsContract.RawContacts.SYNC1, BaseColumns._ID };
        String selection = ContactsContract.RawContacts.SYNC1 + " = '" + sysnc_id + "' AND " + ContactsContract.RawContacts.DELETED + " = '0'";
        Cursor cur = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, selection, null, null);
        String contactId = "";
        try {
            cur.moveToFirst();
            if (cur != null  ) {
                while(cur.moveToNext()){
                    contactId = cur.getString(cur.getColumnIndex( BaseColumns._ID ));
                    Log.v("CONTACTO ID:","1_"+contactId);
                }
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return contactId;
    }

    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "?";
        String contactId = "";
        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        return contactId;
    }

    public String getContactByWebsite(String web) {
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Website.URL, ContactsContract.CommonDataKinds.Website.TYPE };
        String selection = ContactsContract.Data.DELETED + " = '0'";
        String sel = ContactsContract.RawContacts.SOURCE_ID + " = ? AND " +
                ContactsContract.RawContacts.DELETED + " = '0'";
        String contactId = "";
        Cursor cur = getContentResolver().query(ContactsContract.Data.CONTENT_URI,null, null, null, null);
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
                            deleteContact(contactId);
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

    public boolean deleteContact(String id) {
        Cursor cur = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.RawContacts.CONTACT_ID + "="
                + id, null, null);
        startManagingCursor(cur);
        while (cur.moveToNext()) {
            try {
                String lookupKey = cur.getString(cur
                        .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                        lookupKey);
                getContentResolver().delete(uri, ContactsContract.RawContacts.CONTACT_ID + "=" + id, null);
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
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

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast toast = Toast.makeText(MainActivity.this, "permission was granted", Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast toast = Toast.makeText(MainActivity.this, "permission denied", Toast.LENGTH_SHORT);
                    toast.show();
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
        Log.v("valido", "V:"+validate);
        if(validate.equals("1")) {
            fab.setEnabled(true);
            fab.setClickable(true);
            //Toast.makeText(getApplicationContext(), "tem permissões", Toast.LENGTH_LONG).show();
        }else{
            fab.setEnabled(false);
            fab.setClickable(false);
            Toast.makeText(getApplicationContext(), "Não tem permissões", Toast.LENGTH_LONG).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //finish();
                }
            }, 2000);

        }
    }

    private class ServiceStubAsyncTask extends AsyncTask<String, String, String> {

        private Context mContext;
        private Activity mActivity;
        String response = "";
        HashMap<String, String> postDataParams;
        //private TaskCompleted mCallback;



        public ServiceStubAsyncTask(Context context, Activity activity) {
            mContext = context;
            mActivity = activity;
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
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
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

            try {
                JSONObject jsonResponse = new JSONObject(response);
                validate = jsonResponse.getString("ativo");
                //validate = "0";
                SharedPreferences sett = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor edit = sett.edit();
                edit.putString("validar", validate);


                //String id_cont = getContactByWebsite("");

                JSONObject empr = jsonResponse.optJSONObject("empresa");

                TextView t_emp = (TextView)findViewById(R.id.textView7);
                TextView t_nome = (TextView)findViewById(R.id.textView8);
                TextView t_nume = (TextView)findViewById(R.id.textView9);
                TextView t_carg = (TextView)findViewById(R.id.textView10);
                TextView t_dep = (TextView)findViewById(R.id.textView11);



                if(validate.equals("1")) {
                    setText(t_emp, empr.getString("empresa"));
                    setText(t_nome,empr.getString("nome"));
                    setText(t_nume,empr.getString("ID"));
                    setText(t_carg,empr.getString("funcao"));
                    setText(t_dep,empr.getString("departamento"));

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
                            lastname = line.getString("ultimonome")+" - "+line.getString("numero_funcionario");
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
                                //for (int i = 0; i < jja.length(); i++) {
                                //String a = jsonArray.getString(i);
                                //JSONObject resultJsonObject = new JSONObject(a);

                                Contact c = new Contact((String) line2.get("id"), (String) line2.get("type"), (String) line2.get("type_name"), (String) line2.get("contact_name"), (String) line2.get("contact_number"), (String) line2.get("ativo_contact"));
                                lstObject.add(c);

                                //}
                            }

                            insertContact2(firstname, lastname, id, lstObject, website, empresa, departamento, cargo);
                        }
                    }
                }else{
                    setText(t_emp, "");
                    setText(t_nome,"");
                    setText(t_nume,"");
                    setText(t_carg,"");
                    setText(t_dep,"");
                }
                edit.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return validate;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        protected void onPostExecute(String result) {
            //progressBar.setVisibility(View.GONE);
            String fDate = "";
            int colorDate;
            SharedPreferences.Editor editor = settings.edit();
            t_date = (TextView)findViewById(R.id.textView4);
            if(result.equals("1")) {
                Date cDate = new Date();
                fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(cDate);
                Log.v("Date:", result);
                colorDate = getResources().getColor(R.color.color6);
                t_date.setTextColor(colorDate);
            }else{
                colorDate = getResources().getColor(R.color.color1);
                t_date.setTextColor(colorDate);
                fDate = "Não Autorizado";

            }

            SharedPreferences sett = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor edit = sett.edit();
            edit.putString("data_sysnc", fDate);
            edit.putString("color_sync", Integer.toString(colorDate));
            edit.commit();

            t_date.setText(fDate);
            //progressBar.setProgress(100);

            super.onPostExecute(result);
            onTaskComplete(result);
        }

        protected void onProgressUpdate(Integer... progress) {
            System.out.println("Contact id=" + progress);
            //progressBar.setProgress(progress[0]);
        }


        /*
        @Override
        protected void onProgressUpdate(Integer... values) {
            System.out.println("Contact id=" + values);
            //progressBar.setProgress(values[0]);
        }*/


    }

    private void setText(final TextView text,final String value){
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
}
