package com.example.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
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
 * Created by radhikaparmar on 15/02/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private Context context;
    private Cursor cursor;
    private Intent intent;
    private SQLiteDatabase mDbs;
    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;
    private Boolean mToggle;
    private static final String SYMBOL_EXTRA = "symbol";

    public final String TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor cursor = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                context = getApplicationContext();
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
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                Binder.restoreCallingIdentity(identityToken);
                Log.e(TAG, "     01");
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
                cursor.moveToPosition(position);
                remoteViews.setTextViewText(R.id.symbol_in_widget, cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                remoteViews.setTextViewText(R.id.price_in_widget, dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));
                float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
                Log.e(TAG,"  03percentageChange: "+percentageChange);

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


                 //   views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
                String symbolToSend = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));

                String description = symbolToSend + dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                   // setRemoteContentDescription(remoteViews, description);
                }

                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra(SYMBOL_EXTRA,symbolToSend);
Log.e(TAG,"  02symbolToSend: "+symbolToSend);
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return remoteViews;
            }

//            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//            private void setRemoteContentDescription(RemoteViews views, String description) {
//                views.setContentDescription(R.id.widget_icon, description);
//            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (cursor.moveToPosition(position))
                    return cursor.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
