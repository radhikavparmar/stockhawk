package com.example.android.stockhawk.sync;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.example.android.stockhawk.data.Contract;
import com.example.android.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;

    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 1;
    private static final String mMarketClose = "Market close";

    private QuoteSyncJob() {
    }

    public static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            if(quotes==null){return;}else {
                Iterator<String> iterator = stockCopy.iterator();

                Timber.d(quotes.toString());

                ArrayList<ContentValues> quoteCVs = new ArrayList<>();

                while (iterator.hasNext()) {
                    String symbol = iterator.next();


                    Stock stock = quotes.get(symbol);
                    StockQuote quote = stock.getQuote();


                    String companyName = stock.getName();
                    String volume = quote.getVolume().toString();
                    String avgVol = quote.getAvgVolume().toString();
                    String open, low, high;
                    //BigDecimal d= quote.getDayHigh();
                    if (quote.getOpen() != null) {
                        open = quote.getOpen().toString();
                    } else {
                        open = mMarketClose;
                    }
                    if (quote.getDayLow() != null) {
                        low = quote.getDayLow().toString();
                    } else {
                        low = mMarketClose;
                    }
                    if (quote.getDayHigh() != null) {
                        high = quote.getDayHigh().toString();
                    } else {
                        high = mMarketClose;
                    }
                    //String high = quote.getDayHigh().toString();
                    //String low = quote.getDayLow().toString();
                    //String high = "123";
                    //String low = "321";
                    //String open = "122";
                    //
                    String preClose = quote.getPreviousClose().toString();


                    float price = quote.getPrice().floatValue();
                    float change = quote.getChange().floatValue();
                    float percentChange = quote.getChangeInPercent().floatValue();

                    // WARNING! Don't request historical data for a stock that doesn't exist!
                    // The request will hang forever X_x
                    List<HistoricalQuote> history = stock.getHistory(from, to, Interval.DAILY);

                    StringBuilder historyBuilder = new StringBuilder();

                    for (HistoricalQuote it : history) {
                        historyBuilder.append(it.getDate().getTimeInMillis());
                        historyBuilder.append(", ");
                        historyBuilder.append(it.getClose());
                        historyBuilder.append("\n");
                    }

                    ContentValues quoteCV = new ContentValues();
                    quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                    quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                    quoteCV.put(Contract.Quote.COLUMN_COMPANY_NAME, companyName);
                    quoteCV.put(Contract.Quote.COLUMN_VOLUME, volume);
                    quoteCV.put(Contract.Quote.COLUMN_AVG_VOL, avgVol);
                    quoteCV.put(Contract.Quote.COLUMN_HIGH, high);
                    quoteCV.put(Contract.Quote.COLUMN_LOW, low);
                    quoteCV.put(Contract.Quote.COLUMN_OPEN, open);
                    quoteCV.put(Contract.Quote.COLUMN_PREVIOUS_CLOSE, preClose);
                    quoteCVs.add(quoteCV);

                }

                context.getContentResolver()
                        .bulkInsert(
                                Contract.Quote.URI,
                                quoteCVs.toArray(new ContentValues[quoteCVs.size()]));
            }
//                Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());;
//                context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


}
