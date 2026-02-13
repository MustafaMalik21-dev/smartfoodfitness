package com.mustafa.smartfoodfitness.dto;

import java.util.List;

public class FoodSearchResponse {
    private List<FoodSearchResultItem> items;

    public List<FoodSearchResultItem> getItems() { return items; }
    public void setItems(List<FoodSearchResultItem> items) { this.items = items; }
}
