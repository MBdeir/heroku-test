package com.example.wildsight;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class HomePageActivity extends AppCompatActivity {
    ImageView btnCam,plusIcon;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE = 2;
    private static final int PERMISSION_REQUEST_STORAGE = 1000;


    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            }
        }
    }

    // Handle the user's response to the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(this, "Storage permission is required to access photos.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        requestStoragePermission();
        btnCam = findViewById(R.id.cameraBtn);
        plusIcon = findViewById(R.id.plusIcon);

        btnCam.setOnClickListener(view -> {
            try{
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        });
        plusIcon.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // here we are converting the bitmap to a file form since the api accepts file
            File imageFile = convertBitmapToFile("capturedImage", imageBitmap);
            uploadImageToServer(imageFile);
        }
        else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                String imagePath = getPathFromUri(selectedImageUri);
                if (imagePath != null) {
                    File imageFile = new File(imagePath);
                    uploadImageToServer(imageFile);
                }
            }
        }
    }
    private String getPathFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }
    private File convertBitmapToFile(String fileName, Bitmap bitmap) {
        // Create a file in the application's directory
        File file = new File(getExternalFilesDir(null), fileName + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void uploadImageToServer(File imageFile) {
        String url = "https://wildsight.onrender.com/uploadImg";

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, imageFile,
                response -> {
                    try {
                        String responseStr = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        JSONObject jsonResponse = new JSONObject(responseStr);

                        if (jsonResponse.has("prediction")) {
                            JSONObject prediction = jsonResponse.getJSONObject("prediction");
                            String animalClass = prediction.getString("class");
                            double confidence = prediction.getDouble("confidence");

                            showToast("Detected: " + animalClass + " with confidence: " + confidence);
                        } else if (jsonResponse.has("error")) {
                            String errorMessage = jsonResponse.getString("error");
                            showToast("Error: " + errorMessage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    showToast("Network error: " + error.getMessage());
                });

        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(HomePageActivity.this, message, Toast.LENGTH_LONG).show());
    }

    // Example method to update UI
    private void showResult(String message) {
        // Update your UI with the message
        // This could be showing a dialog, updating a TextView, etc.
    }


    public void DiscoverAnimals(View view){
        Intent intent = new Intent(this, AnimalsListActivity.class);
        startActivity(intent);
    }

    public void FunFacts(View view){
        Intent intent = new Intent(this, FunFactsActivity.class);
        startActivity(intent);
    }
}
