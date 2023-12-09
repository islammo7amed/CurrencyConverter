package com.example.currencyconverter.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance = null;
    private CurrencyApi currencyApi;

    private RetrofitClient(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CurrencyApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        currencyApi = retrofit.create(CurrencyApi.class);
    }

    public static synchronized RetrofitClient getInstance(){
        if (instance == null)
            instance = new RetrofitClient();
        return instance;
    }

    public CurrencyApi getCurrencyApi(){
        return currencyApi;
    }
}
