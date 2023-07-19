package com.example.sent_sms_identification;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private EditText phoneNumberEditText;
    private EditText messageEditText;
    private Button sendButton;
    public static final String SMS_SENT_ACTION = "com.example.sent_sms_identification.SMS_SENT_ACTION";
    public static final String SMS_DELIVERED_ACTION = "com.example.sent_sms_identification.SMS_DELIVERED_ACTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEditText.getText().toString().trim();
            String message = messageEditText.getText().toString().trim();

            if (phoneNumber.isEmpty() || message.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter phone number and message", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                    sendSMS(phoneNumber, message);
                } else {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
                }
            }

        });

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = null;
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        message = "Message Sent Successfully";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        message = "Error";
                        break;
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(SMS_SENT_ACTION));


        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Message Delivered", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(SMS_DELIVERED_ACTION));
    }


    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message,
                PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT_ACTION), PendingIntent.FLAG_IMMUTABLE),
                PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED_ACTION), PendingIntent.FLAG_IMMUTABLE));
        Toast.makeText(this, "Message sent triggered", Toast.LENGTH_SHORT).show();
        String selection = Telephony.Sms.ADDRESS + " = ? AND " + Telephony.Sms.BODY + " = ?";
        String[] selectionArgs = {phoneNumber, message};
        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null, selection, selectionArgs, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                int status = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.STATUS));

                Log.d("MyApp", "Address: " + address);
                Log.d("MyApp", "Body: " + body);
                Log.d("MyApp", "Status: " + status);
            }
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                String message = messageEditText.getText().toString().trim();

                if (!phoneNumber.isEmpty() && !message.isEmpty()) {
                    sendSMS(phoneNumber, message);
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}