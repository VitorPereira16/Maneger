package pt.manager.maneger;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private String apiPath = "http://itmanager.pt/mobile_contacts/get_contacts.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //fetchContacts();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();

                CharSequence text = "Hello toast!";
                int duration = Toast.LENGTH_SHORT;
                //getVCF(context);

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_CONTACTS)) {

                        // Show an expanation to the user *asynchronously* -- don't block
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
                        new ServiceStubAsyncTask(MainActivity.this, MainActivity.this).execute();
                    }
                    //addContact("Coderz","1234567890");
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
                    while (phones.moveToNext())
                    {
                        String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //Log.d("Name:", name+" "+phoneNumber);
                    }
                    phones.close();
                }

            }
        });


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
                String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
                String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)};
                // Number TELEMOVEL
                builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                builder.withSelection(where, numberParams);
                builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number());
                ops.add(builder.build());
            } else if (type.equals("1")) {
                String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
                String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};
                // Number TELEFONE
                builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                builder.withSelection(where, numberParams);
                builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number());
                ops.add(builder.build());
            } else if (type.equals("4")) {
                //EMAIL
                builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                builder.withSelection(where1, emailParams);
                builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, c.getContact_number());
                ops.add(builder.build());
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

    public boolean insertContact(String firstName, String lastname, String id, List<Object> arr) {
        //ContentResolver resolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.SYNC1, id)
                .build());

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastname)
                .build());

        for (Object obj : arr){
            Contact c = (Contact) obj;
            String type = c.getType();
            if (type.equals("5")) {
                // Number TELEMOVEL
                ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            } else if (type.equals("1")) {
                // Number TELEFONE
                ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, c.getContact_number())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
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

        try {
            //resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
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


    public void fetchContacts() {

        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        String Notes = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        StringBuffer output = new StringBuffer();

        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                Log.d("ID:",contact_id);
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                Log.d("NAME:",name);
                //String sync = cursor.getString(cursor.getColumnIndex( ContactsContract.RawContacts.SYNC1 ));
                //Log.d("SYNC:",sync);
                //String SYSNC = cursor.getString(cursor.getColumnIndex( ContactsContract.RawContacts.SYNC1 ));

                //String sync1 = null;
                //int sync1Index = cursor.getColumnIndex(ContactsContract.RawContacts.SYNC1);

                //System.out.println(" sync1=" + sync1);
                //int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                /*
                if (hasPhoneNumber > 0) {

                    //output.append("\n First Name:" + name);
                    Log.d("Contact ID:",contact_id);
                    Log.d("First Name:",name);
                    //Log.d("SYSNC:",SYSNC);

                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        //output.append("\n Phone number:" + phoneNumber);
                        //Log.d("Phone number:",phoneNumber);
                    }

                    phoneCursor.close();

                    // Query and loop for every email of the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,	null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);

                    while (emailCursor.moveToNext()) {

                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                        //Log.d("EMAIL:",email);
                        //output.append("\nEmail:" + email);

                    }

                    emailCursor.close();
                }*/
            }

        }
    }

    public String getContactBySYSNC(String sysnc_id) {
        ContentResolver cr = this.getContentResolver();
        //String[] projection = new String[] { ContactsContract.RawContacts._ID, ContactsContract.RawContacts.CONTACT_ID, ContactsContract.RawContacts.SYNC1, BaseColumns._ID };
        String selection = ContactsContract.RawContacts.SYNC1 + " = '" + sysnc_id + "' AND " + ContactsContract.RawContacts.DELETED + " = '0'";
        Cursor cur = cr.query(ContactsContract.RawContacts.CONTENT_URI, null, selection, null, null);
        String contactId = "";

        try {
            if (cur != null && cur.getCount() > 0) {
                cur.moveToNext();
                contactId = cur.getString(cur.getColumnIndex( ContactsContract.RawContacts._ID ));
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

    public String getContactDisplayNameByNote(String number) {
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        StringBuffer output = new StringBuffer();

        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts._ID ));
                String name = cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME ));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER )));

                if (hasPhoneNumber > 0) {
                    Cursor noteCursor = null;
                    try {
                        noteCursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                                new String[] {ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Note.NOTE},
                                ContactsContract.Data.RAW_CONTACT_ID + "=?" + " AND "
                                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE + "'",
                                new String[] {contact_id}, null);

                        if (noteCursor != null && noteCursor.moveToFirst()) {
                            String note = noteCursor.getString(noteCursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                            Log.d("Note: ",note);
                        }
                    } finally {
                        if (noteCursor != null) {
                            noteCursor.close();
                        }
                    }
                }

            }

        }
        return "ok";
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class ServiceStubAsyncTask extends AsyncTask<Void, Void, Void> {

        private Context mContext;
        private Activity mActivity;
        String response = "";
        HashMap<String, String> postDataParams;



        public ServiceStubAsyncTask(Context context, Activity activity) {
            mContext = context;
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String[] numeros = new String[20];
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            String android_id = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            Log.d("Name:", apiPath+"?api_key="+android_id);

            HttpConnectionService service = new HttpConnectionService();
            response = service.sendRequest(apiPath+"?api_key="+android_id, postDataParams);
            JSONArray jsonArray = null;
            String id;
            String firstname = "";
            String lastname = "";

            try {
                JSONObject jsonResponse = new JSONObject(response);
                Iterator keys = jsonResponse.keys();
                while(keys.hasNext()) {
                    List<Object> lstObject = new ArrayList<Object>();
                    String dynamicKey = (String) keys.next();
                    JSONObject line = jsonResponse.getJSONObject(dynamicKey);
                    if (line.has("contacts")) {
                        id = line.getString("id");
                        firstname = line.getString("nome");
                        lastname = line.getString("ultimonome");
                        jsonArray = line.getJSONArray("contacts");
                        for (int i = 0; i < jsonArray.length(); i++) {

                            String a = jsonArray.getString(i);
                            JSONObject resultJsonObject = new JSONObject(a);
                            Contact c = new Contact((String) resultJsonObject.get("id"), (String) resultJsonObject.get("type"), (String) resultJsonObject.get("type_name"), (String) resultJsonObject.get("contact_name"), (String) resultJsonObject.get("contact_number"));
                            lstObject.add(c);
                        }

                        //System.out.println("ID: " + id);
                        String id_cont = getContactBySYSNC(id);
                        //System.out.println("IDC: " + id_cont);
                        if (id_cont != null && !id_cont.isEmpty()) {
                            Log.d("UPDATE:", "EXISTE");
                            updateContact(id_cont, firstname, lastname, lstObject);
                        } else {
                            Log.d("INSERT:", "NAO EXISTE");
                            insertContact(firstname, lastname, id, lstObject);
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService( CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
