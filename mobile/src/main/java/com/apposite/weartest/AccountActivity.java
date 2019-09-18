package com.apposite.weartest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class AccountActivity extends AppCompatActivity {

    private final String TAG = "log_cat";

    EditText name, uid, accNum, branchCode;
    Button save, setSecurity;

    private DatabaseReference mDatabase;
    private int timesBackPressed = 2;

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

        save = findViewById(R.id.btnSaveUser);
        setSecurity = findViewById(R.id.btnSetSecurity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nameVal = name.getText().toString();
                String uidVal = uid.getText().toString() + getString(R.string.peezy);
                String accNumVal = accNum.getText().toString();
                String branchCodeVal = branchCode.getText().toString();

                if (securityDone && validUId) {
                    saveDetails(nameVal, uidVal, accNumVal, branchCodeVal);
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
}
