package com.example.admin.cafeapp.Model;

public class Favourites {
    private String FoodId, FoodName,FoodImage,FoodDescription,FoodPrice,FoodDiscount,FoodMenuId,UserPhone;

    public Favourites() {
    }

    public Favourites(String foodId,String foodName,String foodPrice,String foodMenuId,String foodImage,String foodDiscount,String foodDescription,String userPhone) {

        FoodId=foodId;
        FoodName=foodName;
        FoodPrice=foodPrice;
        FoodMenuId=foodMenuId;
        FoodImage=foodImage;
        foodDiscount=foodDiscount;
        FoodDescription=foodDescription;
        UserPhone=userPhone;
    }

    public void setFoodId(String foodId) {
        FoodId = foodId;
    }

    public void setFoodName(String foodName) {
        FoodName = foodName;
    }

    public void setFoodPrice(String foodPrice) {
        FoodPrice = foodPrice;
    }

    public void setFoodMenuId(String foodMenuId) {
        FoodMenuId = foodMenuId;
    }

    public void setFoodImage(String foodImage) {
        FoodImage = foodImage;
    }

    public void setFoodDiscount(String foodDiscount) {
        FoodDiscount = foodDiscount;
    }

    public void setFoodDescription(String foodDescription) {
        FoodDescription = foodDescription;
    }

    public void setUserPhone(String userPhone) {
        UserPhone = userPhone;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public String getFoodId() {
        return FoodId;
    }

    public String getFoodDescription() {
        return FoodDescription;
    }

    public String getFoodDiscount() {
        return FoodDiscount;
    }

    public String getFoodImage() {
        return FoodImage;
    }

    public String getFoodMenuId() {
        return FoodMenuId;
    }

    public String getFoodName() {
        return FoodName;
    }

    public String getFoodPrice() {
        return FoodPrice;
    }
}
