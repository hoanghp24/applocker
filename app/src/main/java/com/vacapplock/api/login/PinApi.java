package com.vacapplock.api.login;

import android.content.Context;
import android.widget.Toast;

import com.vacapplock.api.ApiService;
import com.vacapplock.model.Password;
import com.vacapplock.utils.LogUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PinApi {

    public interface OnPinVerificationListener {
        void onPinCorrect();

        void onPinIncorrect();
    }

    private OnPinVerificationListener listener;

    public PinApi(OnPinVerificationListener listener) {
        this.listener = listener;
    }

    public void verifyPin(String currentPin) {
        int pin = Integer.parseInt(currentPin);
        ApiService.apiService.sendPin(pin).enqueue(new Callback<Password>() {
            @Override
            public void onResponse(Call<Password> call, Response<Password> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LogUtil.i("API_RESPONSE", "Response body: " + response.body().toString());
                    listener.onPinCorrect();

                } else {
                    LogUtil.i("API_RESPONSE", "Response unsuccessful: " + response.message());
                    listener.onPinIncorrect();
                }
            }

            @Override
            public void onFailure(Call<Password> call, Throwable t) {
                Toast.makeText((Context) listener, "Network error", Toast.LENGTH_SHORT).show();
                listener.onPinIncorrect();
            }
        });
    }
}