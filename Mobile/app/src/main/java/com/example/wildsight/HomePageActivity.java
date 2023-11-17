package com.example.wildsight;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class HomePageActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private ImageView btnCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        btnCam = findViewById(R.id.cameraBtn);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            setClickListener();
        }
    }

    private void setClickListener() {
        btnCam.setOnClickListener(view -> dispatchTakePictureIntent());
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    // Convert bitmap to bytes
                    byte[] imageBytes = convertBitmapToBytes(imageBitmap);

                    // Send bytes to the API using Volley
                    sendImageToApi(imageBytes);
                }
            }
        }
    }

    private byte[] convertBitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private void sendImageToApi(byte[] imageBytes) {
        String apiUrl = "https://wildsight.onrender.com/uploadImg";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    // Handle successful response
                    // You can read the response if needed
                    if (response.equals("success")) { // Replace "success" with the expected response
                        startNewActivity();
                    } else {
                        Toast.makeText(HomePageActivity.this,
                                "API response indicates failure", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle error
                    error.printStackTrace();
                    Toast.makeText(HomePageActivity.this,
                            "Failed to send image to the API", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                return imageBytes;  // This is where the image bytes are sent in the request body
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add your headers here if needed
                // headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN");
                return headers;
            }
        };

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void startNewActivity() {
        // Start your new activity here
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
    }

    public void DiscoverAnimals(View view) {
        Intent intent = new Intent(this, AnimalsListActivity.class);
        startActivity(intent);
    }

    public void FunFacts(View view) {
        Intent intent = new Intent(this, FunFactsActivity.class);
        startActivity(intent);
    }

    // Handle the result of the permission request
    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setClickListener();
            } else {
                Toast.makeText(this, "Camera permission denied. Please enable it in app settings.",
                        Toast.LENGTH_SHORT).show();
                redirectToAppSettings();
            }
        }
    }

    private void redirectToAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

}
