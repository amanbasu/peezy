package com.apposite.weartest;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.util.Log;
import android.view.View;

public class QrCodeScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scanner);

        // Programmatically initialize the scanner view
        mScannerView = new ZXingScannerView(this);
        // Set the scanner view as the content view
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {

        Intent intent = new Intent();
        intent.putExtra("SCAN_RESULT", result.getText());
        setResult(RESULT_OK, intent);
        finish();
    }
}
