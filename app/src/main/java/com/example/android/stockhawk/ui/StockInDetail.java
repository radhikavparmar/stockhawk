package com.example.android.stockhawk.ui;

import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.android.stockhawk.R;
import com.example.android.stockhawk.data.Contract;
import com.example.android.stockhawk.data.DbHelper;
import com.example.android.stockhawk.data.PrefUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


/**
 * Created by radhikaparmar on 07/02/17.
 */

public class StockInDetail extends AppCompatActivity {
    private static final String SYMBOL_EXTRA = "symbol";
    private static final String mYearly = "Yearly data";
    private static final String BOOL_KEY = "bool_value";
    @BindView(R.id.stock_title_in_detailview)
    TextView mStockTitle;
    @BindView(R.id.price_in_detailview)
    TextView mPriceTextView;
    @BindView(R.id.change_in_detailview)
    TextView mChangeTextView;
    @BindView(R.id.todays_date_in_detailview)
    TextView mdateTextView;
    @BindView(R.id.companyname_in_detailview)
    TextView mCompanyNameTextView;
    @BindView(R.id.vol_in_detailview)
    TextView mVolumeTextView;
    @BindView(R.id.avg_vol_in_detailview)
    TextView mAvgVolumeTextview;
    @BindView(R.id.high_in_detailview)
    TextView mHighTextview;
    @BindView(R.id.low_in_detailview)
    TextView mLowTextview;
    @BindView(R.id.open_in_detailview)
    TextView mOpenTextview;
    @BindView(R.id.prev_close_in_detailview)
    TextView mPrevCloseTextview;
    @BindView(R.id.stock_chart)
    LineChart mLineChart;
    private Bundle extras;
    private List<Entry> list;
    private SQLiteDatabase mDbs;
    private String mSymbol;
    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;
    private Boolean mToggle;
    private Date mToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_in_detail);
        ButterKnife.bind(this);
        extras = getIntent().getExtras();
        mSymbol = extras.getString(SYMBOL_EXTRA);
        mStockTitle.setText(mSymbol);
        DbHelper dbHelper = new DbHelper(this);
        mDbs = dbHelper.getWritableDatabase();
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
        mToday = Calendar.getInstance().getTime();

        Cursor cursor = getHistory();
        if (cursor != null && (cursor.getCount() > 0)) {
            cursor.moveToFirst();
            mCompanyNameTextView.setText(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_COMPANY_NAME)));

            mdateTextView.setText(mToday.toString());
            mVolumeTextView.setText(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_VOLUME)));
            mPriceTextView.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));
            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            final String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            final String percentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(this).equals(this.getString(R.string.pref_display_mode_absolute_key))) {
                mChangeTextView.setText(change);
                mToggle = true;
            } else {
                mChangeTextView.setText(percentage);
                mToggle = false;
            }
            if (savedInstanceState != null) {
                mToggle = savedInstanceState.getBoolean(BOOL_KEY, mToggle);

                if (mToggle == false) {
                    mChangeTextView.setText(percentage);

                } else {
                    mChangeTextView.setText(change);

                }
            }
            mChangeTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mToggle == true) {
                        mChangeTextView.setText(percentage);
                        mToggle = false;
                    } else {
                        mChangeTextView.setText(change);
                        mToggle = true;
                    }
                }
            });
            mAvgVolumeTextview.setText(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_AVG_VOL)));
            mHighTextview.setText(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HIGH)));
            mLowTextview.setText(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_LOW)));
            mOpenTextview.setText(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_OPEN)));
            mPrevCloseTextview.setText(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_PREVIOUS_CLOSE)));


            String columnHistory = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            String[] millandstock = columnHistory.split("[\\r\\n]+");
            list = new ArrayList<>();
            for (int i = millandstock.length - 1, k = 0; i >= 0; i--, k++) {
                String cv = millandstock[i].substring(15);
                list.add(k, new Entry(k, Float.parseFloat(cv)));
            }
            for (int i = 0; i < millandstock.length; i++) {
                System.out.println(list.get(i));
            }


            LineDataSet lds = new LineDataSet(list, mYearly);
            lds.setDrawCircles(false);

            LineData data = new LineData(lds);


            mLineChart.setData(data);
            TypedArray a = getTheme().obtainStyledAttributes(R.style.AppTheme, new int[]{R.attr.background});
            int attributeResourceId = a.getResourceId(0, 0);
            mLineChart.getXAxis().setTextColor(attributeResourceId);
            YAxis leftAxis = mLineChart.getAxisLeft();
            leftAxis.setTextColor(ColorTemplate.getHoloBlue());
            YAxis rightAxis = mLineChart.getAxisRight();
            rightAxis.setTextColor(ColorTemplate.getHoloBlue());
            Legend l = mLineChart.getLegend();
            l.setTextColor(ColorTemplate.getHoloBlue());
            mLineChart.invalidate();

        } else {

            Timber.d("no cursor value");
              }
//        mWeeklyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Cursor cursor = getHistory();
//
//                if (cursor != null && (cursor.getCount() > 0)) {
//                    cursor.moveToFirst();
//
//                    for (int i = 0; i < cursor.getCount(); i++) {
//
//                        String id = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
//                        System.out.println(id);
//
//                        String[] millandstock = id.split("[\\r\\n]+");
//                        for (String part : millandstock) {
//                            System.out.println("p " + part + " P");
//                        }
//
//                        System.out.println(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
//                        cursor.moveToNext();
//                    }
//
//                } else {
//
//                    Timber.d("no cursor value");
//                    //Toast.makeText(getApplicationContext(), "No cursor value", Toast.LENGTH_LONG).show();
//                }
//
//
//            }
//        });
    }


    public Cursor getHistory() {
        return mDbs.query(
                Contract.Quote.TABLE_NAME,
                null,
                Contract.Quote.COLUMN_SYMBOL + "='" + mSymbol + "'",
                null,
                null,
                null,
                null
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the values you need into "outState"
        super.onSaveInstanceState(outState);
        outState.putBoolean(BOOL_KEY, mToggle);
    }


}
