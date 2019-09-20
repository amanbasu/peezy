package com.apposite.weartest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private static final int MIC_NAME_REQUEST_CODE = 1;
    private static final int MIC_UID_REQUEST_CODE = 2;
    private static final int MIC_ACC_NUM_REQUEST_CODE = 3;
    private static final int MIC_B_CODE_REQUEST_CODE = 4;
    private final String TAG = "log_cat";

    EditText name, uid, accNum, branchCode;
    Button save, setSecurity;
    ImageButton micName, micUId, micAccNum, micBranchCode;

    private DatabaseReference mDatabase;
    private int timesBackPressed = 1;

    private boolean validUId = false;
    private boolean securityDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        name = findViewById(R.id.etName);
        uid = findViewById(R.id.etUId);
        accNum = findViewById(R.id.etAccountNo);
        branchCode = findViewById(R.id.etBranch);

        micName = findViewById(R.id.ibAcMicName);
        micUId = findViewById(R.id.ibAcMicUId);
        micAccNum = findViewById(R.id.ibAcMicAccNum);
        micBranchCode = findViewById(R.id.ibAcMicBrCode);

        save = findViewById(R.id.btnSaveUser);
        setSecurity = findViewById(R.id.btnSetSecurity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nameVal = name.getText().toString();
                String uidVal = uid.getText().toString().toLowerCase() + getString(R.string.peezy);
                String accNumVal = accNum.getText().toString();
                String branchCodeVal = branchCode.getText().toString();

                if (securityDone && validUId) {
                    if (save.getText().equals("Update")) {
                        updateDetails(nameVal, uidVal, accNumVal, branchCodeVal);
                    }
                    else {
                        saveDetails(nameVal, uidVal, accNumVal, branchCodeVal);
                    }
                }
            }
        });

        setSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: security logic

                ECGAuth ecgAuth = new ECGAuth();

                showPD();
            }
        });

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);
        String myUid = sharedPref.getString("uid", "");

        if (!myUid.isEmpty()) {
            save.setText("Update");
            int id = myUid.indexOf('@');
            uid.setText(myUid.substring(0, id));
            uid.setEnabled(false);
            validUId = true;
        } else {
            uid.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(final Editable editable) {
                    String tempUId = editable.toString() + getString(R.string.peezy);
                    checkUniqueUId(tempUId);
                }
            });
        }

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

        micUId.setOnClickListener(new View.OnClickListener() {
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

        micAccNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String DIALOG_TEXT = "Speech recognition demo";
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, DIALOG_TEXT);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MIC_ACC_NUM_REQUEST_CODE);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                startActivityForResult(intent, MIC_ACC_NUM_REQUEST_CODE);
            }
        });

        micBranchCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String DIALOG_TEXT = "Speech recognition demo";
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, DIALOG_TEXT);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MIC_B_CODE_REQUEST_CODE);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                startActivityForResult(intent, MIC_B_CODE_REQUEST_CODE);
            }
        });
    }

    private void updateDetails(final String nameVal, final String uidVal, final String accNumVal, final String branchCodeVal) {
        if (nameVal.equals("")){
            name.setError("Name cannot be empty.");
            return;
        }

        if (accNumVal.equals("")){
            name.setError("Account number cannot be empty.");
            return;
        }

        if (branchCodeVal.equals("")){
            name.setError("Branch code cannot be empty.");
            return;
        }

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);
        String email = sharedPref.getString(getString(R.string.email), "");

        NewUser user = new NewUser(email, nameVal, accNumVal, branchCodeVal);
        mDatabase.child("users").child(uidVal).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addSharedPref(uidVal, nameVal);

                        name.setText("");
                        uid.setEnabled(true);
                        uid.setText("");
                        accNum.setText("");
                        branchCode.setText("");

                        Log.d(TAG, "user data inserted.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Data not updated! Please check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPD(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Authenticating your ECG...");
        pd.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "You are now SECURED!", Toast.LENGTH_SHORT).show();
                securityDone = true;
                save.setVisibility(View.VISIBLE);
                pd.dismiss();
            }
        }, 3000);
    }

    private void checkUniqueUId(final String tempUId){
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "checking stored UIds " + dataSnapshot.toString());
                validUId = true;

                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if (data.getKey()!=null && data.getKey().equals(tempUId)) {
                        validUId = false;
                        uid.setError("This uid has already been taken.");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveDetails(final String nameVal, final String uidVal, final String accNumVal, final String branchCodeVal) {

        if (nameVal.equals("")){
            name.setError("Name cannot be empty.");
            return;
        }

        if (accNumVal.equals("")){
            name.setError("Account number cannot be empty.");
            return;
        }

        if (branchCodeVal.equals("")){
            name.setError("Branch code cannot be empty.");
            return;
        }

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);
        String email = sharedPref.getString(getString(R.string.email), "");

        NewUser user = new NewUser(email, nameVal, accNumVal, branchCodeVal);
        mDatabase.child("users").child(uidVal).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addSharedPref(uidVal, nameVal);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        Log.d(TAG, "user data inserted.");
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

    public void addSharedPref(String uid, String name){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("uid", uid);
        editor.putString("name", name);
        editor.apply();
        editor.commit();

        Log.d(TAG, "email, uid added in shared preference");
    }

    public void openMainActivity(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void openFriendsActivity(View view) {
        startActivity(new Intent(this, FriendsActivity.class));
        finish();
    }

    public void openQuickPayActivity(View view) {
        startActivity(new Intent(this, QuickPayActivity.class));
        finish();
    }

    public void openProfileActivity(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        ArrayList<String> speech;
        if (resultCode == RESULT_OK) {
            speech = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (requestCode == MIC_NAME_REQUEST_CODE) {
                name.setText(speech.get(0));
            } else if (requestCode == MIC_UID_REQUEST_CODE) {
                String temp_uid = speech.get(0);
                temp_uid = temp_uid.replaceAll("\\s+","");
                uid.setText(temp_uid);
                //you can set resultSpeech to your EditText or TextView
            } else if (requestCode == MIC_ACC_NUM_REQUEST_CODE) {
                String temp_uid = speech.get(0);
                temp_uid = temp_uid.replaceAll("\\s+","");
                accNum.setText(temp_uid);
                //you can set resultSpeech to your EditText or TextView
            } else if (requestCode == MIC_B_CODE_REQUEST_CODE) {
                String temp_uid = speech.get(0);
                temp_uid = temp_uid.replaceAll("\\s+","");
                branchCode.setText(temp_uid);
                //you can set resultSpeech to your EditText or TextView
            }
        }

    }
}
