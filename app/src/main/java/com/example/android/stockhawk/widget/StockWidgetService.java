package com.example.android.stockhawk.widget;

/**
 * Created by radhikaparmar on 14/02/17.
 */

import android.content.Intent;
import android.widget.RemoteViewsService;

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(this, intent);
    }
}