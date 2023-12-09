package com.example.currencyconverter.api;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrencyRepository {
    public static void getRates(OnReceivedRatesListner listner){
        Call<CurrencyModel> call = RetrofitClient.getInstance().getCurrencyApi().getPrices("0c7fb8c5a16a41699ef48c929eefa8b9", "USD,EGP,EUR,GBP,SAR,KWD");
        call.enqueue(new Callback<CurrencyModel>() {
            @Override
            public void onResponse(Call<CurrencyModel> call, Response<CurrencyModel> response) {
                listner.onReceivedRates(response.body());
            }

            @Override
            public void onFailure(Call<CurrencyModel> call, Throwable t) {
                listner.onReceivedRatesFailure(t.getMessage());
            }
        });
    }
}
