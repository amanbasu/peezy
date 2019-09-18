package com.apposite.weartest.ui.login;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.apposite.weartest.AccountActivity;
import com.apposite.weartest.MainActivity;
import com.apposite.weartest.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Random;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private SignInButton googleSignIn;
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 101;
    private final String TAG = "log_cat";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleSignIn = findViewById(R.id.btnGoogleSignIn);
        googleSignIn.setSize(SignInButton.SIZE_STANDARD);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            Log.w(TAG, "SignIn failed - request code = " + requestCode);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    public void updateUI(GoogleSignInAccount account){
        // Signed in successfully, show authenticated UI.
        if (account != null){
            Log.i(TAG, "Google Sign In successful");

            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Authenticating your ECG...");
            pd.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    final int random = new Random().nextInt(2);
                    pd.dismiss();

                    if(random>0) doBiometricAuth();
                    else goIn();
                }
            }, 3000);
        } else {
            Toast.makeText(getApplicationContext(), "Sign in failed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void goIn(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);

        String uid = sharedPref.getString("uid", "");
        Log.d(TAG, "SP data: " + uid);
        if (!uid.equals("")){
            Log.d(TAG, "data found in Shared preference");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else {
            Log.d(TAG, "data not found in shared preference");
            startActivity(new Intent(getApplicationContext(), AccountActivity.class));
        }

        finish();
    }

    private void doBiometricAuth() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Authentication");
        alertDialog.setMessage("ECG not found.\nPlease verify your identity.");
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication")
                .setDescription("ECG not found.\nPlease verify your identity.")
                .setNegativeButtonText("Cancel")
                .build();

        final BiometricPrompt myBiometricPrompt = new BiometricPrompt(this,
                Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    finish();
                } else {
                    Log.d(TAG, "An unrecoverable error occurred");
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Fingerprint recognised successfully");
                alertDialog.dismiss();
                goIn();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "Fingerprint not recognised");
            }

        });

        myBiometricPrompt.authenticate(promptInfo);
    }
}
