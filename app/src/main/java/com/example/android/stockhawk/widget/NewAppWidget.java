package com.example.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.example.android.stockhawk.R;
import com.example.android.stockhawk.sync.QuoteSyncJob;
import com.example.android.stockhawk.ui.MainActivity;
import com.example.android.stockhawk.ui.StockInDetail;

/**
 * Implementation of App Widget functionality.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewAppWidget extends AppWidgetProvider {

//    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
//                                int appWidgetId) {
//
//        CharSequence widgetText = context.getString(R.string.app_name);
//        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
//        //views.setTextViewText(R.id.appwidget_text, widgetText);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            setRemoteAdapter(context, views);
//        } else {
//            setRemoteAdapterV11(context, views);
//        }
//        /** PendingIntent to launch the MainActivity when the widget was clicked **/
//        Intent launchMain = new Intent(context, MainActivity.class);
//        PendingIntent pendingMainIntent = PendingIntent.getActivity(context, 0, launchMain, 0);
//        views.setOnClickPendingIntent(R.id.widget_toolbar, pendingMainIntent);
//        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.widget_listView);
//
//        // Instruct the widget manager to update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, views);
//    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_toolbar, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }
            boolean useDetailActivity = context.getResources().getBoolean(R.bool.use_detail_activity);
            Intent clickIntentTemplate = useDetailActivity ? new Intent(context, StockInDetail.class) : new Intent(context, MainActivity.class);

            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_listView, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_listView, R.id.widget_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
             }
    }
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        //context.startService(new Intent(context, WidgetIntentService.class));

    }


//    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
//        views.setRemoteAdapter(R.id.widget_listView,
//                new Intent(context, StockWidgetService.class));
//    }



    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listView);
            //context.startService(new Intent(context, DetailWidgetRemoteViewsService.class));

        }
    }
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
            views.setRemoteAdapter(R.id.widget_listView,
                    new Intent(context, DetailWidgetRemoteViewsService.class));
        }

        /**
         * Sets the remote adapter used to fill in the list items
         *
         * @param views RemoteViews to set the RemoteAdapter
         */
        @SuppressWarnings("deprecation")
        private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
            views.setRemoteAdapter(0, R.id.widget_listView,
                    new Intent(context, DetailWidgetRemoteViewsService.class));
        }
}

