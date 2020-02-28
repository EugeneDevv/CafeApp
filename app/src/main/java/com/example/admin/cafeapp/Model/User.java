package com.example.admin.cafeapp.Model;

public class User {

    private String name;
    private String password;
    private String Phone;
    private String IsStaff;
    private String homeAddress;
    private Object balance;
    private String DOB,emailAddress,gender;

    public User() {
    }

    public User(String Pname, String Ppassword) {


        name = Pname;
        password = Ppassword;
        IsStaff="false";
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDOB() {
        return DOB;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getGender() {
        return gender;
    }

    public void setBalance(Object balance) {
        this.balance = balance;
    }

    public Object getBalance() {
        return balance;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPhone() {
        return Phone;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getIsStaff() {
        return IsStaff;
    }
}
