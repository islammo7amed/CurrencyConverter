package com.example.currencyconverter.api;

public interface OnReceivedRatesListner {
    void onReceivedRates(CurrencyModel currencyModel);
    void onReceivedRatesFailure(String message);
}
