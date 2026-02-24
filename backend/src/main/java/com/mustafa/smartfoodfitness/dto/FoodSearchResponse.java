package com.mustafa.smartfoodfitness.dto;
// Define the FoodSearchResponse DTO with a field for a list of FoodSearchResultItem objects, along with getter and setter methods for the items field to facilitate data transfer of food search results between the backend and frontend of the application
import java.util.List;

public class FoodSearchResponse {
    private List<FoodSearchResultItem> items;

    public List<FoodSearchResultItem> getItems() { 
        return items; 
    }
    public void setItems(List<FoodSearchResultItem> items) { 
        this.items = items; 
    }
}
