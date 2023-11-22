package com.example.wildsight;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class HomePageActivity extends AppCompatActivity {
    ImageView btnCam, plusIcon;
    Button signOut;
    private Dialog customProgressDialog;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA_AND_STORAGE_PERMISSION = 1001;

    private void requestCameraAndStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_AND_STORAGE_PERMISSION);
            }
        }
    }

    // Handle the user's response to the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted
            } else {
                // Permissions denied
                Toast.makeText(this, "Permissions are required to access files and camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void signOut(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_page);
        // Request both storage and camera permissions
        requestCameraAndStoragePermissions();
        btnCam = findViewById(R.id.cameraBtn);
        plusIcon = findViewById(R.id.plusIcon);
        signOut = findViewById(R.id.signOutButton);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut(view);
            }
        });

        btnCam.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        plusIcon.setOnClickListener(view -> {
            // Calling intent on below line.
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Starting activity on below line.
            startActivityForResult(intent, 2);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            File imageFile = convertBitmapToFile("capturedImage", imageBitmap);
            String imagePath = uploadImageToServer(imageFile);
            if (imagePath != null) {
                openResultActivity(imagePath);
            }
        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                String imagePath = getPathFromUri(selectedImageUri);
                if (imagePath != null) {
                    File imageFile = new File(imagePath);
                    String uploadedImagePath = uploadImageToServer(imageFile);
                    if (uploadedImagePath != null) {
                        openResultActivity(uploadedImagePath);
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
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

    private String uploadImageToServer(File imageFile) {

        customProgressDialog = new Dialog(this);
        customProgressDialog.setContentView(R.layout.custom_progress_dialog);
        customProgressDialog.setCancelable(false);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.show();
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
                            customProgressDialog.dismiss();
                            fetchAdditionalInfo(animalClass, imageFile.getAbsolutePath());
                        } else if (jsonResponse.has("error")) {
                            customProgressDialog.dismiss();
                            String errorMessage = jsonResponse.getString("error");
                            showToast("Error: " + errorMessage);
                        }
                    } catch (Exception e) {
                        customProgressDialog.dismiss();
                        e.printStackTrace();
                        showToast("Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    customProgressDialog.dismiss();
                    showToast("Network error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                }) {

            @Override
            public RetryPolicy getRetryPolicy() {
                return new DefaultRetryPolicy(
                        10000, // Timeout in milliseconds (e.g., 5000ms or 5 seconds)
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // Number of retries (e.g., 1 or 2)
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT); // Backoff multiplier
            }
        };

        Volley.newRequestQueue(this).add(volleyMultipartRequest);

        return null;
    }

    private void fetchAdditionalInfo(String animalName, String imagePath) {
        customProgressDialog = new Dialog(this);
        customProgressDialog.setContentView(R.layout.custom_progress_dialog);
        customProgressDialog.setCancelable(false);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.show();
        String secondApiUrl = "https://wildsight.onrender.com/animal_info?name=" + animalName;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, secondApiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResult = new JSONObject(response);
                            JSONObject animalDetails = jsonResult.getJSONObject(animalName);

                            String shortDescription = animalDetails.getString("shortDescription");
                            String habitat = animalDetails.getString("habitat");
                            String diet = animalDetails.getString("diet");
                            String location = animalDetails.getString("location");
                            String type = animalDetails.getString("type");
                            String lifeSpan = animalDetails.getString("lifeSpan");
                            String weight = animalDetails.getString("weight");
                            String topSpeed = animalDetails.getString("top_speed");

                            Intent intent = new Intent(HomePageActivity.this, ResultActivity.class);
                            intent.putExtra("name", animalName);
                            intent.putExtra("imagePath", imagePath);
                            intent.putExtra("shortDescription", shortDescription);
                            intent.putExtra("habitat", habitat);
                            intent.putExtra("diet", diet);
                            intent.putExtra("location", location);
                            intent.putExtra("type", type);
                            intent.putExtra("lifeSpan", lifeSpan);
                            intent.putExtra("weight", weight);
                            intent.putExtra("topSpeed", topSpeed);
                            customProgressDialog.dismiss();
                            startActivity(intent);
                        } catch (JSONException e) {
                            customProgressDialog.dismiss();
                            e.printStackTrace();
                            showToast("Error parsing additional info: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        customProgressDialog.dismiss();
                        showToast("Error fetching additional info: " + error.getMessage());
                    }
                });

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void openResultActivity(String imagePath) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("imagePath", imagePath);
        startActivity(intent);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(HomePageActivity.this, message, Toast.LENGTH_LONG).show());
    }

    public void DiscoverAnimals(View view) {
        Intent intent = new Intent(this, AnimalsListActivity.class);
        startActivity(intent);
    }

    public void FunFacts(View view) {
        Intent intent = new Intent(this, FunFactsActivity.class);
        startActivity(intent);
    }
}
