package com.example.admin.cafeapp.Model;

public class Feedback {
    private String name;
    private String homeAddress;
    private String DOB,emailAddress,gender;
    private String rateValue;
    private String  comment;

    public Feedback() {
    }

    public Feedback(String name, String homeAddress, String DOB, String emailAddress, String gender, String rateValue, String comment) {
        this.name = name;
        this.homeAddress = homeAddress;
        this.DOB = DOB;
        this.emailAddress = emailAddress;
        this.gender = gender;
        this.rateValue = rateValue;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRateValue() {
        return rateValue;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
