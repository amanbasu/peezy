package com.apposite.weartest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class QuickPayActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int QR_REQUEST_CODE = 2;
    private final int MIC_AMOUNT_REQUEST_CODE = 3;

    private int timesBackPressed = 2;

    EditText uid, amount;
    Button pay;
    ImageButton micAmount, scan;

    private final String TAG = "log_cat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_pay);

        uid = findViewById(R.id.etQPUid);
        amount = findViewById(R.id.etQPAmount);
        pay = findViewById(R.id.btnQPPay);
        micAmount = findViewById(R.id.ibQPMicAmount);
        scan = findViewById(R.id.ibQPScanQR);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO  authenticate user and pay

                makePayment();
            }
        });

        micAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String DIALOG_TEXT = "Speech recognition demo";
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, DIALOG_TEXT);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MIC_AMOUNT_REQUEST_CODE);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                startActivityForResult(intent, MIC_AMOUNT_REQUEST_CODE);
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });

        try{
            String friend_uid = getIntent().getStringExtra("FRIEND_UID");

            uid.setText(friend_uid);
            Log.d(TAG, "QuickPay activity launched from Friend List.");
        } catch (Exception e) {
            Log.d(TAG, "Normal QP activity launch");
        }
    }

    private void makePayment() {
        uid.setText("");
        amount.setText("");

        Toast.makeText(this, "Payment Successful.", Toast.LENGTH_SHORT).show();
    }

    private void checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            startQRScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    startQRScanning();
                }
                break;
        }
    }

    private void startQRScanning(){
        startActivityForResult(new Intent(this, QrCodeScanner.class), QR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_REQUEST_CODE){
            try {
                String uidVal = data.getStringExtra("SCAN_RESULT");
                uid.setText(uidVal);
            } catch (Exception e) {}
        }

        ArrayList<String> speech;
        if (resultCode == RESULT_OK) {
            if (requestCode == MIC_AMOUNT_REQUEST_CODE) {
                speech = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                amount.setText(speech.get(0));
                //you can set resultSpeech to your EditText or TextView
            }
        }
    }

    public void openProfileActivity(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }


    public void openAccountActivity(View view) {
        startActivity(new Intent(this, AccountActivity.class));
        finish();
    }

    public void openFriendActivity(View view) {
        startActivity(new Intent(this, FriendsActivity.class));
        finish();
    }

    public void openMainActivity(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(timesBackPressed < 1){
            finish();
        }
        timesBackPressed -= 1;
        Toast.makeText(this, "Press back one more time to exit.", Toast.LENGTH_SHORT).show();
    }
}
