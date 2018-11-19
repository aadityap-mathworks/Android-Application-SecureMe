package com.example.aaduk.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.aaduk.myapplication.MESSAGE";
    private static final int CONTACT_PICKER_RESULT = 0x00;
    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 8888;
    public static final String TAG = "My Application";
    private String displayName;
    private String phoneNumber;
    private String emailAddress = "test@test.foo";
    private TextView contactTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button chooseContactButton = (Button) findViewById(R.id.button_choose_contacts);
        chooseContactButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pickContacts ();

            }
        });
        contactTextView = (TextView) findViewById(R.id.ContactTextView);


    }


    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);

    }


    private void pickContacts ()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else
        {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case CONTACT_PICKER_RESULT:
                if (data == null)
                    return;
                Uri uri = data.getData();
                String id = uri.getLastPathSegment();
                Log.i(TAG, uri + "");

                String[] projection = {
                        Phone.DISPLAY_NAME,
                        Phone.NUMBER
                };
                Cursor cursor = getContentResolver().query(Phone.CONTENT_URI,
                        projection,
                        Phone.CONTACT_ID + " = ? ",
                        new String[] {
                                id,
                        }, null);

                if (cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
                    phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                    contactTextView.setText(displayName + "/" + phoneNumber);

                    //setTriggerData();

                }
                break;

        }
    }


}
