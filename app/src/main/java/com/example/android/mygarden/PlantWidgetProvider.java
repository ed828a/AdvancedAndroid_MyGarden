package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;


import static com.example.android.mygarden.provider.PlantContract.INVALID_PLANT_ID;
import static com.example.android.mygarden.ui.PlantDetailActivity.EXTRA_PLANT_ID;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {
    public static final String LOG_TAG = PlantWidgetProvider.class.getSimpleName();
    public static final int PLANT_REQUEST_CODE = 0;
    public static final int WATER_REQUEST_CODE = 1;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, int appWidgetId, long plantId, boolean allowWatering) {

        // Get current width to decide on single plant vs garden grid view
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        RemoteViews remoteViews;
        // single widget if under 300dp
        if (width < 300){
            remoteViews = getSinglePlantRemoteView(context, imgRes, plantId, allowWatering);
        } else {
            remoteViews = getGardenGridRemoteView(context);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    private static  RemoteViews getSinglePlantRemoteView(Context context, int imgRes,
                                                 long plantId, boolean showWater){

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);
        views.setImageViewResource(R.id.widget_plant_image, imgRes); // update image
        views.setTextViewText(R.id.plant_Id_text, String.valueOf(plantId)); // update plantId
        Log.i(LOG_TAG, "updateAppWidget() called, plantId=" + plantId);

        // show/hide water drop button
        if (showWater) {
            views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);
        }

        Intent intent;
        if (plantId == INVALID_PLANT_ID) {
            // Create an Intent to launch MainActivity when clicked.
            intent = new Intent(context, MainActivity.class);
        } else {
            intent = new Intent(context, PlantDetailActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PLANT_REQUEST_CODE, intent, 0);

        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);

        Intent startWateringIntent = new Intent(context, PlantWateringService.class);
        startWateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
        if (plantId != INVALID_PLANT_ID) startWateringIntent.putExtra(EXTRA_PLANT_ID, plantId);
        Log.i(LOG_TAG, "plantId = " + plantId);
        PendingIntent wateringPendingIntent = PendingIntent.getService(context,
                WATER_REQUEST_CODE, startWateringIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);

        return  views;
    }

    private static RemoteViews getGardenGridRemoteView(Context context){

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        // set the GridWidgetService intent to act as the adapter for the GridView
        Intent intent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.widget_grid_view, intent);

        // set the PlantDetailActivity intent to launch when clicked
        Intent appIntent = new Intent(context, PlantDetailActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0,
                                            appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_grid_view, appPendingIntent);

        // handle empty gardens
        views.setEmptyView(R.id.widget_grid_view, R.id.empty_view);

        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, int[] appWidgetIds,
                                          long plantId, boolean isAllowWatering) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, imgRes, appWidgetId,
                    plantId, isAllowWatering);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        PlantWateringService.startActionUpdatePlantWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more Appwidget instances have been deleted
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

