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
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.JsonElement;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements AIListener, SensorEventListener {

    private Button sendRequest, addFriend, showProfile;
    private TextView serviceResponse;

    private AIService mAiService;
    private TextToSpeech mTextToSpeech;

    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 0;
    private final String CLIENT_ACCESS_TOKEN = "e50e3f1fdf3d4b058056e90acc5c880e";
    private final String TAG = "log_tag";

    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;

    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    private OnDataPointListener mListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendRequest = findViewById(R.id.btnSendRequest);
        addFriend = findViewById(R.id.btnAddPerson);
        serviceResponse = findViewById(R.id.tvResponse);
        showProfile = findViewById(R.id.btnShowProfile);

        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        mAiService = AIService.getService(this, config);
        mAiService.setListener(this);

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

        // normal Heart rate data
//        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
//        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        // raw sensor data
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            Fitness.getSensorsClient(this, account)
                    .findDataSources(
                            new DataSourcesRequest.Builder()
                                    .setDataTypes(DataType.TYPE_HEART_RATE_BPM)
                                    .setDataSourceTypes(DataSource.TYPE_RAW)
                                    .build())
                    .addOnSuccessListener(
                            new OnSuccessListener<List<DataSource>>() {
                                @Override
                                public void onSuccess(List<DataSource> dataSources) {
                                    Log.i(TAG, "listing data sources...");
                                    for (DataSource dataSource : dataSources) {
                                        Log.i(TAG, "Data source found: " + dataSource.toString());
                                        Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

                                        // Let's register a listener to receive Activity data!
                                        if (dataSource.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE)
                                                && mListener == null) {
                                            Log.i(TAG, "Data source for LOCATION_SAMPLE found! Registering.");
//                                            registerFitnessDataListener(dataSource, DataType.TYPE_LOCATION_SAMPLE);
                                        }
                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "failed", e);
                                }
                            });
        }
//
//        mListener = new OnDataPointListener() {
//                    @Override
//                    public void onDataPoint(DataPoint dataPoint) {
//                        for (Field field : dataPoint.getDataType().getFields()) {
//                            Value val = dataPoint.getValue(field);
//                            Log.i(TAG, "Detected DataPoint field: " + field.getName());
//                            Log.i(TAG, "Detected DataPoint value: " + val);
//                        }
//                    }
//                };

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            accessGoogleFit();
        }

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), InfoActivity.class));
            }
        });

        showProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });
    }

//    private void registerFitnessDataListener(DataSource dataSource, DataType typeLocationSample) {
//        serviceResponse.setText(typeLocationSample.getName() + ": " + dataSource.toString());
//    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit();
            }
        }
    }

    private void accessGoogleFit() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR, -1);
        long startTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .read(DataType.TYPE_HEART_RATE_BPM)
//                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.TYPE_HEART_RATE_BPM)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        String res = "";
                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                for (DataPoint dp : dataSet.getDataPoints()) {
                                    for (Field field : dp.getDataType().getFields()) {
                                        res += "[Field: "+field.getName()+", Value: "+dp.getValue(field)+"]";
                                    }
                                    res += '\n';
                                }
                            }
                        }
//                        serviceResponse.setText(res);
                        Log.d(TAG, "fitness data loaded. Res: " + res);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {
                        Log.d(TAG, "onComplete()");
                    }
                });
    }

    @Override
    public void onResult(AIResponse response) {

        Result result = response.getResult();

        // Get parameters
        StringBuilder parameterString = new StringBuilder();
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString.append("(").append(entry.getKey()).append(", ").append(entry.getValue()).append(") ");
            }
        }

        // Show results in TextView.
        String full_response = "Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString +
                "\nResponse: " + result.getFulfillment().getSpeech();
        String api_response = result.getFulfillment().getSpeech();
        Log.d(TAG, full_response);

        // TODO
        // add verification logic

        serviceResponse.setText(api_response);

        int speechStatus = mTextToSpeech.speak(api_response, TextToSpeech.QUEUE_FLUSH, null, null);

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
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
    public void onSensorChanged(SensorEvent sensorEvent) {
//        serviceResponse.setText(String.valueOf(sensorEvent.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
//        mSensorManager.registerListener(this, this.mHeartRateSensor, 3);

        // raw sensor data
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//
//        if (account != null) {
//            Fitness.getSensorsClient(this, account)
//                    .add(
//                            new SensorRequest.Builder()
////                                .setDataSource(DataSource.TYPE_RAW) // Optional but recommended for custom data sets.
//                                    .setDataType(DataType.AGGREGATE_WEIGHT_SUMMARY) // Can't be omitted.
//                                    .setSamplingRate(10, TimeUnit.SECONDS)
//                                    .build(),
//                            mListener)
//                    .addOnCompleteListener(
//                            new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Log.i(TAG, "Listener registered!");
//                                    } else {
//                                        Log.e(TAG, "Listener not registered.", task.getException());
//                                    }
//                                }
//                            });
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mSensorManager.unregisterListener(this);
//
//        // raw sensor data
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//
//        if (account != null) {
//            Fitness.getSensorsClient(this, account)
//                    .remove(mListener)
//                    .addOnCompleteListener(
//                            new OnCompleteListener<Boolean>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Boolean> task) {
//                                    if (task.isSuccessful() && task.getResult()) {
//                                        Log.i(TAG, "Listener was removed!");
//                                    } else {
//                                        Log.i(TAG, "Listener was not removed.");
//                                    }
//                                }
//                            });
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTextToSpeech != null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }

}
