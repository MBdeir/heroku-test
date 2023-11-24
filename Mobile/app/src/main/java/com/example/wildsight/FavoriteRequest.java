package com.example.wildsight;

public class FavoriteRequest {
    private String username;
    private String animal;

    public void setUsername (String username){
        this.username = username;
    }

    public String getUsername()
    {
        return this.username;
    }

    public void setAnimal(String animal){
        this.animal = animal;
    }

    public String getAnimal()
    {
        return this.animal;
    }
}
