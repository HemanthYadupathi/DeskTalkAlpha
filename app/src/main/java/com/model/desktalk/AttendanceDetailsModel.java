package com.model.desktalk;

/**
 * Created by User on 2/3/2018.
 */

public class AttendanceDetailsModel {

    String attendance_take_id;
    String attendance_id;
    String user_id;
    String status;
    String fname;
    String lname;
    String reference_id;

    public String getAttendance_take_id() {
        return attendance_take_id;
    }

    public void setAttendance_take_id(String attendance_take_id) {
        this.attendance_take_id = attendance_take_id;
    }

    public String getAttendance_id() {
        return attendance_id;
    }

    public void setAttendance_id(String attendance_id) {
        this.attendance_id = attendance_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getReference_id() {
        return reference_id;
    }

    public void setReference_id(String reference_id) {
        this.reference_id = reference_id;
    }
}
