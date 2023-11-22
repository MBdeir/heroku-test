package com.example.wildsight;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AnimalsListActivity extends AppCompatActivity{
    LinearLayout itemContainer;
    RequestQueue requestQueue;
    private Dialog customProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animals_list);
        itemContainer = findViewById(R.id.itemContainer);
        requestQueue = Volley.newRequestQueue(this);
        fetchAnimals();

    }
    private String getSavedUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getString("username", null); // Returns null if no username is found
    }
    private void fetchAnimals() {
        customProgressDialog = new Dialog(this);
        customProgressDialog.setContentView(R.layout.custom_progress_dialog);
        customProgressDialog.setCancelable(false);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.show();
        String url = "https://wildsight.onrender.com/all_animals";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject animal = response.getJSONObject(i);
                        addAnimalToView(animal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                customProgressDialog.dismiss();

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonArrayRequest);
    }

    private void addAnimalToView(JSONObject animal) {
        try {
            View itemView = getLayoutInflater().inflate(R.layout.item_layout, null);
            final ImageView itemImage = itemView.findViewById(R.id.itemImage);
            TextView itemName = itemView.findViewById(R.id.itemName);
            TextView moreInfoButton = itemView.findViewById(R.id.moreInfoButton);
            TextView descriptionText = itemView.findViewById(R.id.descriptionText);
            ImageView heart = itemView.findViewById(R.id.favorite);
            itemName.setText(animal.getString("category"));
            descriptionText.setText(animal.getString("shortDescription"));

            String imageUrl = animal.getString("image");
            Picasso.get().load(imageUrl)
                    .into(itemImage);

            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (descriptionText.getVisibility() == View.VISIBLE) {
                        descriptionText.setVisibility(View.GONE);
                    } else {
                        descriptionText.setVisibility(View.VISIBLE);
                    }
                }
            });
            heart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    heart.setImageResource(R.drawable.white_filled_heart_icon);
                    String username= getSavedUsername();
                    String animal= itemName.getText().toString();
                    AddToFavorites(username,  animal);
                }
            });


            itemContainer.addView(itemView);
            Space space = new Space(this);
            space.setMinimumHeight(20);
            itemContainer.addView(space);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void GoToFavorites(View view){
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
    }
    public void AddToFavorites(String username, String animal){
        customProgressDialog = new Dialog(this);
        customProgressDialog.setContentView(R.layout.custom_progress_dialog);
        customProgressDialog.setCancelable(false);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.show();

        String url = "https://wildsight.onrender.com/add_favourite_animal";
        if (username == null) {
            // Handle the case where username is not found
            customProgressDialog.dismiss();
            return;
        }
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("username", username);
            jsonRequest.put("animal", animal);
        } catch (JSONException e) {
            e.printStackTrace();
            customProgressDialog.dismiss();
            return;
        }
        JsonArrayPostRequest jsonArrayPostRequest = new JsonArrayPostRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {

                    JSONObject animal = response.getJSONObject(0);
                    addAnimalToView(animal);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                customProgressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                customProgressDialog.dismiss();
            }
        });

        requestQueue.add(jsonArrayPostRequest);
    }


}
