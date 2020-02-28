package com.example.admin.cafeapp.Model;

public class ShippingInformation {
    private String orderId,shipperPhone,name;
    private Double lat,lng;

    public ShippingInformation() {
    }

    public ShippingInformation(String orderId,String shipperPhone,Double lat,Double lng,String name) {
        this.orderId=orderId;
        this.shipperPhone=shipperPhone;
        this.lat=lat;
        this.lng=lng;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setShipperPhone(String shipperPhone) {
        this.shipperPhone = shipperPhone;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getShipperPhone() {
        return shipperPhone;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
