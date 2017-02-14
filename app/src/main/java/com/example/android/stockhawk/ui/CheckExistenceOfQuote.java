package com.example.android.stockhawk.ui;

import android.os.AsyncTask;

import java.io.IOException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by radhikaparmar on 10/02/17.
 */
public class CheckExistenceOfQuote extends AsyncTask<String, Void, String> {

    public String doInBackground(String... symbols) {
        Stock stock = null;

        try {
            stock = YahooFinance.get(symbols[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String sym = stock.getName();

        return sym;

    }
}
