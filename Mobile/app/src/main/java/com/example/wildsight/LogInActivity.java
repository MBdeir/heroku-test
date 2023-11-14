package com.example.wildsight;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LogInActivity extends AppCompatActivity {
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
    }
    public void BackToMainScreen(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void BackToSignUpScreen(View view){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}
