package com.example.currencyconverter.api;

import java.util.HashMap;

public class CurrencyModel {
    private String date;
    private String base;
    private HashMap<Currencies,Double> rates;

    public CurrencyModel(String date, String base, HashMap<Currencies, Double> rates) {
        this.date = date;
        this.base = base;
        this.rates = rates;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public HashMap<Currencies, Double> getRates() {
        return rates;
    }

    public void setRates(HashMap<Currencies, Double> rates) {
        this.rates = rates;
    }
}
