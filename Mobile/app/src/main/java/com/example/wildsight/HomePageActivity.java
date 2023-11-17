package com.example.wildsight;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {
    ImageView btnCam;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        btnCam = findViewById(R.id.cameraBtn);
        btnCam.setOnClickListener(view -> {
            try{
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(intent);
            }catch(Exception e){
                e.printStackTrace();
            }
        });


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
