package com.example.currencyconverter.api;

import androidx.lifecycle.ViewModel;

public class CurrencyApiViewModel extends ViewModel {

    public void getRates(OnReceivedRatesListner listner){
        CurrencyRepository.getRates(listner);
    }
}
