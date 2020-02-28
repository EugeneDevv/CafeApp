package com.example.admin.cafeapp.Model;

public class Rating {
    private String foodId;
    private String rateValue;
    private String  comment;
    private String  name;

    public Rating() {

    }

    public Rating(String foodId, String rateValue, String comment, String name) {
        this.foodId = foodId;
        this.rateValue = rateValue;
        this.comment = comment;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFoodId() {
        return foodId;
    }

    public String getRateValue() {
        return rateValue;
    }

    public String getComment() {
        return comment;
    }
}
