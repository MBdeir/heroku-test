package com.example.wildsight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
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
