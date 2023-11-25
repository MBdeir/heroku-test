package com.example.wildsight;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalsListActivity extends AppCompatActivity{
    LinearLayout itemContainer;
    RequestQueue requestQueue;
    private static JSONArray favorites = null;
    private Dialog customProgressDialog;
    private static JSONArray cachedAnimalsData = null; // Add this line
    private EditText searchEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animals_list);
        itemContainer = findViewById(R.id.itemContainer);
        requestQueue = Volley.newRequestQueue(this);
        searchEditText = findViewById(R.id.searchEditText);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterAnimals(charSequence.toString()); // Call filterAnimals method whenever text changes
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Log the text for debugging
                Log.d("SearchText", "Current search text: " + editable.toString());
            }
        });

        Intent intent = getIntent();
        if (intent!=null && intent.hasExtra("favorites")) {
            String favoritesJsonString = intent.getStringExtra("favorites");

            try {
                 favorites = new JSONArray(favoritesJsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        fetchAnimals();

    }
    private String getSavedUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getString("username", null); // Returns null if no username is found
    }

    private void filterAnimals(String query) {
        itemContainer.removeAllViews();

        for (int i = 0; i < cachedAnimalsData.length(); i++) {
            try {
                JSONObject animal = cachedAnimalsData.getJSONObject(i);
                String animalName = animal.getString("category");
                if (animalName.toLowerCase().contains(query.toLowerCase())) {
                    addAnimalToView(animal);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }




    private void fetchAnimals() {
        itemContainer.removeAllViews();

        customProgressDialog = new Dialog(this);
        customProgressDialog.setContentView(R.layout.custom_progress_dialog);
        customProgressDialog.setCancelable(false);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.show();
        String url = "https://wildsight.onrender.com/all_animals";
        if (cachedAnimalsData != null) {
            populateAnimals(cachedAnimalsData);
            return;
        }
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,

                new com.android.volley.Response.Listener<JSONArray>() {

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
                cachedAnimalsData = response;

            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonArrayRequest);
    }
    private void populateAnimals(JSONArray response) {
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

    private static boolean containsObject(JSONArray jsonArray, JSONObject jsonObject) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                // Compare JSONObjects
                if (jsonObjectSimilar(jsonArray.getJSONObject(i), jsonObject)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    private static boolean jsonObjectSimilar(JSONObject obj1, JSONObject obj2) {
        return obj1.toString().equals(obj2.toString());
    }
    private void addAnimalToView(JSONObject animal) {
        try {
            View itemView = getLayoutInflater().inflate(R.layout.item_layout, null);
            final ImageView itemImage = itemView.findViewById(R.id.itemImage);
            TextView itemName = itemView.findViewById(R.id.itemName);
            TextView moreInfoButton = itemView.findViewById(R.id.moreInfoButton);
            TextView descriptionText = itemView.findViewById(R.id.descriptionText);
            ImageView heart = itemView.findViewById(R.id.favorite);
            if(favorites !=null && containsObject(favorites,animal)){
                heart.setImageResource(R.drawable.white_filled_heart_icon);
            }
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
                    AddToFavorites(username, animal);

                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AnimalsListActivity.this, ResultActivity.class);
                    try {
                        intent.putExtra("name", animal.getString("category")); // Use "category" instead of "name"
                        intent.putExtra("shortDescription", animal.getString("shortDescription"));
                        intent.putExtra("habitat", animal.getString("habitat"));
                        intent.putExtra("diet", animal.getString("diet"));
                        intent.putExtra("location", animal.getString("location"));
                        intent.putExtra("type", animal.getString("type"));
                        intent.putExtra("lifeSpan", animal.getString("lifeSpan"));
                        intent.putExtra("weight", animal.getString("weight"));
                        intent.putExtra("topSpeed", animal.getString("top_speed")); // Note the underscore in "top_speed"
                        intent.putExtra("imagePath", animal.getString("image"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
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
            ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
            FavoriteRequest request = new FavoriteRequest();
            request.setUsername(username);
            request.setAnimal(animal);

         Call<FavoriteResponse> call = service.addToFavorites(request);
         call.enqueue(new Callback<FavoriteResponse>() {
             @Override
             public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                 if (response.isSuccessful()) {
                     FavoriteResponse favoriteResponse = response.body();
                     Toast.makeText(AnimalsListActivity.this, "Success: " + favoriteResponse.getMessage(), Toast.LENGTH_SHORT).show();
                 } else {
                     Log.e(TAG, "AddToFavorites: onResponse - Response not successful. Code: " + response.code());
                     try {
                         // Try to get the error body if it exists
                         String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                         Log.e(TAG, "AddToFavorites: Error Body: " + errorBody);
                     } catch (IOException e) {
                         Log.e(TAG, "AddToFavorites: Error while reading error body", e);
                     }
                 }
             }

             @Override
             public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                 Log.e(TAG, "AddToFavorites: onFailure", t);
             }
         });
     }

}
