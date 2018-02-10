package com.utils.desktalk;

import com.google.gson.JsonElement;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * Created by Pavan.Chunchula on 28-02-2017.
 */

public interface Apis {

    @POST("api/login")
    @FormUrlEncoded
    Call<JsonElement> Authenticate(
            @FieldMap Map<String, String> form);


    @GET("api/get_userdatabytoken")
    Call<JsonElement> getUserProfile(@Header("token") String token);

    @POST("api/logout")
    Call<JsonElement> Logout(
            @Header("token") String token);

    @GET("api/get_classesbyuid/{id}")
    Call<JsonElement> getClassDetails(
            @Header("token") String token, @Path("id") String UID);

    @GET("api/get_attendance/{class_id}/{date}")
    Call<JsonElement> getAttendanceDetailsbyDate(
            @Header("token") String token, @Path("class_id") String Class_ID, @Path("date") String Date);

    @GET("api/get_attendance_historybydate/{class_id}/{class_id}")
    Call<JsonElement> getHistoryDates(
            @Header("token") String token, @Path("class_id") String Class_ID);

    @POST("api/get_student_location/4")
    Call<JsonElement> getStudentLocation(
            @Header("token") String token);

    @POST("api/mark_attendance")
    @FormUrlEncoded
    Call<JsonElement> markAttedance(
            @Header("token") String token, @FieldMap Map<String, String> form);
}
