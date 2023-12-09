package com.example.currencyconverter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.currencyconverter.api.Currencies;
import com.example.currencyconverter.api.CurrencyApiViewModel;
import com.example.currencyconverter.api.CurrencyModel;
import com.example.currencyconverter.api.OnReceivedRatesListner;
import com.example.currencyconverter.api.RetrofitClient;
import com.example.currencyconverter.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    AnimationDrawable animationDrawable;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayAdapter<Currencies> currenciesAdapter;
    Currencies base_currency, target_currency;
    Currencies[] otherTargets;
    TextView[] tv_bases, tv_targets;
    CurrencyApiViewModel currencyApiViewModel;
    ProgressDialog progressDialog;
    AlertDialog alertDialog;
    double value;
    public final String CURRENCIES_LATEST_KEY = "latest";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        currencyApiViewModel = new ViewModelProvider(this).get(CurrencyApiViewModel.class);

        sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String get_gson_string = sharedPreferences.getString(CURRENCIES_LATEST_KEY, "");

        tv_bases = new TextView[]{binding.mainTvBasePriceOne, binding.mainTvBasePriceTwo,
                binding.mainTvBasePriceThree, binding.mainTvBasePriceFour};

        tv_targets = new TextView[]{binding.mainTvTargetPriceOne, binding.mainTvTargetPriceTwo,
                binding.mainTvTargetPriceThree, binding.mainTvTargetPriceFour};

        currenciesAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_item, Currencies.values());

        binding.mainSpFrom.setAdapter(currenciesAdapter);
        binding.mainSpTo.setAdapter(currenciesAdapter);

        base_currency = (Currencies) binding.mainSpFrom.getSelectedItem();
        target_currency = (Currencies) binding.mainSpTo.getSelectedItem();

        defaultTargets(get_gson_string);

        otherTargets = targetCurrencies(base_currency, target_currency);

        binding.mainTvBase.setText(base_currency.name());
        binding.mainTvTarget.setText(target_currency.name());

        progressDialog = new ProgressDialog(this);

        binding.fab.setImageDrawable(getDrawable(R.drawable.rotate_fab));

        animationDrawable = (AnimationDrawable) binding.fab.getDrawable();

        binding.mainSpFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                base_currency = (Currencies) parent.getItemAtPosition(position);
                binding.mainTvBase.setText(base_currency.name());
                binding.mainEtFrom.setHint(getString(R.string.main_et_from)+" "+ base_currency);
                binding.mainEtFrom.setText("");
                binding.mainEtTo.setText("");
                setOthersTargetsText(base_currency, target_currency);
                otherTargets = targetCurrencies(base_currency, target_currency);
                String getJsonString = sharedPreferences.getString(CURRENCIES_LATEST_KEY, "");
                double target_value;
                double other_targets_value;
                if (! getJsonString.isEmpty()){
                    CurrencyModel currencyModel = convertStringToObject(getJsonString);
                    HashMap<Currencies, Double> getTargetHashMap = getTarget(base_currency, currencyModel.getRates());
                    currencyModel.setBase(base_currency.name());
                    currencyModel.setRates(getTargetHashMap);
                    target_value = rounding(currencyModel.getRates().get(target_currency));
                    setTextBaseAndTarget(base_currency, target_currency, binding.mainTvFrom, binding.mainTvTo, target_value);
                    for (int i = 0; i < otherTargets.length; i++){
                        other_targets_value = rounding(currencyModel.getRates().get(otherTargets[i]));
                        setTextBaseAndTarget(base_currency, otherTargets[i], tv_bases[i], tv_targets[i], other_targets_value);
                    }
                }else{
                    target_value = 0.0;
                    other_targets_value = 0.0;
                    setTextBaseAndTarget(base_currency, target_currency, binding.mainTvFrom, binding.mainTvTo, target_value);
                    for (int i = 0; i < otherTargets.length; i++){
                        setTextBaseAndTarget(base_currency, otherTargets[i], tv_bases[i], tv_targets[i], other_targets_value);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.mainSpTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                target_currency = (Currencies) parent.getItemAtPosition(position);
                binding.mainTvTarget.setText(target_currency.name());
                binding.mainEtTo.setHint(getString(R.string.main_et_to)+" "+ target_currency);
                binding.mainEtFrom.setText("");
                binding.mainEtTo.setText("");
                setOthersTargetsText(base_currency, target_currency);
                otherTargets = targetCurrencies(base_currency, target_currency);
                String getJsonString = sharedPreferences.getString(CURRENCIES_LATEST_KEY, "");
                double target_value;
                double other_targets_value;
                if (! getJsonString.isEmpty()){
                    CurrencyModel currencyModel = convertStringToObject(getJsonString);
                    HashMap<Currencies, Double> getTargetHashMap = getTarget(base_currency, currencyModel.getRates());
                    currencyModel.setBase(base_currency.name());
                    currencyModel.setRates(getTargetHashMap);
                    target_value = rounding(currencyModel.getRates().get(target_currency));
                    setTextBaseAndTarget(base_currency, target_currency, binding.mainTvFrom, binding.mainTvTo, target_value);
                    for (int i = 0; i < otherTargets.length; i++){
                        other_targets_value = rounding(currencyModel.getRates().get(otherTargets[i]));
                        setTextBaseAndTarget(base_currency, otherTargets[i], tv_bases[i], tv_targets[i], other_targets_value);
                    }
                }else{
                    target_value = 0.0;
                    other_targets_value = 0.0;
                    setTextBaseAndTarget(base_currency, target_currency, binding.mainTvFrom, binding.mainTvTo, target_value);
                    for (int i = 0; i < otherTargets.length; i++){
                        setTextBaseAndTarget(base_currency, otherTargets[i], tv_bases[i], tv_targets[i], other_targets_value);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.rotate_floating_button);
                binding.fab.startAnimation(animation);
                Currencies fromCurrency, toCurrency;
                fromCurrency = (Currencies) binding.mainSpFrom.getSelectedItem();
                toCurrency = (Currencies) binding.mainSpTo.getSelectedItem();
                binding.mainSpFrom.setSelection(currenciesAdapter.getPosition(toCurrency));
                binding.mainSpTo.setSelection(currenciesAdapter.getPosition(fromCurrency));
            }
        });

        binding.mainEtFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                otherTargets = targetCurrencies(base_currency, target_currency);
                if (! s.toString().isEmpty() && ! s.toString().equals(".")){
                    double target_value;
                    value = Double.parseDouble(s.toString());
                    String getJsonString = sharedPreferences.getString(CURRENCIES_LATEST_KEY, "");
                    if (! getJsonString.isEmpty()){
                        CurrencyModel currencyModel = convertStringToObject(getJsonString);
                        HashMap<Currencies, Double> getTargetHashMap = getTarget(base_currency, currencyModel.getRates());
                        currencyModel.setBase(base_currency.name());
                        currencyModel.setRates(getTargetHashMap);
                        target_value = rounding(currencyModel.getRates().get(target_currency));
                        setTextBaseAndTarget(base_currency, target_currency, binding.mainTvFrom, binding.mainTvTo, target_value);
                        double res = convertValue(value, currencyModel.getRates().get(target_currency));
                        binding.mainEtTo.setText(String.valueOf(res));
                        otherTargetsResults(targetCurrencies(base_currency,target_currency), getTargetHashMap);
                    }

                }else {
                    value = 0.0;
                    otherTargetsResults(targetCurrencies(base_currency,target_currency), null);
                    binding.mainEtTo.setText(String.valueOf(value));
                }
            }
        });

        binding.mainBtnFromPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String et_from = binding.mainEtFrom.getText().toString();
                if (et_from.isEmpty() || et_from.equals(".")){
                    binding.mainEtFrom.setText("0.0");
                }else{
                    double et_number = Double.parseDouble(et_from);
                    if (et_number != 99999999){
                        et_number++;
                        binding.mainEtFrom.setText(String.valueOf(et_number));
                    }
                }
            }
        });

        binding.mainBtnFromMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String et_from = binding.mainEtFrom.getText().toString();
                if (et_from.isEmpty() || et_from.equals(".")){
                    binding.mainEtFrom.setText("0.0");
                }else{
                    double et_number = Double.parseDouble(et_from);
                    if (et_number < 1){
                        binding.mainEtFrom.setText("0.0");
                    }else{
                        et_number--;
                        binding.mainEtFrom.setText(String.valueOf(et_number));
                    }
                }
            }
        });

        binding.mainBtnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! isNetworkConnected()){
                    networkErrorDialog();
                }else {
                    progressDialog.setMessage(getString(R.string.progress_dialog_message));
                    progressDialog.show();
                    currencyApiViewModel.getRates(new OnReceivedRatesListner() {
                        @Override
                        public void onReceivedRates(CurrencyModel currencyModel) {
                            String currencyModelJson = convertObjectToString(currencyModel);
                            editor.putString(CURRENCIES_LATEST_KEY, currencyModelJson);
                            editor.apply();
                            double other_targets_value;
                            HashMap<Currencies, Double> getTargetHashMap = getTarget(base_currency, currencyModel.getRates());
                            currencyModel.setBase(base_currency.name());
                            currencyModel.setRates(getTargetHashMap);
                            double target_value = currencyModel.getRates().get(target_currency);
                            setTextBaseAndTarget(base_currency, target_currency, binding.mainTvFrom, binding.mainTvTo, target_value);
                            for (int i = 0; i < otherTargets.length; i++){
                                other_targets_value = rounding(currencyModel.getRates().get(otherTargets[i]));
                                setTextBaseAndTarget(base_currency, otherTargets[i], tv_bases[i], tv_targets[i], other_targets_value);
                            }
                            String et_from = binding.mainEtFrom.getText().toString();
                            if (! et_from.isEmpty() && ! et_from.equals(".")){
                                double et_from_value = Double.parseDouble(et_from);
                                double result = convertValue(et_from_value, target_value);
                                binding.mainEtTo.setText(String.valueOf(result));
                                otherTargetsResults(targetCurrencies(base_currency,target_currency), getTargetHashMap);
                            }else{
                                otherTargetsResults(targetCurrencies(base_currency,target_currency), null);
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onReceivedRatesFailure(String message) {
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });

                }
            }
        });

    }

    public HashMap<Currencies, Double> getTarget(Currencies base, HashMap<Currencies,Double> rates){
        double value;
        HashMap<Currencies,Double> target = new HashMap<>();
        Double USD = rates.get(Currencies.USD);
        Double BASE = rates.get(base);
        Double BASE_TO_USD = USD / BASE;
        for (Currencies i : rates.keySet()){
            value = BASE_TO_USD * (rates.get(i));
            target.put(i,value);
        }
        return target;
    }

    public boolean isNetworkConnected(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        if (info != null) {
            return info.isConnected();
        }
        return false;
    }

    public void networkErrorDialog(){
        if (alertDialog == null){
            alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.alert_dialog_title))
                    .setIcon(R.drawable.ic_baseline_error)
                    .setMessage(getString(R.string.alert_dialog_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.alert_dialog_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        }
        alertDialog.show();
    }

    public double convertValue(double value, double price){
        double r = value * price;
        return rounding(r);
    }

    public double rounding(double v){
        BigDecimal bd = new BigDecimal(v).setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void defaultTargets(String jsonString){
        if (jsonString.isEmpty()){
            setTextBaseAndTarget(base_currency, Currencies.EGP, binding.mainTvBasePriceOne, binding.mainTvTargetPriceOne, 0.0);
            setTextBaseAndTarget(base_currency, Currencies.EUR, binding.mainTvBasePriceTwo, binding.mainTvTargetPriceTwo, 0.0);
            setTextBaseAndTarget(base_currency, Currencies.GBP, binding.mainTvBasePriceThree, binding.mainTvTargetPriceThree, 0.0);
            setTextBaseAndTarget(base_currency, Currencies.SAR, binding.mainTvBasePriceFour, binding.mainTvTargetPriceFour, 0.0);

        }else{
            CurrencyModel currencyModel = convertStringToObject(jsonString);
            HashMap<Currencies, Double> getHashMap = currencyModel.getRates();

            setTextBaseAndTarget(base_currency, Currencies.EGP, binding.mainTvBasePriceOne, binding.mainTvTargetPriceOne, rounding(getHashMap.get(Currencies.EGP)));
            setTextBaseAndTarget(base_currency, Currencies.EUR, binding.mainTvBasePriceTwo, binding.mainTvTargetPriceTwo, rounding(getHashMap.get(Currencies.EUR)));
            setTextBaseAndTarget(base_currency, Currencies.GBP, binding.mainTvBasePriceThree, binding.mainTvTargetPriceThree, rounding(getHashMap.get(Currencies.GBP)));
            setTextBaseAndTarget(base_currency, Currencies.SAR, binding.mainTvBasePriceFour, binding.mainTvTargetPriceFour, rounding(getHashMap.get(Currencies.SAR)));

        }
    }

    public Currencies[] targetCurrencies(Currencies base, Currencies target){
        Currencies[] result = new Currencies[4];
        int index = 0;
        if (base.equals(target)){
            for (Currencies i : Currencies.values()){
                if (i.equals(base)){
                    continue;
                }else{
                    result [index] = i;
                    index ++;
                    if (index == 4){
                        break;
                    }
                }
            }
        }else{
            for (Currencies i : Currencies.values()){
                if (i.equals(base) || i.equals(target)){
                    continue;
                }else{
                    result [index] = i;
                    index ++;
                    if (index == 4){
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void otherTargetsResults(Currencies[] otherCurrencies, HashMap<Currencies,Double> rates){
        double baseValue, targetValueOne, targetValueTwo, targetValueThree, targetValueFour;
        String fromEntries = binding.mainEtFrom.getText().toString();
        if (! fromEntries.isEmpty() && ! fromEntries.equals(".")){
            baseValue = Double.parseDouble(fromEntries);
            targetValueOne = convertValue(baseValue, rates.get(otherCurrencies[0]));
            targetValueTwo = convertValue(baseValue, rates.get(otherCurrencies[1]));
            targetValueThree = convertValue(baseValue, rates.get(otherCurrencies[2]));
            targetValueFour = convertValue(baseValue, rates.get(otherCurrencies[3]));
            binding.mainTvResultOne.setText(String.valueOf(targetValueOne));
            binding.mainTvResultTwo.setText(String.valueOf(targetValueTwo));
            binding.mainTvResultThree.setText(String.valueOf(targetValueThree));
            binding.mainTvResultFour.setText(String.valueOf(targetValueFour));
        }else{
            binding.mainTvResultOne.setText(String.valueOf(0.0));
            binding.mainTvResultTwo.setText(String.valueOf(0.0));
            binding.mainTvResultThree.setText(String.valueOf(0.0));
            binding.mainTvResultFour.setText(String.valueOf(0.0));
        }

    }

    public void setOthersTargetsText(Currencies base, Currencies target){
        Currencies[] target_currencies = targetCurrencies(base, target);
        binding.mainTvTargetOne.setText(target_currencies[0].name());
        binding.mainTvTargetTwo.setText(target_currencies[1].name());
        binding.mainTvTargetThree.setText(target_currencies[2].name());
        binding.mainTvTargetFour.setText(target_currencies[3].name());
    }

    public String convertObjectToString(CurrencyModel currencyModel){
        Gson gson = new Gson();
        String currencyModelJson = gson.toJson(currencyModel);
        return currencyModelJson;
    }

    public CurrencyModel convertStringToObject(String jsonString){
        Gson gson = new Gson();
        CurrencyModel currencyModel = gson.fromJson(jsonString, CurrencyModel.class);
        return currencyModel;
    }

    public void setTextBaseAndTarget(Currencies base, Currencies target, TextView from, TextView to, double targetPrice){
        String textFrom, textTo;
        textFrom = "~ 1 " + base.name() + " = " + rounding(targetPrice) + " " + target.name();
        if (targetPrice == 0.0){
            textTo = "~ 1 " + target.name() + " = " + rounding(targetPrice) + " " + base.name();
        }else{
            textTo = "~ 1 " + target.name() + " = " + rounding((1 / targetPrice)) + " " + base.name();
        }
        from.setText(textFrom);
        to.setText(textTo);
    }
}