package com.example.currencyconverter.api;

public enum Currencies {
    USD("USD"),
    EGP("EGP"),
    EUR("EUR"),
    GBP("GBP"),
    SAR("SAR"),
    KWD("KWD");

    private String rate;

    Currencies(String rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return rate;
    }
}
