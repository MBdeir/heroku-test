package com.example.wildsight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class AnimalsListActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animals_list);

        // Get the container
        LinearLayout itemContainer = findViewById(R.id.itemContainer);

        // Add items dynamically (you can replace this with your data)
        for (int i = 1; i <= 5; i++) {
            View itemView = getLayoutInflater().inflate(R.layout.item_layout, null);
            ImageView itemImage = itemView.findViewById(R.id.itemImage);
            TextView itemName = itemView.findViewById(R.id.itemName);
            TextView moreInfoButton = itemView.findViewById(R.id.moreInfoButton);
            TextView descriptionText = itemView.findViewById(R.id.descriptionText);

            // Set item data (replace with your data)
            itemImage.setImageResource(R.drawable.download1);
            itemName.setText("Cat " + i);
            descriptionText.setText("Cats are graceful, carnivorous (meat-eating) mammals with sharp teeth and claws. Most kinds of cat prey on other mammals or birds, and most hunt alone at night. Only lions live and hunt in groups. The claws of cats are extended to help grip their prey, but retracted (pulled back) when not in use ");

            // Set button click listener
            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toggle visibility of the description
                    if (descriptionText.getVisibility() == View.VISIBLE) {
                        descriptionText.setVisibility(View.GONE);
                    } else {
                        descriptionText.setVisibility(View.VISIBLE);
                    }
                }
            });
            itemContainer.addView(itemView);
            Space space = new Space(this);
            space.setMinimumHeight(20);
            space.setMinimumWidth(20);
            itemContainer.addView(space);


        }
    }
    public void GoToFavorites(View view){
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
    }
}
