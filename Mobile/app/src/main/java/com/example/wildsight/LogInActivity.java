package com.example.wildsight;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {
    private Dialog customProgressDialog;

    private static final String TAG = SignUpActivity.class.getSimpleName();
    EditText usernameEdit;
    EditText passwordEdit;

    Button postDataBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        // Get the TextView from your layout
        TextView textView = findViewById(R.id.signup);

        // Create a SpannableString with the text you want to underline
        String textToUnderline = "signup";
        SpannableString content = new SpannableString(textToUnderline);

        // Apply the UnderlineSpan to the specific portion of the text
        content.setSpan(new UnderlineSpan(), 0, textToUnderline.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the SpannableString to the TextView
        textView.setText(content);


        usernameEdit = findViewById(R.id.uasername_input_login);
        passwordEdit = findViewById(R.id.password_input_login);
        postDataBtn =findViewById(R.id.button);

        postDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validating if the text field is empty or not.
                if (usernameEdit.getText().toString().isEmpty()) {
                    Toast.makeText(LogInActivity.this, "username is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (passwordEdit.getText().toString().isEmpty()) {
                    Toast.makeText(LogInActivity.this, "password is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                postDataUsingVolley(usernameEdit.getText().toString(), passwordEdit.getText().toString());
            }
        });


    }
    public void BackToMainScreen(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void BackToSignUpScreen(View view){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
    public void OpenHomePage(View view){
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

    public void GoToHomeScreen(){
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

    private void saveUsername(String username) {
    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
    SharedPreferences.Editor myEdit = sharedPreferences.edit();

    // Storing the key and its value as the data fetched from edittext
    myEdit.putString("username", username);
    myEdit.apply();
    }


    private void postDataUsingVolley(String username, String password) {
        customProgressDialog = new Dialog(this);
        customProgressDialog.setContentView(R.layout.custom_progress_dialog);
        customProgressDialog.setCancelable(false);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.show();

        String url = "https://wildsight.onrender.com/login";

        // Replace the JSON object with your actual request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            // Add other parameters as needed
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Log the response for debugging
                        Log.d(TAG, "Response: " + response.toString());

                        String message = response.optString("message");


                        if ("Account authorized".equals(message)) {
                            saveUsername(username);
                            GoToHomeScreen();
                        } else {
                            Toast.makeText(LogInActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                        customProgressDialog.dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors here
                        Log.e(TAG, "Error: " + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Add headers if needed
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);


    }
}
