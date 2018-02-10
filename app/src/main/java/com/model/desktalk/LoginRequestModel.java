package com.model.desktalk;

/**
 * Created by anshikas on 25-01-2017.
 * <p>
 * LoginRequestModel is having  the getter and setter method of login request.
 */

public class LoginRequestModel {


    String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    String password;

    public String getDevicetoken() {
        return devicetoken;
    }

    public void setDevicetoken(String devicetoken) {
        this.devicetoken = devicetoken;
    }

    String devicetoken;


}
