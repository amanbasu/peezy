package com.apposite.weartest;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AIListener {

    private Button sendRequest, confirmTransaction, rejectTransaction;
    private TextView serviceResponse;

    private AIService mAiService;
    private TextToSpeech mTextToSpeech;

    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 0;
    private final String CLIENT_ACCESS_TOKEN = "e50e3f1fdf3d4b058056e90acc5c880e";
    private final String TAG = "log_tag";

    private final String PAYMENT_INTENT = "payment";
    private final String RECEIVE_INTENT = "receive";

    private int timesBackPressed = 2;

    private DatabaseReference mDatabase;

    private String dfIntent;

    public TransactionDetails transactionDetails;

    private ArrayList<String> friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendRequest = findViewById(R.id.btnSendRequest);
        serviceResponse = findViewById(R.id.tvResponse);
        confirmTransaction = findViewById(R.id.btnConfirm);
        rejectTransaction = findViewById(R.id.btnCancelTransaction);

        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        mAiService = AIService.getService(this, config);
        mAiService.setListener(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAiService.startListening();
            }
        });

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = mTextToSpeech.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        confirmTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceedTransaction();
            }
        });

        rejectTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cancel the transaction
                cancelTransaction();
            }
        });

        populateFriendList();

    }

    public void cancelTransaction(){
        rejectTransaction.setVisibility(View.GONE);
        confirmTransaction.setVisibility(View.GONE);
        sendRequest.setVisibility(View.VISIBLE);
        serviceResponse.setText(getString(R.string.greeting));
        transactionDetails = null;
    }

    public void openFriendActivity(View view){
        startActivity(new Intent(this, FriendsActivity.class));
        finish();
    }

    public void openQuickPayActivity(View view){
        startActivity(new Intent(this, QuickPayActivity.class));
        finish();
    }

    public void openProfileActivity(View view){
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }

    public void openAccountActivity(View view){
        startActivity(new Intent(this, AccountActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (MY_PERMISSIONS_REQUEST_RECORD_AUDIO == requestCode) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
        }

        // other 'case' lines to check for other
        // permissions this app might request.
    }

    @Override
    public void onResult(AIResponse response) {

        Result result = response.getResult();

        double amount = 0;
        String currency = "", person = "";

        // Get parameters
        StringBuilder parameterString = new StringBuilder();
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                try {
                    if (entry.getKey().equals("unit-currency")) {
                        JsonObject val = entry.getValue().getAsJsonObject();
                        amount = val.get("amount").getAsDouble();
                        currency = val.get("currency").getAsString();
                    } else if (entry.getKey().equals("person")) {
                        JsonObject val = entry.getValue().getAsJsonObject();
                        person = val.get("name").getAsString();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                parameterString.append("(").append(entry.getKey()).append(", ").append(entry.getValue()).append(") ");
            }
        }

        // Show results in TextView.
        String full_response = "Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString +
                "\nSource: " + result.getSource() +
                "\nMetadata" + result.getMetadata().toString() +
                "\nResponse: " + result.getFulfillment().getSpeech();

        String fulfillment = result.getFulfillment().getSpeech();
        String api_response = fulfillment;
        int idx = fulfillment.indexOf(':');
        Log.d(TAG, full_response);

        if (amount!=0 && !currency.isEmpty() && !person.isEmpty()){
            transactionDetails = new TransactionDetails(amount, currency, person);
        }

        if(idx>0) {
            // valid intent received
            dfIntent = fulfillment.substring(0, idx);
            api_response = fulfillment.substring(idx + 2);

            Log.d(TAG, "Intent: " + dfIntent);

            if (transactionDetails!=null){

//            Toast.makeText(this, "Press CONFIRM to proceed.", Toast.LENGTH_LONG).show();

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Press CONFIRM to proceed.", Snackbar.LENGTH_LONG);
                snackbar.show();

                sendRequest.setVisibility(View.GONE);
                confirmTransaction.setVisibility(View.VISIBLE);
                rejectTransaction.setVisibility(View.VISIBLE);
            }

        } else{
            // fallback intent
        }

        serviceResponse.setText(api_response);

        int speechStatus = mTextToSpeech.speak(api_response, TextToSpeech.QUEUE_FLUSH, null, null);

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }

    public void populateFriendList(){

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);
        String myUId = sharedPref.getString("uid", "");

        friendList = new ArrayList<>();

        mDatabase.child("users").child(myUId).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "checking stored users " + dataSnapshot.toString());

                Log.d(TAG, "Checking friends...");
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Log.d(TAG, data.getKey().toLowerCase() + " ");
                    friendList.add(data.getKey().toLowerCase());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private boolean isValidFriends(String friend) {
        for(String f: friendList){
            if(f.equalsIgnoreCase(friend))
                return true;
        }
        return false;
    }

    public void proceedTransaction(){

        String friend = transactionDetails.getPerson();
        if (!isValidFriends(friend.toLowerCase())){
            Toast.makeText(this, "You have no friend named \"" + friend + "\".", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dfIntent.equals(PAYMENT_INTENT)){
            // pay someone
            String response = "Payment successful.";
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();

        } else if (dfIntent.equals(RECEIVE_INTENT)){
            // receive from someone

            // TODO send receive notification
            String response = "Money request sent to " + transactionDetails.getPerson() + ".";
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();


        } else {
            Log.d(TAG, "Invalid Intent.");
        }

        cancelTransaction();
    }

    @Override
    public void onError(AIError error) {
        serviceResponse.setText(error.toString());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTextToSpeech != null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
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
