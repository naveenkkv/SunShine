package com.example.naveenkumark.sunshine.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by naveenkumark on 10/27/16.
 */

public class City {
    int id;
    @SerializedName("id")
    String city_name;
    String country;
    String population;
    Coordinates coord;

    //if we don't want null objects we can initialize them in the constructor
    City(){
        coord = new Coordinates();
    }
}
