package com.example.wildsight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View mainLayout = findViewById(R.id.mainL);

        mainLayout.setOnClickListener(v -> {
            clickCount++;
            if (clickCount == 5) {
                saveUsername("countbat");
                goToHomePage();
            }
        });
    }

    public void MoveToSignUpView(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void MoveToLogInView(View view) {
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }

    private void goToHomePage() {
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

}
