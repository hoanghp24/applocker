package com.vacapplock.api;

import com.vacapplock.model.AppVersion;
import com.vacapplock.model.Password;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();


    ApiService apiService = new Retrofit.Builder()
            .baseUrl("http://10.50.4.21:8095/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @POST("Login/LoginVACApp")
    Call<Password> sendPin(@Query("PIN") int pin);

    @GET("VersionAndroid/GetVersionAndroid")
    Call<List<AppVersion>> checkVersion(@Query("androidApp") String androidApp);
}
