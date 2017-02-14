package com.example.android.stockhawk;

/**
 * Created by radhikaparmar on 07/02/17.
 */

public class StockValueModel {


    public StockValueModel(String month,double stockCloseValue ) {
        this.stockCloseValue = stockCloseValue;
        this.month = month;
    }

    private double stockCloseValue;
    private String month;

}
