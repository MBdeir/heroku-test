package com.example.wildsight;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);

        // Retrieve details from Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("name", "");
            String shortDescription = extras.getString("shortDescription", "");
            String habitat = extras.getString("habitat", "");
            String diet = extras.getString("diet", "");
            String location = extras.getString("location", "");
            String type = extras.getString("type", "");
            String lifeSpan = extras.getString("lifeSpan", "");
            String weight = extras.getString("weight", "");
            String topSpeed = extras.getString("topSpeed", "");
            String imagePath = extras.getString("imagePath", "");

            // Set text views
            TextView result_name = findViewById(R.id.name);
            result_name.setText(name + "!!");
            TextView result_description = findViewById(R.id.description);
            result_description.setText(shortDescription);
            TextView result_habitat = findViewById(R.id.habitat);
            result_habitat.setText(habitat);
            TextView result_diet = findViewById(R.id.diet);
            result_diet.setText(diet);
            TextView result_location = findViewById(R.id.location);
            result_location.setText(location);
            TextView result_type = findViewById(R.id.type);
            result_type.setText(type);
            TextView result_lifeSpan = findViewById(R.id.lifespan);
            result_lifeSpan.setText(lifeSpan);
            TextView result_weight = findViewById(R.id.weight);
            result_weight.setText(weight);
            TextView result_topSpeed = findViewById(R.id.speed);
            result_topSpeed.setText(topSpeed);

            // Set ShapeableImageView
            ShapeableImageView imageView = findViewById(R.id.Image);
            // Load and display the image using the imagePath
            // Example: You may use a library like Picasso or Glide to efficiently load images.
            // For simplicity, here is a basic approach using the file path:
            if (!imagePath.isEmpty()) {
                imageView.setImageURI(Uri.parse(imagePath));
            }
        }
    }
}
