package com.apposite.weartest;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendsActivity extends AppCompatActivity {

    private int timesBackPressed = 1;

    private DatabaseReference mDatabase;

    GridView friendView;

    String myUId;
    private final String TAG = "log_cat";
    ArrayList<String> friendList, friendUIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);
        myUId = sharedPref.getString("uid", "");

        friendView = findViewById(R.id.glFriendView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        friendView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String uid = friendUIdList.get(i);
                startQuickPay(uid);
            }
        });
    }

    private void startQuickPay(String uid) {
        Intent intent = new Intent(this, QuickPayActivity.class);
        intent.putExtra("FRIEND_UID", uid);
        startActivity(intent);
    }

    public void makeUI(){

        Log.d(TAG, friendList.toString());
        MyCustomAdapter myCustomAdapter = new MyCustomAdapter(this, friendList);
        friendView.setAdapter(myCustomAdapter);
    }

    public void openAddFriends(View view) {
        startActivity(new Intent(this, AddFriendActivity.class));
    }

    public void openProfileActivity(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }

    public void openQuickPayActivity(View view) {
        startActivity(new Intent(this, QuickPayActivity.class));
        finish();
    }

    public void openMainActivity(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void openAccountActivity(View view) {
        startActivity(new Intent(this, AccountActivity.class));
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
    protected void onResume() {
        super.onResume();

        friendList = new ArrayList<>();
        friendUIdList = new ArrayList<>();

        mDatabase.child("friends").child(myUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "checking stored users " + dataSnapshot.toString());

                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Log.d(TAG, "checking sub data " + data.getValue().toString());

                    friendList.add(data.getKey());

                    Friends val = data.getValue(Friends.class);
                    friendUIdList.add(val.getUid());
                }

                makeUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
