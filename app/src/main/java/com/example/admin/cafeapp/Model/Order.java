package com.example.admin.cafeapp.Model;

public class Order {
    private String UserPhone;
    private String ProductId;
    private String ProductName;
    private String Quantity;
    private String Price;
    private String Discount;
    private String Image;

    public Order() {

    }

    public Order(String userPhone,String productId,String productName,String quantity,String price,String discount,String image) {
        UserPhone=userPhone;
        ProductId=productId;
        ProductName=productName;
        Quantity=quantity;
        Price=price;
        Discount=discount;
        Image=image;
    }

    public void setUserPhone(String userPhone) {
        UserPhone = userPhone;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getImage() {
        return Image;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getProductId() {
        return ProductId;
    }

    public String getProductName() {
        return ProductName;
    }

    public String getQuantity() {
        return Quantity;
    }

    public String getPrice() {
        return Price;
    }

    public String getDiscount() {
        return Discount;
    }

}
