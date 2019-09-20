package com.apposite.weartest;

import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Random;

import androidx.annotation.NonNull;

public class MainActivity extends WearableActivity {

    TextView message;
    Button request, confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        message = findViewById(R.id.tvResponse);
        request = findViewById(R.id.btnRequest);
        confirm = findViewById(R.id.btnConfirm);

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listen();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        confirm.setVisibility(View.GONE);
                        request.setVisibility(View.VISIBLE);
                        message.setText("Transaction Successful!");
                    }
                }, 500);
            }
        });
    }

    private void listen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                message.setText("Requesting 20 pounds from Thomas.");
                request.setVisibility(View.GONE);
                confirm.setVisibility(View.VISIBLE);
            }
        }, 6000);
    }
}
