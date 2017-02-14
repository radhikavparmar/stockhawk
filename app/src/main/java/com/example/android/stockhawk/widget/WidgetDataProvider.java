package com.example.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.stockhawk.R;
import com.example.android.stockhawk.data.Contract;
import com.example.android.stockhawk.data.DbHelper;
import com.example.android.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by radhikaparmar on 14/02/17.
 */

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Cursor cursor;
    private Intent intent;
    private SQLiteDatabase mDbs;
    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;
    private Boolean mToggle;

    //For obtaining the activity's context and intent
    public WidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    private void initCursor() {
        if (cursor != null) {
            cursor.close();
        }
        final long identityToken = Binder.clearCallingIdentity();
        /**This is done because the widget runs as a separate thread
         when compared to the current app and hence the app's data won't be accessible to it
         because I'm using a content provided **/
        DbHelper dbHelper = new DbHelper(context);
        mDbs = dbHelper.getWritableDatabase();
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
        cursor = mDbs.query(
                Contract.Quote.TABLE_NAME,
                new String[]{Contract.Quote.COLUMN_SYMBOL, Contract.Quote.COLUMN_ABSOLUTE_CHANGE, Contract.Quote.COLUMN_PERCENTAGE_CHANGE, Contract.Quote.COLUMN_PRICE},
                null,
                null,
                null,
                null,
                null
        );
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onCreate() {
        initCursor();
        if (cursor != null) {
            cursor.moveToFirst();
        }
    }

    @Override
    public void onDataSetChanged() {
        initCursor();
    }

    @Override
    public void onDestroy() {
        cursor.close();
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        cursor.moveToPosition(i);
        remoteViews.setTextViewText(R.id.symbol_in_widget, cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
        remoteViews.setTextViewText(R.id.price_in_widget, dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));
        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);


        if (rawAbsoluteChange > 0) {
            remoteViews.setInt(R.id.change_in_widget, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            remoteViews.setInt(R.id.change_in_widget, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }
        final String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        final String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(context).equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            remoteViews.setTextViewText(R.id.change_in_widget, change);
            mToggle = true;
        } else {
            remoteViews.setTextViewText(R.id.change_in_widget, percentage);
            mToggle = true;
        }
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
