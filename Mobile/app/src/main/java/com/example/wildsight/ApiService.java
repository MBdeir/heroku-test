package com.example.wildsight;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("add_favourite_animal")
    Call<FavoriteResponse> addToFavorites(@Body FavoriteRequest request);
}
