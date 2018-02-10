package com.model.desktalk;

/**
 * Created by User on 2/3/2018.
 */

public class StudentLocationModel {

    String bus_id;
    String bus_no;
    String student_id;
    String fname;
    String lname;
    String gender;
    String device_track_id;
    String lat;
    String lng;
    String last_location_time;

    public String getBus_id() {
        return bus_id;
    }

    public void setBus_id(String bus_id) {
        this.bus_id = bus_id;
    }

    public String getBus_no() {
        return bus_no;
    }

    public void setBus_no(String bus_no) {
        this.bus_no = bus_no;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDevice_track_id() {
        return device_track_id;
    }

    public void setDevice_track_id(String device_track_id) {
        this.device_track_id = device_track_id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLast_location_time() {
        return last_location_time;
    }

    public void setLast_location_time(String last_location_time) {
        this.last_location_time = last_location_time;
    }
}
