package com.example.currencyconverter.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CurrencyApi {
    String BASE_URL = "https://api.currencyfreaks.com/v2.0/rates/";
    @GET("latest")
    Call<CurrencyModel> getPrices(@Query("apikey") String api_key,@Query("symbols") String rates);
}
