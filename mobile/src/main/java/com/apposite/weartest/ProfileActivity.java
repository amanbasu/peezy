package com.apposite.weartest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "log_cat";
    private int timesBackPressed = 2;

    private ImageView imageQR;
    private TextView name, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        imageQR = findViewById(R.id.ivQRPreview);
        name = findViewById(R.id.tvNamePreview);
        uid = findViewById(R.id.tvUidPreview);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER_INFO_PREF", 0);
        String myUId = sharedPref.getString("uid", "");
        String myName = sharedPref.getString("name", "");

        String msg = "Hi " + myName + "! Find your QR code containing the Unique ID below ☟.";

        name.setText(msg);
        uid.setText(myUId);

        Bitmap qrBitmap = null;

        try{
            qrBitmap = TextToImageEncode(myUId);
        } catch (WriterException e){
            Log.d(TAG, "Error generating QR.");
        }

        imageQR.setImageBitmap(qrBitmap);
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(Value, BarcodeFormat.QR_CODE, 350, 350, null);
        } catch (IllegalArgumentException Illegalargumentexception) {
            return null;
        }

        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? getColor(R.color.black): getColor(R.color.white);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 350, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
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
}
