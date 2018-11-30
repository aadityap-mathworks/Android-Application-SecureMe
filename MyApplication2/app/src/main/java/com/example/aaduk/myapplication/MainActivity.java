package com.example.aaduk.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private TextView panic_message;
    private GestureDetectorCompat gd;

    public static final String EXTRA_MESSAGE = "com.example.aaduk.myapplication.MESSAGE";
    private static final int CONTACT_PICKER_RESULT = 0x00;
    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 8888;
    public static final String TAG = "My Application";
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 8888;
    private String displayName;
    private String phoneNumber;
    private String emailAddress = "test@test.foo";
    private TextView contactTextView;
    private TextView panicMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button chooseContactButton = (Button) findViewById(R.id.button_choose_contacts);
        chooseContactButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pickContacts();

            }
        });

        contactTextView = (TextView) findViewById(R.id.ContactTextView);
        panicMessage = (TextView) findViewById(R.id.panic_message);
        Button button_send = (Button) findViewById(R.id.button_send);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        this.gd = new GestureDetectorCompat(this, this);
        gd.setOnDoubleTapListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.gd.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onDown(MotionEvent event){
        return true;
        }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY){

        return true;
    }

    @Override
    public void onLongPress(MotionEvent event){

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        sendMessage();
        return true;
    }

   @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    public void sendMessage()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            String message = panicMessage.getText().toString();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }

    }


    private void pickContacts() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
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
                        new String[]{
                                id,
                        }, null);

                if (cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
                    phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                    contactTextView.setText(displayName + "/" + phoneNumber);
                }
                break;

        }
    }

}