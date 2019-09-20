package com.apposite.weartest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AddFriendActivity extends AppCompatActivity {

    EditText name, uid;
    Button save;
    ImageButton scanQR, micName, micUid;

    private DatabaseReference mDatabase;

    GoogleSignInAccount account;

    private final String TAG = "log_cat";
    private boolean validUId = false;
    private boolean validName = false;
    String myUId;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int QR_REQUEST_CODE = 3;
    private final int MIC_NAME_REQUEST_CODE = 4;
    private final int MIC_UID_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        name = findViewById(R.id.etFriendName);
        uid = findViewById(R.id.etFriendUId);
        save = findViewById(R.id.btnSaveFriend);
        scanQR = findViewById(R.id.ibScanQR);
        micName = findViewById(R.id.ibMicName);
        micUid = findViewById(R.id.ibMicUid);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        account = GoogleSignIn.getLastSignedInAccount(this);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);
        myUId = sharedPref.getString("uid", "");

        uid.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                String tempUid = editable.toString();
                if (tempUid.equals(myUId)){
                    uid.setError("You can't add your own UId.");
                } else {
                    checkValidUId(tempUid);
                }
            }
        });

        name.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                checkValidName(editable.toString());
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validUId && validName){
                    saveDetails(name.getText().toString(), uid.getText().toString());
                }
            }
        });

        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });

        micName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String DIALOG_TEXT = "Speech recognition demo";
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, DIALOG_TEXT);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MIC_NAME_REQUEST_CODE);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                startActivityForResult(intent, MIC_NAME_REQUEST_CODE);
            }
        });

        micUid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String DIALOG_TEXT = "Speech recognition demo";
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, DIALOG_TEXT);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MIC_UID_REQUEST_CODE);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                startActivityForResult(intent, MIC_UID_REQUEST_CODE);
            }
        });
    }

    private void startQRScanning(){
        startActivityForResult(new Intent(this, QrCodeScanner.class), QR_REQUEST_CODE);
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

    private void checkValidUId(final String uidVal){
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "checking stored users " + dataSnapshot.toString());

                validUId = false;
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if (data.getKey().equals(uidVal)){
                        validUId = true;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                validUId = false;
            }
        });

        if(!validUId){
            uid.setError("The given UId is invalid.");
        }

        mDatabase.child("friends").child(myUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "checking stored users " + dataSnapshot.toString());

                validUId = true;
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Log.d(TAG, "checking sub data " + data.getValue().toString());
                    Friends friendData = data.getValue(Friends.class);

                    if (friendData.getUid().equals(uidVal)) {
                        validUId = false;
                        uid.setError("The given UId already belongs to a friend.");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                validUId = false;
            }
        });

    }

    private void checkValidName(final String nameVal){
        mDatabase.child("friends").child(myUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d(TAG, "checking stored friends " + dataSnapshot.toString());
                validName = true;

                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if (data.getKey().equalsIgnoreCase(nameVal)){
                        validName = false;
                        name.setError("A person with this name already exists.");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveDetails(final String mName, final String mUId) {

        Log.d(TAG, "Adding values: " + mName + ", " + mUId);
        mDatabase.child("friends").child(myUId).child(mName).setValue(new Friends(mUId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        uid.setText("");
                        name.setText("");
                        Toast.makeText(getApplicationContext(), mName + " added to your friends.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Data not updated! Please check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_REQUEST_CODE){
            try {
                String uidVal = data.getStringExtra("SCAN_RESULT");
                uid.setText(uidVal);
            }
            catch (Exception e){

            }
        }

        ArrayList<String> speech;
        if (resultCode == RESULT_OK) {
            if (requestCode == MIC_NAME_REQUEST_CODE) {
                speech = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                name.setText(speech.get(0));
                //you can set resultSpeech to your EditText or TextView
            } else if (requestCode == MIC_UID_REQUEST_CODE) {
                speech = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String temp_uid = speech.get(0);
                temp_uid = temp_uid.replaceAll("\\s+","");
                uid.setText(temp_uid);
                //you can set resultSpeech to your EditText or TextView
            }
        }

    }
}
